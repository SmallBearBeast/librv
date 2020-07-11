package com.bear.librv;

import android.util.Log;

public class RvLog {
    static final String RV_LOG_TAG = "rv_log_tag";

    private static boolean DEBUG = false;

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }
}
