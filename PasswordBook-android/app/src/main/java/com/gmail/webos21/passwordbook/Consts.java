package com.gmail.webos21.passwordbook;

import java.text.SimpleDateFormat;

public class Consts {

    public static final boolean DEBUG = true;
    public static final boolean DB_DEBUG = false;

    public static final int DB_VERSION = 1;

    public static final int ACTION_PASS_CFG = 1;
    public static final int ACTION_LOGIN = 2;
    public static final int ACTION_ADD = 3;
    public static final int ACTION_MODIFY = 4;


    public static final int PERM_REQ_EXTERNAL_STORAGE = 101;

    public static final String EXTRA_ARG_ID = "com.gmail.webos21.pb.id";

    public static final String PREF_FILE = "pb_pref";
    public static final String PREF_PASSKEY = "pref_passkey";
    public static final String PREF_SHOW_ICON = "pref_show_icon";

    public static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

}
