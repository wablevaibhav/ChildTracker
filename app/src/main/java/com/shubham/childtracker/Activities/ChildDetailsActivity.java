package com.shubham.childtracker.Activities;

import static com.shubham.childtracker.Activities.ParentSignedInActivity.CHILD_NAME_EXTRA;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.shubham.childtracker.R;
import com.shubham.childtracker.databinding.ActivityChildDetailsBinding;
import com.shubham.childtracker.fragments.ActivityLogFragment;
import com.shubham.childtracker.fragments.AppsFragment;
import com.shubham.childtracker.fragments.LocationFragment;

import java.util.Objects;

public class ChildDetailsActivity extends AppCompatActivity {

    ActivityChildDetailsBinding binding;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChildDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        ImageButton btnSettings = findViewById(R.id.btnSettings);

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ChildDetailsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        TextView txtTitle = findViewById(R.id.txtTitle);

        Intent intent = getIntent();
        String childName = intent.getStringExtra(CHILD_NAME_EXTRA);
        String title = childName + getString(R.string.upper_dot_s) + " " + getString(R.string.device);
        txtTitle.setText(title);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new AppsFragment()).commit();

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navApps) {
                selectedFragment = new AppsFragment();
            } else if (itemId == R.id.navLocation) {
                selectedFragment = new LocationFragment();
            } else if (itemId == R.id.navActivityLog) {
                selectedFragment = new ActivityLogFragment();
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, Objects.requireNonNull(selectedFragment)).commit();
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ParentSignedInActivity.class));
    }
}
