package com.shubham.childtracker.DialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.shubham.childtracker.R;
import com.shubham.childtracker.interfaces.OnLanguageSelectionListener;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.SharedPrefsUtils;

public class LanguageSelectionDialogFragment extends DialogFragment {
    private Spinner spinnerLanguageEntries;
    private OnLanguageSelectionListener onLanguageSelectionListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_language_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onLanguageSelectionListener = (OnLanguageSelectionListener) getActivity();

        String appLanguage = SharedPrefsUtils.getStringPreference(requireContext(), Constant.APP_LANGUAGE, "en");
        spinnerLanguageEntries = view.findViewById(R.id.spinnerLanguageEntries);
        if (appLanguage.equals("en")) spinnerLanguageEntries.setSelection(0);
        else if (appLanguage.equals("hi")) spinnerLanguageEntries.setSelection(1);


        Button btnOkLanguageSelection = view.findViewById(R.id.btnOkLanguageSelection);
        btnOkLanguageSelection.setOnClickListener(view12 -> {
            onLanguageSelectionListener.onLanguageSelection(spinnerLanguageEntries.getSelectedItem().toString());
            dismiss();
        });

        Button btnCancelLanguageSelection = view.findViewById(R.id.btnCancelLanguageSelection);
        btnCancelLanguageSelection.setOnClickListener(view1 -> dismiss());


    }
}
