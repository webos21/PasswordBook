package com.gmail.webos21.passwordbook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gmail.webos21.nano.NanoHTTPD;
import com.gmail.webos21.passwordbook.web.DirWebServer;
import com.gmail.webos21.passwordbook.web.HttpNewClientListener;
import com.gmail.webos21.passwordbook.web.PbWebServer;
import com.gmail.webos21.passwordbook.web.StaticResourceExtractor;
import com.gmail.webos21.passwordbook.web.UiLog;

import java.io.File;
import java.io.IOException;

public class NanoActivity extends AppCompatActivity {

    private final static String NO_SERVICE = "NO SERVICE!!";
    private final static String WEB_ADDR_ANY = "0.0.0.0";
    private final static int WEB_PORT = 9999;

    private TextView tvIpAddr;
    private TextView tvLog;

    private Switch swPbWeb;
    private Switch swDirWeb;

    private HttpNewClientListener nhcl;

    private DirWebServer dws;
    private PbWebServer pws;

    private DirWebSwitchListener dwsl;
    private PbWebSwitchListener pwsl;

    private UiLog logger;

    private File pwdir;
    private File exdir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nano);

        tvIpAddr = (TextView) findViewById(R.id.tv_ip_addr);
        tvLog = (TextView) findViewById(R.id.tv_log);

        dwsl = new DirWebSwitchListener();
        pwsl = new PbWebSwitchListener();

        swPbWeb = (Switch) findViewById(R.id.sw_pb_web);
        swDirWeb = (Switch) findViewById(R.id.sw_dir_web);

        swPbWeb.setOnCheckedChangeListener(pwsl);
        swDirWeb.setOnCheckedChangeListener(dwsl);

        tvLog.setText("");
        tvLog.setMovementMethod(new ScrollingMovementMethod());

        logger = new UiLog(tvLog);
        nhcl = new NewHttpClientListener();

        try {
            exdir = Environment.getExternalStorageDirectory();
            pwdir = getFilesDir();

            dws = new DirWebServer(WEB_ADDR_ANY, WEB_PORT, exdir);
            dws.setUiLog(logger);
            dws.setClientListener(nhcl);

            pws = new PbWebServer(WEB_ADDR_ANY, WEB_PORT, pwdir);
            pws.setUiLog(logger);
            pws.setClientListener(nhcl);

        } catch (Exception e) {
            e.printStackTrace();
        }

        tvIpAddr.setText(NanoActivity.NO_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        File indexHtml = new File(pwdir, "index.html");
        if (true || !indexHtml.exists()) {
            new StaticResourceExtractor(this, "static", pwdir.getPath()).execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (swPbWeb.isChecked()) {
            swPbWeb.setChecked(false);
        }
        if (swDirWeb.isChecked()) {
            swDirWeb.setChecked(false);
        }
    }

    private String getIpAddress() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    private void showDialogGrant(final String ip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder adBuilder = new AlertDialog.Builder(NanoActivity.this);
                adBuilder.setTitle("웹 접속 허용");
                adBuilder.setMessage("[" + ip + "]로부터 신규 웹 접속이 요청되었습니다.\n허용할까요?");
                adBuilder.setPositiveButton("허용",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                NanoActivity.this.dws.addWhiteList(ip);
                                NanoActivity.this.pws.addWhiteList(ip);
                            }
                        });
                adBuilder.setNegativeButton("거부",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // Nothing to do
                            }
                        });
                adBuilder.create().show();
            }
        });
    }

    private class NewHttpClientListener implements HttpNewClientListener {
        @Override
        public void onNewClientRequest(final String ip) {
            NanoActivity.this.showDialogGrant(ip);
        }
    }

    private class DirWebSwitchListener implements Switch.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (NanoActivity.this.dws != null) {
                if (isChecked) {
                    if (NanoActivity.this.swPbWeb.isChecked()) {
                        NanoActivity.this.swPbWeb.setChecked(false);
                    }
                    try {
                        String addr = "http://" + getIpAddress() + ":" + NanoActivity.WEB_PORT + "/";
                        NanoActivity.this.tvIpAddr.setText(addr);
                        NanoActivity.this.dws.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    NanoActivity.this.tvIpAddr.setText(NanoActivity.NO_SERVICE);
                    NanoActivity.this.dws.stop();
                }
            }
        }
    }

    private class PbWebSwitchListener implements Switch.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (NanoActivity.this.pws != null) {
                if (isChecked) {
                    if (NanoActivity.this.swDirWeb.isChecked()) {
                        NanoActivity.this.swDirWeb.setChecked(false);
                    }
                    try {
                        String addr = "http://" + getIpAddress() + ":" + NanoActivity.WEB_PORT + "/";
                        NanoActivity.this.tvIpAddr.setText(addr);
                        NanoActivity.this.pws.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    NanoActivity.this.tvIpAddr.setText(NanoActivity.NO_SERVICE);
                    NanoActivity.this.pws.stop();
                }
            }
        }
    }

}
