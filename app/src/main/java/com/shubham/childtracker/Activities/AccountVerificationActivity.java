package com.shubham.childtracker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shubham.childtracker.R;
import com.shubham.childtracker.databinding.ActivityAccountVerificationBinding;
import com.shubham.childtracker.utils.LocaleUtils;

import java.util.Objects;

public class AccountVerificationActivity extends AppCompatActivity {

    ActivityAccountVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sendVerificationMessage();

        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified())
            startActivity(new Intent(this, LoginActivity.class));


        //timer starting for verification
        startCountDownTimer(binding.btnVerify);


        binding.btnVerify.setOnClickListener(view -> {
            sendVerificationMessage();
            startCountDownTimer(binding.btnVerify);

        });

    }

    private void startCountDownTimer(final Button btnVerify) {
        btnVerify.setEnabled(false);
        btnVerify.setClickable(false);
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                btnVerify.setText(String.valueOf(l / 1000));
            }

            @Override
            public void onFinish() {
                btnVerify.setEnabled(true);
                btnVerify.setClickable(true);
                btnVerify.setText(R.string.resend_verification_email);

            }
        }.start();
    }

    private void sendVerificationMessage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseAuth.getInstance().setLanguageCode(LocaleUtils.getAppLanguage());
        Objects.requireNonNull(user).sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                Toast.makeText(AccountVerificationActivity.this, getString(R.string.verification_email), Toast.LENGTH_LONG).show();
        });
    }


}
