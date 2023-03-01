package com.shubham.childtracker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shubham.childtracker.DialogFragments.InformationDialogFragment;
import com.shubham.childtracker.DialogFragments.LoadingDialogFragment;
import com.shubham.childtracker.R;
import com.shubham.childtracker.databinding.ActivitySignUpBinding;
import com.shubham.childtracker.models.Child;
import com.shubham.childtracker.models.Parent;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.Validators;

import java.util.Objects;


public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;

    private static final String TAG = "SingUpActivityTAG";
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FragmentManager fragmentManager;
    private String uid;
    private boolean parent = true;
    private boolean validParent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fragmentManager = getSupportFragmentManager();

        Intent intent = getIntent();
        parent = intent.getBooleanExtra(Constant.PARENT_SIGN_UP, true);

        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        binding.txtParentEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Query query = databaseReference.child("parents").orderByChild("email").equalTo(binding.txtParentEmail.getText().toString().toLowerCase());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        validParent = dataSnapshot.exists();
                        Log.i(TAG, "onDataChange: " + validParent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        if (!parent) {
            binding.txtParentEmail.setVisibility(View.VISIBLE);
        }

        binding.btnSignUp.setOnClickListener(v -> {
            String email = binding.txtSignUpEmail.getText().toString().toLowerCase();
            String password = binding.txtSignUpPassword.getText().toString();
            signUp(email, password);
        });

    }


    private void signUp(String email, String password) {
        if (isValid()) {
            final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
            startLoadingFragment(loadingDialogFragment);
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                stopLoadingFragment(loadingDialogFragment);
                if (task.isSuccessful()) {
                    signUpRoutine(binding.txtParentEmail.getText().toString().toLowerCase());
                } else {
                    String errorCode = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();
                    switch (errorCode) {
                        case "ERROR_INVALID_EMAIL":
                            binding.txtSignUpEmail.setError(getString(R.string.enter_valid_email));
                            break;
                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            binding.txtSignUpEmail.setError(getString(R.string.email_is_already_in_use));
                            break;
                        case "ERROR_WEAK_PASSWORD":
                            binding.txtSignUpPassword.setError(getString(R.string.weak_password));
                            break;
                        default:
                            Toast.makeText(SignUpActivity.this, getString(R.string.sign_up_falied), Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }
    }

    private void signUpRoutine(String parentEmail) {
        uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        Log.i(TAG, "signUpRoutine: UID: " + uid);
        addUserToDB(parentEmail, parent);
        startAccountVerificationActivity();
    }

    private void startAccountVerificationActivity() {
        Intent intent = new Intent(this, AccountVerificationActivity.class);
        startActivity(intent);
    }

    private void addUserToDB(String parentEmail, boolean parent) {
        String email;
        String name;
        email = binding.txtSignUpEmail.getText().toString().toLowerCase();
        name = binding.txtSignUpName.getText().toString().replaceAll("\\s+$", "");
        Log.i(TAG, "signUpRoutine: UID: " + uid);

        if (parent) {
            Parent p = new Parent(name, email);
            databaseReference.child("parents").child(uid).setValue(p);
        } else {
            Child c = new Child(name, email, parentEmail);
            databaseReference.child("childs").child(uid).setValue(c);
        }
    }

    private void startLoadingFragment(LoadingDialogFragment loadingDialogFragment) {
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(fragmentManager, Constant.LOADING_FRAGMENT);
    }

    private void stopLoadingFragment(LoadingDialogFragment loadingDialogFragment) {
        loadingDialogFragment.dismiss();
    }

    private boolean isValid() {
        if (!Validators.isValidName(binding.txtSignUpName.getText().toString())) {
            binding.txtSignUpName.setError(getString(R.string.name_validation));
            binding.txtSignUpName.requestFocus();
            return false;
        }

        if (!Validators.isValidEmail(binding.txtSignUpEmail.getText().toString())) {
            binding.txtSignUpEmail.setError(getString(R.string.enter_valid_email));
            binding.txtSignUpEmail.requestFocus();
            return false;
        }

        if (!parent) {
            if (!Validators.isValidEmail(binding.txtParentEmail.getText().toString().toLowerCase()) || !validParent) {
                binding.txtParentEmail.setError(getString(R.string.this_email_isnt_registered_as_parent));
                binding.txtParentEmail.requestFocus();
                return false;
            }
        }

        if (!Validators.isValidPassword(binding.txtSignUpPassword.getText().toString())) {
            binding.txtSignUpPassword.setError(getString(R.string.enter_valid_password));
            binding.txtSignUpPassword.requestFocus();
            return false;
        }

        if (!Validators.isInternetAvailable(this)) {
            startInformationDialogFragment();
            return false;
        }

        return true;
    }

    private void startInformationDialogFragment() {
        InformationDialogFragment informationDialogFragment = new InformationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.INFORMATION_MESSAGE, getResources().getString(R.string.you_re_offline_ncheck_your_connection_and_try_again));
        informationDialogFragment.setArguments(bundle);
        informationDialogFragment.setCancelable(false);
        informationDialogFragment.show(getSupportFragmentManager(), Constant.INFORMATION_DIALOG_FRAGMENT_TAG);
    }


}
