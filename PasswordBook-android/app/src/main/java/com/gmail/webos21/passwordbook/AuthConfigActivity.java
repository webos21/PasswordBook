package com.gmail.webos21.passwordbook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AuthConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edPassNew;
    private EditText edPassConfirm;
    private TextView tvMessage;
    private Button btnInputOk;

    private String inputPass;

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
        inputPass = "";
    }

    @Override
    protected void onStop() {
        super.onStop();
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

        prefEdit.putString(Consts.PREF_PASSKEY, p1);
        prefEdit.commit();

        finish();
    }

}
