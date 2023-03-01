package com.shubham.childtracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class SharedPrefsUtils {


    public static String getStringPreference(Context context, String key, String defValue) {
        String value = null;
        SharedPreferences preferences = context.getSharedPreferences(Constant.KID_SAFE_PREFS, Context.MODE_PRIVATE);
        if (preferences != null) {
            value = preferences.getString(key, defValue);
        }
        return value;
    }


    public static void setStringPreference(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(Constant.KID_SAFE_PREFS, Context.MODE_PRIVATE);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }


    public static boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
        boolean value = defaultValue;
        SharedPreferences preferences = context.getSharedPreferences(Constant.KID_SAFE_PREFS, Context.MODE_PRIVATE);
        if (preferences != null) {
            value = preferences.getBoolean(key, defaultValue);
        }
        return value;
    }

    public static void setBooleanPreference(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(Constant.KID_SAFE_PREFS, Context.MODE_PRIVATE);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }
    }
}