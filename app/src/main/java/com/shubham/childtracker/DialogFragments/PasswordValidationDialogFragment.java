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
import com.shubham.childtracker.interfaces.OnPasswordValidationListener;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.SharedPrefsUtils;

public class PasswordValidationDialogFragment extends DialogFragment {
    private EditText txtValidationPassword;
    private OnPasswordValidationListener onPasswordValidationListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_password_validation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onPasswordValidationListener = (OnPasswordValidationListener) getActivity();

        txtValidationPassword = view.findViewById(R.id.txtValidationPassword);
        Button btnValidation = view.findViewById(R.id.btnValidation);
        Button btnCancelValidation = view.findViewById(R.id.btnCancelValidation);
        final String passwordPrefs = SharedPrefsUtils.getStringPreference(requireContext(), Constant.PASSWORD, "");

        btnValidation.setOnClickListener(view1 -> {
            if (txtValidationPassword.getText().toString().equals(passwordPrefs)) {
                onPasswordValidationListener.onValidationOk();
                dismiss();
            } else {
                txtValidationPassword.requestFocus();
                txtValidationPassword.setError(getString(R.string.wrong_password));
            }

        });


        btnCancelValidation.setOnClickListener(view12 -> dismiss());


    }
}
