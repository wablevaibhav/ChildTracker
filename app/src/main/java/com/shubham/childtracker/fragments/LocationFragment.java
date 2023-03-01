package com.shubham.childtracker.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shubham.childtracker.R;
import com.shubham.childtracker.DialogFragments.GeoFenceSettingDialogFragment;
import com.shubham.childtracker.DialogFragments.InformationDialogFragment;
import com.shubham.childtracker.DialogFragments.PermissionExplanationDialogFragment;
import com.shubham.childtracker.interfaces.OnGeoFenceSettingListener;
import com.shubham.childtracker.interfaces.OnPermissionExplanationListener;
import com.shubham.childtracker.models.Child;
import com.shubham.childtracker.models.Location;
import com.shubham.childtracker.services.GeoFencingForegroundService;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.Validators;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import static com.shubham.childtracker.Activities.ParentSignedInActivity.CHILD_EMAIL_EXTRA;
import static com.shubham.childtracker.Activities.ParentSignedInActivity.CHILD_NAME_EXTRA;

import java.util.Objects;

public class LocationFragment extends Fragment implements OnGeoFenceSettingListener, OnPermissionExplanationListener {
    public static final int requestCode = 10;
    public static final String TAG = "LocationFragmentTAG";
    public static final int REQUEST_CODE = 922;
    public static final int LOCATION_UPDATE_INTERVAL = 1;    //every second
    public static final int LOCATION_UPDATE_DISPLACEMENT = 1;  //every meter
    private DatabaseReference databaseReference;
    private MapView mapView;
    private IMapController mapController;
    private String childEmail;
    private String childName;
    private Context context;
    private MyLocationNewOverlay locationNewOverlay;
    private Location userLocation;
    private TextView txtNoLocation;
    private FrameLayout layoutLocation;
    private ProgressBar progressbarLocationFragment;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.mapView);

        FloatingActionButton fabGeoFence = view.findViewById(R.id.fabGeoFence);
        fabGeoFence.setOnClickListener(v -> startGeoFencingDialogFragment());

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        context = getContext();
        Activity activity = getActivity();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(activity), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        } else {

            Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            //	mapView.setBuiltInZoomControls(false);
            mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            mapView.setMultiTouchControls(true);

            mapController = mapView.getController();
            mapController.setZoom(18);

            Log.i(TAG, "onViewCreated: executed");
            locationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mapView);
            locationNewOverlay.enableMyLocation();
            Log.i(TAG, "onViewCreated: not null");
            mapController.setCenter(locationNewOverlay.getMyLocation());

            progressbarLocationFragment = view.findViewById(R.id.progressbarLocationFragment);
            progressbarLocationFragment.setVisibility(View.VISIBLE);
            txtNoLocation = view.findViewById(R.id.txtNoLocation);
            txtNoLocation.setVisibility(View.GONE);
            layoutLocation = view.findViewById(R.id.layoutLocation);
            layoutLocation.setVisibility(View.GONE);

            getData();
            getUserLocation();
            getChildLocation();

        }
    }

    private void startGeoFencingDialogFragment() {
        GeoFenceSettingDialogFragment geoFenceSettingDialogFragment = new GeoFenceSettingDialogFragment();
        geoFenceSettingDialogFragment.setTargetFragment(LocationFragment.this, REQUEST_CODE);
        geoFenceSettingDialogFragment.setCancelable(false);
        geoFenceSettingDialogFragment.show(getParentFragmentManager(), Constant.GEO_FENCING_FRAGMENT_TAG);
    }


    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Configuration.getInstance().save(context, prefs);
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void getData() {
        Bundle bundle = requireActivity().getIntent().getExtras();
        if (bundle != null) {
            childEmail = bundle.getString(CHILD_EMAIL_EXTRA);
            childName = bundle.getString(CHILD_NAME_EXTRA);
        }
    }

    private void getChildLocation() {
        Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                    Child child = nodeShot.getValue(Child.class);
                    Location childLocation;
                    try {
                        childLocation = Objects.requireNonNull(child).getLocation();
                        addMarkerForChild(childLocation);
                        progressbarLocationFragment.setVisibility(View.GONE);
                        txtNoLocation.setVisibility(View.GONE);
                        layoutLocation.setVisibility(View.VISIBLE);
                    } catch (NullPointerException e) {
                        startInformationDialogFragment();
                        progressbarLocationFragment.setVisibility(View.GONE);
                        txtNoLocation.setVisibility(View.VISIBLE);
                        String body = getString(R.string.no_location_history_found) + " " + childName;
                        txtNoLocation.setText(body);
                        layoutLocation.setVisibility(View.GONE);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void startInformationDialogFragment() {
        InformationDialogFragment informationDialogFragment = new InformationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.INFORMATION_MESSAGE, requireActivity().getString(R.string.please_turn_on_the_gps) + " " + childName + requireActivity().getString(R.string.s_device));
        informationDialogFragment.setArguments(bundle);
        informationDialogFragment.setCancelable(false);
        informationDialogFragment.show(getParentFragmentManager(), Constant.INFORMATION_DIALOG_FRAGMENT_TAG);
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void addMarkerForChild(Location location) {
        mapView.getOverlays().clear();
        GeoPoint childGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        Marker childMarker = new Marker(mapView);
        childMarker.setPosition(childGeoPoint);
        childMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        childMarker.setTitle(childName);
        //childMarker.setTextIcon(childName);
        childMarker.setIcon(getResources().getDrawable(R.drawable.ic_location_child));
        mapView.getOverlays().add(childMarker);
        mapController.setCenter(childGeoPoint);
        addOverlays();
    }

    private void addOverlays() {

        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mapView);
        rotationGestureOverlay.setEnabled(true);

        mapView.getOverlays().add(rotationGestureOverlay);
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                if (location != null) {
                    userLocation = new Location(location.getLatitude(), location.getLongitude());
                    Log.i(TAG, "onLocationChanged: location lat: " + location.getLatitude() + "location long: " + location.getLongitude());
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //these two statements will be only executed when the permission is granted.
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
        }

    }

    @Override
    public void onGeoFenceSet(final String geoFenceCenter, final double geoFenceDiameter) {
        Log.i(TAG, "onGeoFenceSet: locationOverlay: " + locationNewOverlay.toString());
        Log.i(TAG, "onGeoFenceSet: location: " + locationNewOverlay.getMyLocation());
        Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                    Child child = nodeShot.getValue(Child.class);
                    Location childLocation = Objects.requireNonNull(child).getLocation();
                    String key = nodeShot.getKey();

                    if (geoFenceCenter.equals("You")) {
                        if (userLocation == null || !Validators.isLocationOn(context)) {
                            startPermissionExplanationDialogFragment();
                        } else {
                            double fenceCenterLatitude = userLocation.getLatitude();
                            double fenceCenterLongitude = userLocation.getLongitude();
                            double childLatitude = childLocation.getLatitude();
                            double childLongitude = childLocation.getLongitude();
                            Location location = new Location(childLatitude, childLongitude, geoFenceDiameter, fenceCenterLatitude, fenceCenterLongitude, false, true);
                            databaseReference.child("childs").child(Objects.requireNonNull(key)).child("location").setValue(location);
                            startFencingService();
                            Toast.makeText(context, getString(R.string.center) + geoFenceCenter + " " + getString(R.string.diameter) + geoFenceDiameter, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        double childLatitude = childLocation.getLatitude();
                        double childLongitude = childLocation.getLongitude();
                        Location location = new Location(childLatitude, childLongitude, geoFenceDiameter, childLatitude, childLongitude, false, true);
                        databaseReference.child("childs").child(Objects.requireNonNull(key)).child("location").setValue(location);
                        startFencingService();
                        Toast.makeText(context, getString(R.string.center) + geoFenceCenter + " " + getString(R.string.diameter) + geoFenceDiameter, Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void startFencingService() {
        Intent serviceIntent = new Intent(getActivity(), GeoFencingForegroundService.class);
        serviceIntent.putExtra(Constant.CHILD_EMAIL_EXTRA, childEmail);
        serviceIntent.putExtra(Constant.CHILD_NAME_EXTRA, childName);

        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }

    private void startPermissionExplanationDialogFragment() {
        PermissionExplanationDialogFragment permissionExplanationDialogFragment = new PermissionExplanationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.PERMISSION_REQUEST_CODE, Constant.CHILD_LOCATION_PERMISSION_REQUEST_CODE);
        permissionExplanationDialogFragment.setArguments(bundle);
        permissionExplanationDialogFragment.setCancelable(false);
        permissionExplanationDialogFragment.setTargetFragment(this, Constant.PERMISSION_EXPLANATION_FRAGMENT);
        permissionExplanationDialogFragment.show(getParentFragmentManager(), Constant.PERMISSION_EXPLANATION_FRAGMENT_TAG);
    }

    @Override
    public void onOk(int requestCode) {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    @Override
    public void onCancel(int switchId) {
        Toast.makeText(context, getString(R.string.canceled), Toast.LENGTH_SHORT).show();

    }

}
