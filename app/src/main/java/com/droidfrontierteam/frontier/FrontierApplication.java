package com.droidfrontierteam.frontier;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.droidfrontierteam.frontier.services.ReportingService;
import com.droidfrontierteam.frontier.utils.SharedPreferencesHelper;

/**
 * Created by snigavig on 10.08.15.
 */
public class FrontierApplication extends Application {
    public static final String LOG_TAG = "frontier-app";
    private static Context mContext;
    private static SharedPreferencesHelper mSharedPreferencesHelper;
    private static FrontierApplication mInstance;

    public static FrontierApplication getInstance() {
        return mInstance;
    }

    public SharedPreferencesHelper getSharedPreferencesHelper() {
        return mSharedPreferencesHelper;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mSharedPreferencesHelper = new SharedPreferencesHelper(this);
        mInstance = this;
        Intent reportingServiceIntent = new Intent(this, ReportingService.class);
        reportingServiceIntent.setAction(ReportingService.ACTION.START_FOREGROUND_ACTION);
        startService(reportingServiceIntent);
    }
}
