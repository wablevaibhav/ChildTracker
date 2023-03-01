package com.shubham.childtracker.DialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.shubham.childtracker.R;
import com.shubham.childtracker.interfaces.OnConfirmationListener;
import com.shubham.childtracker.utils.Constant;

import java.util.Objects;

public class ConfirmationDialogFragment extends DialogFragment {
    private OnConfirmationListener onConfirmationListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        onConfirmationListener = (OnConfirmationListener) getActivity();

        Bundle bundle = getArguments();
        String confirmationMessage = Objects.requireNonNull(bundle).getString(Constant.CONFIRMATION_MESSAGE);

        TextView txtConfirmationBody = view.findViewById(R.id.txtConfirmationBody);
        txtConfirmationBody.setText(confirmationMessage);

        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            onConfirmationListener.onConfirm();
            dismiss();
        });

        Button btnCancelConfirm = view.findViewById(R.id.btnCancelConfirm);
        btnCancelConfirm.setOnClickListener(v -> {
            onConfirmationListener.onConfirmationCancel();
            dismiss();
        });

    }
}
