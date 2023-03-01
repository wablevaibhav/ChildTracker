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
import com.shubham.childtracker.interfaces.OnPasswordChangeListener;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.SharedPrefsUtils;
import com.shubham.childtracker.utils.Validators;

public class PasswordChangingDialogFragment extends DialogFragment {
    private EditText txtOldPassword;
    private EditText txtNewPassword;
    private EditText txtNewPasswordConfirmation;
    private OnPasswordChangeListener onPasswordChangeListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onPasswordChangeListener = (OnPasswordChangeListener) getActivity();

        txtOldPassword = view.findViewById(R.id.txtOldPassword);
        txtNewPassword = view.findViewById(R.id.txtNewPassword);
        txtNewPasswordConfirmation = view.findViewById(R.id.txtNewPasswordConfirmation);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(view1 -> {
            if (isValid()) {
                onPasswordChangeListener.onPasswordChange(txtNewPassword.getText().toString());
                dismiss();
            }

        });
        Button btnCancelChangePassword = view.findViewById(R.id.btnCancelChangePassword);
        btnCancelChangePassword.setOnClickListener(view12 -> dismiss());

    }


    private boolean isValid() {
        if (!Validators.isValidPassword(txtOldPassword.getText().toString())) {
            txtOldPassword.setError(getString(R.string.wrong_password));
            txtOldPassword.requestFocus();
            return false;
        }

        if (!txtOldPassword.getText().toString().equals(SharedPrefsUtils.getStringPreference(requireContext(), Constant.PASSWORD, ""))) {
            txtOldPassword.setError(getString(R.string.wrong_password));
            txtOldPassword.requestFocus();
            return false;
        }


        if (!Validators.isValidPassword(txtNewPassword.getText().toString())) {
            txtNewPassword.setError(getString(R.string.enter_valid_password));
            txtNewPassword.requestFocus();
            return false;
        }

        if (!txtNewPasswordConfirmation.getText().toString().equals(txtNewPassword.getText().toString())) {
            txtNewPasswordConfirmation.setError(getString(R.string.new_password_doesnt_match));
            txtNewPasswordConfirmation.requestFocus();
            return false;
        }

        return true;
    }
}

