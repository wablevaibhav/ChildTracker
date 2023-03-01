package com.shubham.childtracker.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shubham.childtracker.databinding.ActivityChildSignedInBinding;

import java.util.Objects;

import com.shubham.childtracker.R;
import com.shubham.childtracker.DialogFragments.InformationDialogFragment;
import com.shubham.childtracker.DialogFragments.PasswordValidationDialogFragment;
import com.shubham.childtracker.DialogFragments.PermissionExplanationDialogFragment;
import com.shubham.childtracker.interfaces.OnPasswordValidationListener;
import com.shubham.childtracker.interfaces.OnPermissionExplanationListener;
import com.shubham.childtracker.services.MainForegroundService;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.SharedPrefsUtils;
import com.shubham.childtracker.utils.Validators;

public class ChildSignedInActivity extends AppCompatActivity implements OnPermissionExplanationListener, OnPasswordValidationListener {

    ActivityChildSignedInBinding binding;

    public static final String CHILD_EMAIL = "childEmail";
    private static final String TAG = "ChildSignedInTAG";

    @SuppressWarnings("deprecation")
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChildSignedInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean childFirstLaunch = SharedPrefsUtils.getBooleanPreference(this, Constant.CHILD_FIRST_LAUNCH, true);

        if (childFirstLaunch) {
            startActivity(new Intent(this, PermissionsActivity.class));
        } else {

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            String email = Objects.requireNonNull(user).getEmail();

            ImageButton btnBack = findViewById(R.id.btnBack);
            btnBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_home_));
            ImageButton btnSettings = findViewById(R.id.btnSettings);
            btnSettings.setOnClickListener(v -> startPasswordValidationDialogFragment());
            TextView txtTitle = findViewById(R.id.txtTitle);
            txtTitle.setText(getString(R.string.home));

            startMainForegroundService(email);

            if (!Validators.isLocationOn(this)) startPermissionExplanationDialogFragment();

            if (!Validators.isInternetAvailable(this))
                startInformationDialogFragment(getResources().getString(R.string.you_re_offline_ncheck_your_connection_and_try_again));

        }
    }

    private void startMainForegroundService(String email) {
        Intent intent = new Intent(this, MainForegroundService.class);
        intent.putExtra(CHILD_EMAIL, email);
        ContextCompat.startForegroundService(this, intent);

    }

    private void startPermissionExplanationDialogFragment() {
        PermissionExplanationDialogFragment permissionExplanationDialogFragment = new PermissionExplanationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.PERMISSION_REQUEST_CODE, Constant.CHILD_LOCATION_PERMISSION_REQUEST_CODE);
        permissionExplanationDialogFragment.setArguments(bundle);
        permissionExplanationDialogFragment.setCancelable(false);
        permissionExplanationDialogFragment.show(getSupportFragmentManager(), Constant.PERMISSION_EXPLANATION_FRAGMENT_TAG);
    }

    private void startInformationDialogFragment(String message) {
        InformationDialogFragment informationDialogFragment = new InformationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.INFORMATION_MESSAGE, message);
        informationDialogFragment.setArguments(bundle);
        informationDialogFragment.setCancelable(false);
        informationDialogFragment.show(getSupportFragmentManager(), Constant.INFORMATION_DIALOG_FRAGMENT_TAG);
    }

    private void startPasswordValidationDialogFragment() {
        PasswordValidationDialogFragment passwordValidationDialogFragment = new PasswordValidationDialogFragment();
        passwordValidationDialogFragment.setCancelable(false);
        passwordValidationDialogFragment.show(getSupportFragmentManager(), Constant.PASSWORD_VALIDATION_DIALOG_FRAGMENT_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.DEVICE_ADMIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult: DONE");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onOk(int requestCode) {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    @Override
    public void onCancel(int switchId) {
        Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onValidationOk() {
        Intent intent = new Intent(ChildSignedInActivity.this, SettingsActivity.class);
        startActivity(intent);
    }


}
