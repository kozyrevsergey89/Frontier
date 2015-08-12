package com.droidfrontierteam.frontier.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.droidfrontierteam.frontier.FrontierApplication;
import com.droidfrontierteam.frontier.MainActivity;
import com.droidfrontierteam.frontier.R;
import com.droidfrontierteam.frontier.location.KalmanLocationManager;
import com.droidfrontierteam.frontier.utils.SharedPreferencesHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class ReportingService extends Service implements
        LocationListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final HashMap<FrontierApplication.AlertLevel, Long> ALERT_TIMEOUTS = new HashMap<>();
    private static final String LOCK_TAG = "com.droidfrontierteam.frontier.lock";
    private static final DateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private static final String TIMEZONE = "UTC";
    private static final long GPS_TIME = 500;
    private static final long NET_TIME = 3000;
    private static final long FILTER_TIME = 10000;
    private static final int NANOS_IN_SECOND = 1000000000;
    private static final int NANOS_IN_MILLISECOND = 1000000;

    static {
        FORMATTER.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        ALERT_TIMEOUTS.put(FrontierApplication.AlertLevel.emergency, (long) 120 * NANOS_IN_SECOND);
        ALERT_TIMEOUTS.put(FrontierApplication.AlertLevel.normal, (long) 1200 * NANOS_IN_SECOND);
    }

    private long ALERT_TIMEOUT;

    private PowerManager.WakeLock mWakeLock;
    private Handler refreshTimer = null;
    private Runnable refreshRunnable = null;
    // Context
    private KalmanLocationManager mKalmanLocationManager;

    public ReportingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void handleIntent(Intent intent) {
        if (ACTION.START_FOREGROUND_ACTION.equals(intent.getAction())) {
            Log.d(FrontierApplication.LOG_TAG, "Received Start Foreground Intent");
            ALERT_TIMEOUT = ALERT_TIMEOUTS.get(
                    FrontierApplication.AlertLevel.valueOf(
                            FrontierApplication.getInstance().getSharedPreferencesHelper().getAlertLevel()
                    )
            );
            FrontierApplication.getInstance().getSharedPreferencesHelper()
                    .getPrefs().registerOnSharedPreferenceChangeListener(this);
            FrontierApplication.getInstance().getSharedPreferencesHelper()
                    .setLastUpdateTime(System.nanoTime());
            mKalmanLocationManager = new KalmanLocationManager(this);
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire();
            }

            mKalmanLocationManager.requestLocationUpdates(
                    KalmanLocationManager.UseProvider.GPS_AND_NET, FILTER_TIME, GPS_TIME, NET_TIME, this, false);

            refreshTimer = new Handler();
            final int delay = 10000; //10 sec

            refreshRunnable = new Runnable() {
                public void run() {
                    long duration = System.nanoTime() - FrontierApplication
                            .getInstance()
                            .getSharedPreferencesHelper()
                            .getLastUpdateTime();
                    if (duration >= ALERT_TIMEOUT) {
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(500);
                    }

                    startForeground(
                            NOTIFICATION_ID.FOREGROUND_SERVICE,
                            buildNotification(duration)
                    );
                    refreshTimer.postDelayed(this, delay);
                }
            };
            refreshTimer.postDelayed(refreshRunnable, delay);

            startForeground(NOTIFICATION_ID.FOREGROUND_SERVICE, buildNotification(0));
        } else if (ACTION.UPDATE_FOREGROUND_ACTION.equals(intent.getAction())) {

            Log.d(FrontierApplication.LOG_TAG, "Received Update Foreground Intent");

            FrontierApplication.getInstance().getSharedPreferencesHelper().setLastUpdateTime(System.nanoTime());
            startForeground(NOTIFICATION_ID.FOREGROUND_SERVICE, buildNotification(0));
        } else if (ACTION.STOP_FOREGROUND_ACTION.equals(intent.getAction())) {
            Log.d(FrontierApplication.LOG_TAG, "Received Stop Foreground Intent");
            mKalmanLocationManager.removeUpdates(this);
            refreshTimer.removeCallbacks(refreshRunnable);
            stopForeground(true);
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            handleIntent(intent);
        }
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }

    private Notification buildNotification(long duration) {

        duration = duration / NANOS_IN_MILLISECOND;
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setTicker(getString(R.string.app_name))
                .setContentText("LAST UPDATE: " + FORMATTER.format(duration) + " AGO")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getProvider().equals(KalmanLocationManager.KALMAN_PROVIDER)) {

            //Toast.makeText(FrontierApplication.getInstance(),
            //     "Altitude ~= " + String.valueOf(Math.round(location.getAltitude())) + " m"
            //     , Toast.LENGTH_SHORT
            //).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SharedPreferencesHelper.ALERT_LEVEL_KEY:
                ALERT_TIMEOUT = ALERT_TIMEOUTS.get(
                        FrontierApplication.AlertLevel.valueOf(
                                FrontierApplication.getInstance().getSharedPreferencesHelper().getAlertLevel()
                        )
                );
                break;
        }
    }

    public interface ACTION {
        String MAIN_ACTION = "com.droidfrontierteam.action.main";
        String UPDATE_FOREGROUND_ACTION = "com.droidfrontierteam.action.update_foreground";
        String START_FOREGROUND_ACTION = "com.droidfrontierteam.action.start_foreground";
        String STOP_FOREGROUND_ACTION = "com.droidfrontierteam.action.stop_foreground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}