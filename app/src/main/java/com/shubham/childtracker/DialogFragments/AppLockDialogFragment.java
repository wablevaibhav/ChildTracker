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
import com.shubham.childtracker.utils.Constant;

import java.util.Objects;

public class AppLockDialogFragment extends DialogFragment {
    private Spinner spinnerLockAppEntries;
    private LinearLayout layoutLockAppTime;
    private EditText txtLockAppHours;
    private EditText txtLockAppMinutes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_lock_app, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String appName = Objects.requireNonNull(bundle).getString(Constant.CHILD_APP_NAME_EXTRA);
        String headerText = "Block " + appName;

        TextView txtLockAppHeader = view.findViewById(R.id.txtLockAppHeader);
        txtLockAppHeader.setText(headerText);

        Button btnLockApp = view.findViewById(R.id.btnLockApp);
        Button btnCancelLockApp = view.findViewById(R.id.btnCancelLockApp);
        layoutLockAppTime = view.findViewById(R.id.layoutLockAppTime);
        txtLockAppHours = view.findViewById(R.id.txtLockAppHours);
        txtLockAppMinutes = view.findViewById(R.id.txtLockAppMinutes);
        spinnerLockAppEntries = view.findViewById(R.id.spinnerLockAppEntries);
        spinnerLockAppEntries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    layoutLockAppTime.setVisibility(View.GONE);
                } else if (position == 1) {
                    layoutLockAppTime.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutLockAppTime.setVisibility(View.GONE);

            }
        });

        btnLockApp.setOnClickListener(v -> {
            if (spinnerLockAppEntries.getSelectedItemPosition() == 0) {
                dismiss();

            } else if (spinnerLockAppEntries.getSelectedItemPosition() == 1) {
                if (isValid()) {
                    dismiss();
                }

            }
        });

        btnCancelLockApp.setOnClickListener(v -> dismiss());
    }

    private boolean isValid() {
        if (txtLockAppHours.getText().toString().equals("")) {
            txtLockAppHours.setError(getString(R.string.enter_a_valid_number));
            return false;
        }
        if (txtLockAppMinutes.getText().toString().equals("")) {
            txtLockAppMinutes.setError(getString(R.string.enter_a_valid_number));
            return false;
        }
        if (Integer.parseInt(txtLockAppHours.getText().toString()) > 23) {
            txtLockAppHours.setError(getString(R.string.maximum_is_23_hours));
            return false;
        }
        if (Integer.parseInt(txtLockAppMinutes.getText().toString()) > 59) {
            txtLockAppMinutes.setError(getString(R.string.maximum_is_59_minutes));
            return false;
        }

        return true;
    }
}
