package com.droidfrontierteam.frontier.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by snigavig on 10.08.15.
 */
public class SharedPreferencesHelper {
    private static final String LAST_UPDATE_TIME_KEY = "LAST_UPDATE_TIME";

    private SharedPreferences prefs;

    public SharedPreferencesHelper(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void clearAll() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().commit();
    }

    public long getLastUpdateTime() {
        return prefs.getLong(LAST_UPDATE_TIME_KEY, -1);
    }

    public void setLastUpdateTime(long value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_UPDATE_TIME_KEY, value);
        editor.apply();
    }
}