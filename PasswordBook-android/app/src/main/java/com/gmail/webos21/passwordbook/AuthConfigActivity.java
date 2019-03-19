package com.gmail.webos21.passwordbook;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.webos21.passwordbook.crypt.PbCryptHelper;
import com.gmail.webos21.passwordbook.db.PbDbInterface;
import com.gmail.webos21.passwordbook.db.PbDbManager;
import com.gmail.webos21.passwordbook.db.PbKeyChanger;

public class AuthConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edPassNew;
    private EditText edPassConfirm;
    private TextView tvMessage;
    private Button btnInputOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_cfg);

        edPassNew = (EditText) findViewById(R.id.edPassNew);
        edPassConfirm = (EditText) findViewById(R.id.edPassConfirm);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        btnInputOk = (Button) findViewById(R.id.btnInputOk);

        btnInputOk.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Request Permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Consts.PERM_REQ_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onStop() {
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

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        switch (vId) {
            case R.id.btnInputOk:
                setNewPassword();
                break;
            default:
                break;
        }
    }

    private void setNewPassword() {
        String p1 = edPassNew.getText().toString();
        String p2 = edPassConfirm.getText().toString();

        if (p1 == null || p1.length() < 6) {
            tvMessage.setText(R.string.err_input_new);
            tvMessage.setVisibility(View.VISIBLE);
            edPassNew.requestFocus();
            return;
        }

        if (p2 == null || p2.length() < 6) {
            tvMessage.setText(R.string.err_input_confirm);
            tvMessage.setVisibility(View.VISIBLE);
            edPassConfirm.requestFocus();
            return;
        }

        if (!p1.equals(p2)) {
            tvMessage.setText(R.string.err_input_mismatch);
            tvMessage.setVisibility(View.VISIBLE);
            edPassConfirm.setText("");
            edPassConfirm.requestFocus();
            return;
        }

        SharedPreferences pref = getSharedPreferences(Consts.PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = pref.edit();

        String newPasskey = PbCryptHelper.makeSha256Base64(p1);

        prefEdit.putString(Consts.PREF_PASSKEY, newPasskey);
        prefEdit.commit();

        PbApp app = (PbApp) tvMessage.getContext().getApplicationContext();

        byte[] oldKey = app.getPkBytes();
        byte[] newKey = PbCryptHelper.restorePkBytes(newPasskey);
        if (oldKey != null) {
            PbDbInterface pdi = PbDbManager.getInstance().getPbDbInterface();
            new PbKeyChanger(pdi, oldKey, newKey, new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(tvMessage.getContext(), "New key is applyed!!!", Toast.LENGTH_LONG);
                }
            }).execute();
        }

        Intent i = new Intent();
        setResult(Activity.RESULT_OK, i);

        finish();
    }

}
