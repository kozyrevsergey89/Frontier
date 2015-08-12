package com.droidfrontierteam.frontier.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.droidfrontierteam.frontier.FrontierApplication;

/**
 * Created by snigavig on 10.08.15.
 */
public class SharedPreferencesHelper {
    public static final String LAST_UPDATE_TIME_KEY = "LAST_UPDATE_TIME";
    public static final String ALERT_LEVEL_KEY = "ALERT_LEVEL";

    private SharedPreferences prefs;

    public SharedPreferencesHelper(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void clearAll() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().apply();
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public long getLastUpdateTime() {
        return prefs.getLong(LAST_UPDATE_TIME_KEY, -1);
    }

    public void setLastUpdateTime(long value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_UPDATE_TIME_KEY, value);
        editor.apply();
    }

    public String getAlertLevel() {
        return prefs.getString(ALERT_LEVEL_KEY, FrontierApplication.AlertLevel.normal.name());
    }

    public void setAlertLevel(String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ALERT_LEVEL_KEY, value);
        editor.apply();
    }
}