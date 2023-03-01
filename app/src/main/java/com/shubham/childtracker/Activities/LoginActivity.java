package com.shubham.childtracker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shubham.childtracker.DialogFragments.InformationDialogFragment;
import com.shubham.childtracker.DialogFragments.LoadingDialogFragment;
import com.shubham.childtracker.DialogFragments.RecoverPasswordDialogFragment;
import com.shubham.childtracker.R;
import com.shubham.childtracker.databinding.ActivityLoginBinding;
import com.shubham.childtracker.interfaces.OnPasswordResetListener;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.LocaleUtils;
import com.shubham.childtracker.utils.SharedPrefsUtils;
import com.shubham.childtracker.utils.Validators;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements OnPasswordResetListener {

    ActivityLoginBinding binding;

    private static final String TAG = "LoginActivityTAG";
    private FirebaseAuth auth;
    private FragmentManager fragmentManager;
    private String uid;
    private DatabaseReference databaseReference;
    private boolean autoLoginPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragmentManager = getSupportFragmentManager();
        LocaleUtils.setAppLanguage(this);


        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");


        binding.btnLogin.setOnClickListener(v -> {
            autoLogin();
            String email = binding.txtLogInEmail.getText().toString().toLowerCase();
            String password = binding.txtLogInPassword.getText().toString();
            login(email, password);
        });

        binding.txtSignUp.setOnClickListener(v -> startModeSelectionActivity());

        binding.txtForgotPassword.setOnClickListener(v -> sendPasswordRecoveryEmail());

        autoLoginPrefs = SharedPrefsUtils.getBooleanPreference(this, Constant.AUTO_LOGIN, false);
        binding.checkBoxRememberMe.setChecked(autoLoginPrefs);

        String emailPrefs = SharedPrefsUtils.getStringPreference(this, Constant.EMAIL, "");
        String passwordPrefs = SharedPrefsUtils.getStringPreference(this, Constant.PASSWORD, "");
        if (autoLoginPrefs) {
            binding.txtLogInEmail.setText(emailPrefs);
            binding.txtLogInPassword.setText(passwordPrefs);
        }

        if (!Validators.isGooglePlayServicesAvailable(this)) {
            startInformationDialogFragment(getString(R.string.please_download_google_play_services));
            binding.btnLogin.setEnabled(false);
            binding.btnLogin.setClickable(false);
            binding.txtSignUp.setEnabled(false);
            binding.txtSignUp.setClickable(false);
            binding.txtForgotPassword.setEnabled(false);
            binding.txtForgotPassword.setClickable(false);
            binding.checkBoxRememberMe.setEnabled(false);
            binding.checkBoxRememberMe.setClickable(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (autoLoginPrefs) {
            if (Validators.isInternetAvailable(this)) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String email = user.getEmail();
                    checkMode(email);
                }
            } else
                startInformationDialogFragment(getResources().getString(R.string.you_re_offline_ncheck_your_connection_and_try_again));
        }
    }


    private void autoLogin() {
        SharedPrefsUtils.setBooleanPreference(this, Constant.AUTO_LOGIN, binding.checkBoxRememberMe.isChecked());
        SharedPrefsUtils.setStringPreference(this, Constant.EMAIL, binding.txtLogInEmail.getText().toString().toLowerCase());
        SharedPrefsUtils.setStringPreference(this, Constant.PASSWORD, binding.txtLogInPassword.getText().toString());

    }

    private void login(String email, String password) {
        if (isValid()) {
            final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
            startLoadingFragment(loadingDialogFragment);
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                stopLoadingFragment(loadingDialogFragment);
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    String email1 = Objects.requireNonNull(user).getEmail();
                    uid = user.getUid();
                    Log.i(TAG, "onComplete: user: " + user);
                    Log.i(TAG, "onComplete: email: " + email1);
                    Log.i(TAG, "onComplete: uid: " + uid);

                    if (Validators.isVerified(user)) checkMode(email1);
                    else startAccountVerificationActivity();
                } else {
                    String errorCode = null;
                    try {
                        errorCode = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                    String s = Objects.requireNonNull(errorCode);
                    switch (s) {
                        case "ERROR_INVALID_EMAIL":
                            binding.txtLogInEmail.setError(getString(R.string.enter_valid_email));
                            break;
                        case "ERROR_USER_NOT_FOUND":
                            binding.txtLogInEmail.setError(getString(R.string.email_isnt_registered));
                            break;
                        case "ERROR_WRONG_PASSWORD":
                            binding.txtLogInPassword.setError(getString(R.string.wrong_password));
                            break;
                        default:
                            Toast.makeText(LoginActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    private boolean isValid() {
        if (!Validators.isValidEmail(binding.txtLogInEmail.getText().toString())) {
            binding.txtLogInEmail.setError(getString(R.string.enter_valid_email));
            binding.txtLogInEmail.requestFocus();
            return false;
        }

        if (!Validators.isValidPassword(binding.txtLogInPassword.getText().toString())) {
            binding.txtLogInPassword.setError(getString(R.string.enter_valid_email));
            binding.txtLogInPassword.requestFocus();
            return false;
        }

        if (!Validators.isInternetAvailable(this)) {
            startInformationDialogFragment(getResources().getString(R.string.you_re_offline_ncheck_your_connection_and_try_again));
            return false;
        }

        return true;
    }

    private void startInformationDialogFragment(String message) {
        InformationDialogFragment informationDialogFragment = new InformationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.INFORMATION_MESSAGE, message);
        informationDialogFragment.setArguments(bundle);
        informationDialogFragment.setCancelable(false);
        informationDialogFragment.show(getSupportFragmentManager(), Constant.INFORMATION_DIALOG_FRAGMENT_TAG);
    }

    private void startLoadingFragment(LoadingDialogFragment loadingDialogFragment) {
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(fragmentManager, Constant.LOADING_FRAGMENT);
    }

    private void stopLoadingFragment(LoadingDialogFragment loadingDialogFragment) {
        loadingDialogFragment.dismiss();
    }

    private void checkMode(String email) {
        final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        startLoadingFragment(loadingDialogFragment);
        Query query = databaseReference.child("parents").orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadingDialogFragment.dismiss();
                if (dataSnapshot.exists()) {
                    startParentSignedInActivity();
                } else {
                    startChildSignedInActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: canceled");
            }
        });
    }

    private void startParentSignedInActivity() {
        Intent intent = new Intent(this, ParentSignedInActivity.class);
        startActivity(intent);
    }

    private void startChildSignedInActivity() {
        Intent intent = new Intent(this, ChildSignedInActivity.class);
        startActivity(intent);
    }

    private void startAccountVerificationActivity() {
        Intent intent = new Intent(this, AccountVerificationActivity.class);
        startActivity(intent);
    }

    private void startModeSelectionActivity() {
        Intent intent = new Intent(this, ModeSelectionActivity.class);
        startActivity(intent);

    }

    private void sendPasswordRecoveryEmail() {
        RecoverPasswordDialogFragment recoverPasswordDialogFragment = new RecoverPasswordDialogFragment();
        recoverPasswordDialogFragment.setCancelable(false);
        recoverPasswordDialogFragment.show(fragmentManager, Constant.RECOVER_PASSWORD_FRAGMENT);
    }

    @Override
    public void onOkClicked(String email) {
        sendPasswordRecoveryEmail(email);
    }

    @Override
    public void onCancelClicked() {
    }

    private void sendPasswordRecoveryEmail(String email) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, getString(R.string.password_reset_email_sent), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
