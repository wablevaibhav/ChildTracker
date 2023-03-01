package com.shubham.childtracker.Activities;

import static com.shubham.childtracker.services.MainForegroundService.BLOCKED_APP_NAME_EXTRA;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shubham.childtracker.databinding.ActivityBlockedAppBinding;

public class BlockedAppActivity extends AppCompatActivity {

    ActivityBlockedAppBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockedAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Intent intent = getIntent();
        String blockedAppName = intent.getStringExtra(BLOCKED_APP_NAME_EXTRA);
        binding.txtBlockedAppName.setText(blockedAppName);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
