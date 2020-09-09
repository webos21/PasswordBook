package com.gmail.webos21.passwordbook;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gmail.webos21.passwordbook.crypt.PbCryptHelper;
import com.gmail.webos21.passwordbook.keypad.KeypadAdapter;
import com.gmail.webos21.passwordbook.keypad.KeypadButton;

import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity {

    private byte[] pkBytes;

    private View layoutView;

    private TextView tvPass1;
    private TextView tvPass2;
    private TextView tvPass3;
    private TextView tvPass4;
    private TextView tvPass5;
    private TextView tvPass6;

    private Switch swFinger;

    private GridView gvInputPad;

    private KeypadAdapter mKeypadAdapter;

    private String inputPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        layoutView = findViewById(R.id.activity_auth);

        tvPass1 = (TextView) findViewById(R.id.tvPass1);
        tvPass2 = (TextView) findViewById(R.id.tvPass2);
        tvPass3 = (TextView) findViewById(R.id.tvPass3);
        tvPass4 = (TextView) findViewById(R.id.tvPass4);
        tvPass5 = (TextView) findViewById(R.id.tvPass5);
        tvPass6 = (TextView) findViewById(R.id.tvPass6);

        swFinger = (Switch) findViewById(R.id.swFinger);

        gvInputPad = (GridView) findViewById(R.id.inputPad);

        mKeypadAdapter = new KeypadAdapter(this);
        mKeypadAdapter.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                // Get the KeypadButton value which is used to identify the
                // keypad button from the Button's tag
                KeypadButton keypadButton = (KeypadButton) btn.getTag();

                // Process keypad button
                processKeypadInput(keypadButton);
            }
        });

        gvInputPad.setAdapter(mKeypadAdapter);

        SharedPreferences shpref = getSharedPreferences(Consts.PREF_FILE, MODE_PRIVATE);
        String passkey = shpref.getString(Consts.PREF_PASSKEY, "000000");
        pkBytes = PbCryptHelper.restorePkBytes(passkey);

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager != null && biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            swFinger.setVisibility(View.VISIBLE);
        } else {
            swFinger.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        inputPass = "";

        // Request Permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Consts.PERM_REQ_EXTERNAL_STORAGE);
        }

        SharedPreferences shpref = getSharedPreferences(Consts.PREF_FILE, MODE_PRIVATE);
        boolean useFinger = shpref.getBoolean(Consts.PREF_FINGER, false);
        if (useFinger) {
            swFinger.setChecked(true);
            showFingerDialog();
        }
        swFinger.setOnCheckedChangeListener(new FingerPreferenceChanged());
    }

    @Override
    protected void onStop() {
        inputPass = "";
        showInputView();

        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == Consts.PERM_REQ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // OK, nothing to do
            } else {
                Toast.makeText(this, getResources().getString(R.string.err_perm_exflah), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void showFingerDialog() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(AuthActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent i = new Intent();
                setResult(Activity.RESULT_OK, i);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void showInputView() {
        Log.d("PASSWORD-STRING", inputPass);

        int ipLen = inputPass.length();
        switch (ipLen) {
            case 0:
                tvPass1.setText("");
                tvPass2.setText("");
                tvPass3.setText("");
                tvPass4.setText("");
                tvPass5.setText("");
                tvPass6.setText("");
                break;
            case 1:
                tvPass1.setText("*");
                tvPass2.setText("");
                tvPass3.setText("");
                tvPass4.setText("");
                tvPass5.setText("");
                tvPass6.setText("");
                break;
            case 2:
                tvPass1.setText("*");
                tvPass2.setText("*");
                tvPass3.setText("");
                tvPass4.setText("");
                tvPass5.setText("");
                tvPass6.setText("");
                break;
            case 3:
                tvPass1.setText("*");
                tvPass2.setText("*");
                tvPass3.setText("*");
                tvPass4.setText("");
                tvPass5.setText("");
                tvPass6.setText("");
                break;
            case 4:
                tvPass1.setText("*");
                tvPass2.setText("*");
                tvPass3.setText("*");
                tvPass4.setText("*");
                tvPass5.setText("");
                tvPass6.setText("");
                break;
            case 5:
                tvPass1.setText("*");
                tvPass2.setText("*");
                tvPass3.setText("*");
                tvPass4.setText("*");
                tvPass5.setText("*");
                tvPass6.setText("");
                break;
            case 6:
                tvPass1.setText("*");
                tvPass2.setText("*");
                tvPass3.setText("*");
                tvPass4.setText("*");
                tvPass5.setText("*");
                tvPass6.setText("*");

                layoutView.post(new CheckPasswordRunnable(layoutView));
                break;
            default:
                break;
        }
    }

    private void processKeypadInput(KeypadButton keypadButton) {
        Log.d("INPUT-KEY", keypadButton.getText().toString());

        if (KeypadButton.DUMMY == keypadButton) {
            return;
        }

        int ipLen = inputPass.length();

        if (KeypadButton.BACKSPACE == keypadButton) {
            if (ipLen > 0) {
                inputPass = inputPass.substring(0, inputPass.length() - 1);
                showInputView();
            }
            return;
        }

        if (ipLen < 6) {
            inputPass += keypadButton.getText();
            showInputView();
        }
    }

    private class FingerPreferenceChanged implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            SharedPreferences shpref = getSharedPreferences(Consts.PREF_FILE, MODE_PRIVATE);
            SharedPreferences.Editor prefEdit = shpref.edit();
            prefEdit.putBoolean(Consts.PREF_FINGER, b).commit();
            if (b) {
                showFingerDialog();
            }
        }
    }

    private class CheckPasswordRunnable implements Runnable {

        private View targetView;

        public CheckPasswordRunnable(View v) {
            this.targetView = v;
        }

        @Override
        public void run() {
            if (PbCryptHelper.checkPasskey(inputPass, pkBytes)) {
                Intent i = new Intent();
                setResult(Activity.RESULT_OK, i);
                finish();
            } else {
                Animation shake = AnimationUtils.loadAnimation(targetView.getContext(), R.anim.shake);
                targetView.startAnimation(shake);
                inputPass = "";
                showInputView();
            }
        }
    }

}
