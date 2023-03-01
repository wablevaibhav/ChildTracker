package com.shubham.childtracker.Broadcasts;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.shubham.childtracker.services.MainForegroundService;

public class BootCompleteReceiver extends BroadcastReceiver {
	@SuppressLint("UnsafeProtectedBroadcastReceiver")
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, MainForegroundService.class);
		ContextCompat.startForegroundService(context, serviceIntent);
	}
}
