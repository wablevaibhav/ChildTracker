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
import com.shubham.childtracker.utils.Constant;

import java.util.Objects;

public class InformationDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_information, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        String message = Objects.requireNonNull(bundle).getString(Constant.INFORMATION_MESSAGE);

        TextView txtInformationBody = view.findViewById(R.id.txtInformationBody);
        txtInformationBody.setText(message);
        Button btnInformationOk = view.findViewById(R.id.btnInformationOk);
        btnInformationOk.setOnClickListener(view1 -> dismiss());
    }
}
