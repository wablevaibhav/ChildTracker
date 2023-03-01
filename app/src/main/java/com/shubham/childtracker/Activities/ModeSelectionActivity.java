package com.shubham.childtracker.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shubham.childtracker.databinding.ActivityModeSelectionBinding;
import com.shubham.childtracker.utils.Constant;

public class ModeSelectionActivity extends AppCompatActivity {

    ActivityModeSelectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModeSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imgParentSignUp.setOnClickListener(view -> startParentSignUp());

        binding.imgChildSignUp.setOnClickListener(view -> startChildSignUp());

    }

    private void startParentSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        intent.putExtra(Constant.PARENT_SIGN_UP, true);
        startActivity(intent);
    }

    private void startChildSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        intent.putExtra(Constant.PARENT_SIGN_UP, false);
        startActivity(intent);

    }
}
