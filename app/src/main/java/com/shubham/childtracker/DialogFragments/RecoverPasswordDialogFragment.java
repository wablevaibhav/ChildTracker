package com.shubham.childtracker.DialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.shubham.childtracker.R;
import com.shubham.childtracker.interfaces.OnPasswordResetListener;
import com.shubham.childtracker.utils.Validators;

public class RecoverPasswordDialogFragment extends DialogFragment {
    private EditText txtRecoveryEmail;
    private OnPasswordResetListener onPasswordResetListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_recover_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        onPasswordResetListener = (OnPasswordResetListener) getActivity();
        txtRecoveryEmail = view.findViewById(R.id.txtRecoveryEmail);


        Button btnRecoverPassword = view.findViewById(R.id.btnRecoverPassword);
        btnRecoverPassword.setOnClickListener(v -> {
            String email = txtRecoveryEmail.getText().toString();
            if (Validators.isValidEmail(email)) {
                onPasswordResetListener.onOkClicked(email);
                dismiss();
            } else {
                txtRecoveryEmail.setError(getString(R.string.enter_valid_email));
                txtRecoveryEmail.requestFocus();
            }
        });

        Button btnCancelRecoverPassword = view.findViewById(R.id.btnCancelRecoverPassword);
        btnCancelRecoverPassword.setOnClickListener(v -> {
            onPasswordResetListener.onCancelClicked();
            dismiss();
        });
    }
}
