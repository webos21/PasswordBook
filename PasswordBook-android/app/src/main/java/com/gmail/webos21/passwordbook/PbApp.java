package com.gmail.webos21.passwordbook;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class PbApp extends Application {

    private static final String TAG = "PbApp";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private byte[] pkBytes;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Consts.DEBUG) {
            Log.i(TAG, "onCreate!!!!!!");
        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (Consts.DEBUG) {
            Log.i(TAG, "onTerminate!!!!!!");
        }
        pkBytes = null;
    }

    public byte[] getPkBytes() {
        return pkBytes;
    }

    public void setPkBytes(byte[] pkBytes) {
        this.pkBytes = pkBytes;
    }

}
