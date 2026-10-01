package com.shubham.childtracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class NotificationChannelCreator extends Application {
    public static final String CHANNEL_ID = "com.shubham.childtracker.utils.CHANNEL_ID";
    private static final String TAG = "NotificationChannelCreator";

    @Override
    public void onCreate() {
        super.onCreate();
        initFirebase();
        createNotificationChannel();
    }

    private void initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
        } catch (Exception e) {
            Log.w(TAG, "Default FirebaseApp initialization failed, using fallback options", e);
        }

        if (FirebaseApp.getApps(this).isEmpty()) {
            try {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey(getString(R.string.GoogleApi))
                        .setApplicationId("1:100000000000:android:com.shubham.childtracker")
                        .setDatabaseUrl("https://childtracker-default-rtdb.firebaseio.com")
                        .setProjectId("childtracker")
                        .build();
                FirebaseApp.initializeApp(this, options);
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Firebase with fallback options", e);
            }
        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Service Channel", NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }
}
