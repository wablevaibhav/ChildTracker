package com.shubham.childtracker.Broadcasts;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shubham.childtracker.models.ScreenLock;

import java.util.Calendar;

public class ScreenTimeReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenTimeReceiverTAG";
    private long startTime;
    private long screenTime;
    private final int allowedTime;
    private int dayOfYear;
    private int lastDayOfYear;
    private final Calendar calendar;


    public ScreenTimeReceiver(ScreenLock screenLock) {
        allowedTime = screenLock.getTimeInSeconds();
        calendar = Calendar.getInstance();
        Log.i(TAG, "ScreenTimeReceiver: allowedTime: " + allowedTime);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            startTime = System.currentTimeMillis();
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            restIfDifferentDay();

            if (screenTime >= allowedTime) {
                devicePolicyManager.lockNow();
            }

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            long endTime = System.currentTimeMillis();
            long period = endTime - startTime;
            lastDayOfYear = dayOfYear;

            Log.i(TAG, "onReceive: endTime: " + endTime);
            Log.i(TAG, "onReceive: period: " + period);

            if (period != startTime && period != endTime && period != 0) {
                screenTime += period / 1000;    //seconds
                Log.i(TAG, "onReceive: screenTime: " + screenTime + "s");
            }
        }


    }

    private void restIfDifferentDay() {
        dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        if (dayOfYear != lastDayOfYear) {
            screenTime = 0;
        }
    }
}
