package com.example.myapplication22;

import static android.app.Service.STOP_FOREGROUND_LEGACY;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class GeoLocationService extends Service {

    public static GeoLocationService staticInstance = null;
    GeoLocationClient locationClient = null;
    Notification.Builder notification = null;

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        staticInstance = this;
        locationClient = new GeoLocationClient(this);
        locationClient.startLocationUpdates();

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            Log.e("ForegroundService", "Service is running...");
                            Log.e("ForegroundService", "Longitude: " + locationClient.longitude);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();

        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_LOW
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNELID)
                    .setContentText("Activity Recognition is running....")
                    .setContentTitle("Mobile Tracking: currently tracking :)")
                    .setSmallIcon(com.azure.android.maps.control.R.drawable.mapbox_logo_icon);
        }

        startForeground(1001, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop location updates and release resources



        locationClient.stopLocationUpdates();
        stopForeground(true);

        Log.e("ForegroundService", "Service is destroyed...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
