package com.shubham.childtracker.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.shubham.childtracker.R;
import com.shubham.childtracker.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    ActivityAboutBinding binding;

    @SuppressWarnings("deprecation")
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        btnBack.setOnClickListener(v -> onBackPressed());
        ImageButton btnSettings = findViewById(R.id.btnSettings);

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.about));
    }
}
