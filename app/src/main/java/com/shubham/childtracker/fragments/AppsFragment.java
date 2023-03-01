package com.shubham.childtracker.fragments;

import static com.shubham.childtracker.utils.Constant.CHILD_EMAIL_EXTRA;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shubham.childtracker.Activities.ParentSignedInActivity;
import com.shubham.childtracker.Adapters.AppAdapter;
import com.shubham.childtracker.R;
import com.shubham.childtracker.interfaces.OnAppClickListener;
import com.shubham.childtracker.models.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AppsFragment extends Fragment implements OnAppClickListener {
    public static final String TAG = "AppsFragmentTAG";
    private DatabaseReference databaseReference;
    private ArrayList<App> apps;
    private RecyclerView recyclerViewApps;
    private Context context;
    private String childEmail;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");


        recyclerViewApps = view.findViewById(R.id.recyclerViewApps);
        recyclerViewApps.setHasFixedSize(true);
        recyclerViewApps.setLayoutManager(new LinearLayoutManager(getContext()));

        getData();
        initializeAdapter(this);

    }

    public void getData() {
        Bundle bundle = requireActivity().getIntent().getExtras();
        if (bundle != null) {
            apps = bundle.getParcelableArrayList(ParentSignedInActivity.APPS_EXTRA);
            childEmail = bundle.getString(CHILD_EMAIL_EXTRA);
        }
    }

    public void initializeAdapter(OnAppClickListener onAppClickListener) {
        AppAdapter appAdapter = new AppAdapter(context, apps);
        appAdapter.setOnAppClickListener(onAppClickListener);
        recyclerViewApps.setAdapter(appAdapter);
    }

    @Override
    public void onItemClick(final String packageName, String appName, boolean blocked) {
        if (blocked) {
            Toast.makeText(context, appName + " " + "blocked", Toast.LENGTH_SHORT).show();
            updateAppState(packageName, blocked);

        } else {
            Toast.makeText(context, appName + " enabled", Toast.LENGTH_SHORT).show();
            updateAppState(packageName, blocked);

        }
    }


    private void updateAppState(final String packageName, final boolean blocked) {
        Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                final String key = nodeShot.getKey();
                Log.i(TAG, "onDataChange: key: " + key);
                Query query = databaseReference.child("childs").child(Objects.requireNonNull(key)).child("apps").orderByChild("packageName").equalTo(packageName);  //changed from appName
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                            HashMap<String, Object> update = new HashMap<>();
                            update.put("blocked", blocked);
                            databaseReference.child("childs").child(key).child("apps").child(Objects.requireNonNull(snapshot.getKey())).updateChildren(update);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
