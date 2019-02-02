package com.gmail.webos21.passwordbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.gmail.webos21.passwordbook.keypad.KeypadAdapter;
import com.gmail.webos21.passwordbook.keypad.KeypadButton;

public class AuthActivity extends AppCompatActivity {

    private static final String PASS_KEY = "0000";

    private View layoutView;

    private TextView tvPass1;
    private TextView tvPass2;
    private TextView tvPass3;
    private TextView tvPass4;

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
                ProcessKeypadInput(keypadButton);
            }
        });

        gvInputPad.setAdapter(mKeypadAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        inputPass = "";
    }

    @Override
    protected void onStop() {
        inputPass = "";
        ShowInputView();

        super.onStop();
    }

    private void ShowInputView() {
        Log.d("PASSWORD-STRING", inputPass);

        int ipLen = inputPass.length();
        switch (ipLen) {
            case 0:
                tvPass1.setText("");
                tvPass2.setText("");
                tvPass3.setText("");
                tvPass4.setText("");
                break;
            case 1:
                tvPass1.setText("*");
                tvPass2.setText("");
                tvPass3.setText("");
                tvPass4.setText("");
                break;
            case 2:
                tvPass1.setText("*");
                tvPass2.setText("*");
                tvPass3.setText("");
                tvPass4.setText("");
                break;
            case 3:
                tvPass1.setText("*");
                tvPass2.setText("*");
                tvPass3.setText("*");
                tvPass4.setText("");
                break;
            case 4:
                tvPass1.setText("*");
                tvPass2.setText("*");
                tvPass3.setText("*");
                tvPass4.setText("*");

                layoutView.post(new CheckPasswordRunnable(layoutView));
                break;
            default:
                break;
        }
    }

    private void ProcessKeypadInput(KeypadButton keypadButton) {
        Log.d("INPUT-KEY", keypadButton.getText().toString());

        if (KeypadButton.DUMMY == keypadButton) {
            return;
        }

        int ipLen = inputPass.length();

        if (KeypadButton.BACKSPACE == keypadButton) {
            if (ipLen > 0) {
                inputPass = inputPass.substring(0, inputPass.length() - 1);
                ShowInputView();
            }
            return;
        }

        if (ipLen < 4) {
            inputPass += keypadButton.getText();
            ShowInputView();
        }
    }

    private class CheckPasswordRunnable implements Runnable {

        private View targetView;

        public CheckPasswordRunnable(View v) {
            this.targetView = v;
        }

        @Override
        public void run() {
            if (PASS_KEY.equals(inputPass)) {
                Intent i = new Intent();
                setResult(Activity.RESULT_OK, i);
                finish();
            } else {
                Animation shake = AnimationUtils.loadAnimation(targetView.getContext(), R.anim.shake);
                targetView.startAnimation(shake);
                inputPass = "";
                ShowInputView();
            }
        }
    }

}
