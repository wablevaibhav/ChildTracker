package com.shubham.childtracker.DialogFragments;

import static com.shubham.childtracker.utils.Constant.CHILD_NAME_EXTRA;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.shubham.childtracker.R;
import com.shubham.childtracker.interfaces.OnGeoFenceSettingListener;
import com.shubham.childtracker.utils.Validators;

public class GeoFenceSettingDialogFragment extends DialogFragment {
	private Spinner spinnerGeoFenceEntries;
	private EditText txtGeoFenceDiameter;
	private OnGeoFenceSettingListener onGeoFenceSettingListener;
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_dialog_geo_fence, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		onGeoFenceSettingListener = (OnGeoFenceSettingListener) getTargetFragment();
		
		spinnerGeoFenceEntries = view.findViewById(R.id.spinnerGeoFenceEntries);
		txtGeoFenceDiameter = view.findViewById(R.id.txtGeoFenceDiameter);


		TextView txtGeoFenceBody = view.findViewById(R.id.txtGeoFenceBody);
		String body = getResources().getString(R.string.setup_geo_fence_on) + " " + getChildName() + getResources().getString(R.string.if_he_exceeded_it_you_will_be_alerted);
		txtGeoFenceBody.setText(body);

		Button btnSetGeoFence = view.findViewById(R.id.btnSetGeoFence);
		btnSetGeoFence.setOnClickListener(v -> {
			String selectedEntry = spinnerGeoFenceEntries.getSelectedItem().toString();
			String geoFenceDiameter = txtGeoFenceDiameter.getText().toString();

			if (!Validators.isValidGeoFenceDiameter(geoFenceDiameter)) {
				txtGeoFenceDiameter.setError(getResources().getString(R.string.please_enter_a_valid_diameter));
				txtGeoFenceDiameter.requestFocus();
			} else {
				onGeoFenceSettingListener.onGeoFenceSet(selectedEntry, Double.parseDouble(geoFenceDiameter));
				dismiss();
			}

		});

		Button btnCancelSetGeoFence = view.findViewById(R.id.btnCancelSetGeoFence);
		btnCancelSetGeoFence.setOnClickListener(view1 -> dismiss());
		
		
	}
	
	private String getChildName() {
		Bundle bundle = requireActivity().getIntent().getExtras();
		String childName = null;
		if (bundle != null) {
			childName = bundle.getString(CHILD_NAME_EXTRA);
		}
		return childName;
	}
}
