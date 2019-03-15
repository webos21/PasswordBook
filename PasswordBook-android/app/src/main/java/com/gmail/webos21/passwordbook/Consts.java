package com.gmail.webos21.passwordbook;

import java.text.SimpleDateFormat;

public class Consts {

    public static final boolean DEBUG = true;

    public static final int DB_VERSION = 1;

    public static final int ACTION_PASS_CFG = 1;
    public static final int ACTION_LOGIN = 2;
    public static final int ACTION_ADD = 3;
    public static final int ACTION_MODIFY = 4;

    public static final String EXTRA_ID = "com.gmail.webos21.pb.id";

    public static final String PREF_FILE = "pb_pref";
    public static final String PREF_APPKEY = "PREF_APPKEY";
    public static final String PREF_PASSKEY = "PREF_PASSKEY";

    public static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

}
