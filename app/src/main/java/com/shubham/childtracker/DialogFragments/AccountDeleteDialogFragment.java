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
import com.shubham.childtracker.interfaces.OnDeleteAccountListener;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.SharedPrefsUtils;
import com.shubham.childtracker.utils.Validators;

public class AccountDeleteDialogFragment extends DialogFragment {
    private EditText txtDeleteAccountPassword;
    private OnDeleteAccountListener onDeleteAccountListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_delete_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onDeleteAccountListener = (OnDeleteAccountListener) getActivity();

        txtDeleteAccountPassword = view.findViewById(R.id.txtDeleteAccountPassword);
        Button btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(view1 -> {
            if (isValid()) {
                onDeleteAccountListener.onDeleteAccount(txtDeleteAccountPassword.getText().toString());
                dismiss();
            }
        });


        Button btnCancelDeleteAccount = view.findViewById(R.id.btnCancelDeleteAccount);
        btnCancelDeleteAccount.setOnClickListener(view12 -> dismiss());


    }

    private boolean isValid() {
        if (!Validators.isValidPassword(txtDeleteAccountPassword.getText().toString())) {
            txtDeleteAccountPassword.setError(getString(R.string.wrong_password));
            txtDeleteAccountPassword.requestFocus();
            return false;
        }

        if (!txtDeleteAccountPassword.getText().toString().equals(SharedPrefsUtils.getStringPreference(requireContext(), Constant.PASSWORD, ""))) {
            txtDeleteAccountPassword.setError(getString(R.string.wrong_password));
            txtDeleteAccountPassword.requestFocus();
            return false;
        }

        return true;
    }
}
