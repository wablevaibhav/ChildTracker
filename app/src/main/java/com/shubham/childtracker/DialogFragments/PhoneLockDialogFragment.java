package com.shubham.childtracker.DialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.shubham.childtracker.R;
import com.shubham.childtracker.interfaces.OnChildClickListener;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.Validators;

import java.util.Objects;

public class PhoneLockDialogFragment extends DialogFragment {
    private Spinner spinnerLockEntries;
    private LinearLayout layoutLockTime;
    private EditText txtLockHours;
    private EditText txtLockMinutes;
    private OnChildClickListener onChildClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_lock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        onChildClickListener = (OnChildClickListener) getActivity();

        Bundle bundle = getArguments();
        String childName = Objects.requireNonNull(bundle).getString(Constant.CHILD_NAME_EXTRA);


        layoutLockTime = view.findViewById(R.id.layoutLockTime);
        txtLockHours = view.findViewById(R.id.txtLockHours);
        txtLockMinutes = view.findViewById(R.id.txtLockMinutes);

        //private TextView txtLockHeader;
        TextView txtLockBody = view.findViewById(R.id.txtLockBody);
        String body = getString(R.string.lock) + " " + childName + getString(R.string.upper_dot_s) + " " + getString(R.string.phone) + " " + getString(R.string.now_or_after_a_period);
        txtLockBody.setText(body);

        spinnerLockEntries = view.findViewById(R.id.spinnerLockEntries);
        spinnerLockEntries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    layoutLockTime.setVisibility(View.GONE);
                } else if (position == 1) {
                    layoutLockTime.setVisibility(View.VISIBLE);
                    txtLockHours.requestFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutLockTime.setVisibility(View.GONE);

            }
        });

        Button btnLock = view.findViewById(R.id.btnLock);
        btnLock.setOnClickListener(v -> {
            int hours = 0;
            int minutes = 0;
            if (spinnerLockEntries.getSelectedItemPosition() == 0) {
                onChildClickListener.onLockPhoneSet(hours, minutes);
                dismiss();
            }
            if (spinnerLockEntries.getSelectedItemPosition() == 1) {
                if (Validators.isValidHours(txtLockHours.getText().toString())) {
                    hours = Integer.parseInt(txtLockHours.getText().toString());
                } else {
                    txtLockHours.setError(getResources().getString(R.string.maximum_is_23_hours));
                    txtLockHours.requestFocus();
                }

                if (Validators.isValidMinutes(txtLockMinutes.getText().toString())) {
                    minutes = Integer.parseInt(txtLockMinutes.getText().toString());
                } else {
                    txtLockMinutes.setError(getResources().getString(R.string.maximum_is_59_minutes));
                    txtLockMinutes.requestFocus();
                }

                if (Validators.isValidHours(txtLockHours.getText().toString()) && Validators.isValidMinutes(txtLockMinutes.getText().toString())) {
                    onChildClickListener.onLockPhoneSet(hours, minutes);
                    dismiss();
                }
            }

        });

        Button btnCancelLock = view.findViewById(R.id.btnCancelLock);
        btnCancelLock.setOnClickListener(v -> {
            onChildClickListener.onLockCanceled();
            dismiss();
        });
    }
}
