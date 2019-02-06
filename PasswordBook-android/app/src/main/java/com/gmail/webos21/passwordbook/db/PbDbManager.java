package com.gmail.webos21.passwordbook.db;

import android.content.Context;

import com.gmail.webos21.passwordbook.Consts;

public class PbDbManager {

    private static volatile PbDbManager instance;

    private Context context;
    private PbDbHelper dbHelper;

    private PbDbManager() {
    }

    public static PbDbManager getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (PbDbManager.class) {
            if (instance != null) {
                return instance;
            }
            instance = new PbDbManager();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.dbHelper = new PbDbHelper(context, "pb.db", null, Consts.DB_VERSION);
    }

    public void destroy() {
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
        if (context != null) {
            this.context = null;
        }
        if (instance != null) {
            instance = null;
        }
    }

    public PbDbInterface getPbDbInterface() {
        return this.dbHelper;
    }
}
