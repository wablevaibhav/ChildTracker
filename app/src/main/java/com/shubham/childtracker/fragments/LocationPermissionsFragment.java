package com.shubham.childtracker.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.shubham.childtracker.R;
import com.shubham.childtracker.DialogFragments.InformationDialogFragment;
import com.shubham.childtracker.DialogFragments.PermissionExplanationDialogFragment;
import com.shubham.childtracker.interfaces.OnFragmentChangeListener;
import com.shubham.childtracker.interfaces.OnPermissionExplanationListener;
import com.shubham.childtracker.utils.Constant;

public class LocationPermissionsFragment extends Fragment implements OnPermissionExplanationListener {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchLocationPermission;
    private Context context;
    private Activity activity;
    private View layout;
    private FragmentManager fragmentManager;
    private OnFragmentChangeListener onFragmentChangeListener;

    @Override
    public void onOk(int requestCode) {
        if (requestCode == Constant.LOCATION_PERMISSION_REQUEST_CODE) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constant.LOCATION_PERMISSION_REQUEST_CODE);

        }

    }

    @Override
    public void onCancel(int switchId) {
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch pressedSwitch = layout.findViewById(switchId);
        pressedSwitch.setChecked(false);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constant.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
                switchLocationPermission.setChecked(true);
                //switchLocationPermission.setEnabled(false);

            } else {
                Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                switchLocationPermission.setChecked(false);
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permissions_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        activity = getActivity();
        layout = view;
        fragmentManager = getFragmentManager();
        onFragmentChangeListener = (OnFragmentChangeListener) activity;

        Button btnPermissionsLocationNext = view.findViewById(R.id.btnPermissionsLocationNext);
        btnPermissionsLocationNext.setOnClickListener(v -> {
            if (isLocationPermissionGranted())//TODO:: OK?
                onFragmentChangeListener.onFragmentChange(Constant.PERMISSIONS_SETTINGS_FRAGMENT);
            else
                startInformationDialogFragment(getString(R.string.please_allow_location_permission));


        });

        Button btnPermissionsLocationPrev = view.findViewById(R.id.btnPermissionsLocationPrev);
        btnPermissionsLocationPrev.setOnClickListener(v -> onFragmentChangeListener.onFragmentChange(Constant.PERMISSIONS_PHONE_CALLS_FRAGMENT));

        switchLocationPermission = view.findViewById(R.id.switchLocationPermission);
        switchLocationPermission.setChecked(isLocationPermissionGranted());
        switchLocationPermission.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestLocationPermissions();
            }
        });

    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                startPermissionExplanationFragment(switchLocationPermission.getId());
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constant.LOCATION_PERMISSION_REQUEST_CODE);
            }

        }
    }

    private void startPermissionExplanationFragment(int id) {
        PermissionExplanationDialogFragment explanationFragment = new PermissionExplanationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.PERMISSION_REQUEST_CODE, Constant.LOCATION_PERMISSION_REQUEST_CODE);
        bundle.putInt(Constant.SWITCH_ID, id);
        explanationFragment.setArguments(bundle);
        explanationFragment.setCancelable(false);
        explanationFragment.setTargetFragment(this, Constant.PERMISSION_EXPLANATION_FRAGMENT);
        explanationFragment.show(fragmentManager, Constant.PERMISSION_EXPLANATION_FRAGMENT_TAG);
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startInformationDialogFragment(String message) {
        InformationDialogFragment informationDialogFragment = new InformationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.INFORMATION_MESSAGE, message);
        informationDialogFragment.setArguments(bundle);
        informationDialogFragment.setCancelable(false);
        informationDialogFragment.show(getChildFragmentManager(), Constant.INFORMATION_DIALOG_FRAGMENT_TAG);
    }

}
