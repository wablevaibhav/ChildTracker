package com.shubham.childtracker.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import com.shubham.childtracker.Adapters.ChildAdapter;
import com.shubham.childtracker.DialogFragments.PhoneLockDialogFragment;
import com.shubham.childtracker.R;
import com.shubham.childtracker.databinding.ActivityParentSignedInBinding;
import com.shubham.childtracker.interfaces.OnChildClickListener;
import com.shubham.childtracker.models.Child;
import com.shubham.childtracker.models.Parent;
import com.shubham.childtracker.models.ScreenLock;
import com.shubham.childtracker.models.User;
import com.shubham.childtracker.utils.Constant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ParentSignedInActivity extends AppCompatActivity implements OnChildClickListener {

    ActivityParentSignedInBinding binding;

    public static final String TAG = "ParentActivityTAG";
    public static final String APPS_EXTRA = "com.shubham.childtracker.activities.APPS_EXTRA";
    public static final String CHILD_NAME_EXTRA = "com.shubham.childtracker.activities.CHILD_NAME_EXTRA";
    public static final String CHILD_EMAIL_EXTRA = "com.shubham.childtracker.activities.CHILD_EMAIL_EXTRA";
    public static final String PARENT_EMAIL_EXTRA = "com.shubham.childtracker.activities.PARENT_EMAIL_EXTRA";
    public static final String CHILD_MESSAGES_EXTRA = "com.shubham.childtracker.activities.CHILD_MESSAGES_EXTRA";
    public static final String CHILD_CALLS_EXTRA = "com.shubham.childtracker.activities.CHILD_CALLS_EXTRA";
    private RecyclerView recyclerViewChilds;
    private ArrayList<Child> childs;
    private FrameLayout toolbar;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String childEmail;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParentSignedInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        toolbar = findViewById(R.id.toolbar);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.linearLayout.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_home_));
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ParentSignedInActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.home));

        recyclerViewChilds = findViewById(R.id.recyclerViewChilds);
        recyclerViewChilds.setHasFixedSize(true);
        recyclerViewChilds.setLayoutManager(new LinearLayoutManager(this));
        String parentEmail = user.getEmail();
        Log.i(TAG, "onCreate: user:" + user.getEmail());
        Log.i(TAG, "onCreate: user:" + user.getUid());
        getChilds(parentEmail);
        getParentData(parentEmail);

    }

    public void getChilds(String parentEmail) {
        Query query = databaseReference.child("childs").orderByChild("parentEmail").equalTo(parentEmail);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    childs = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        childs.add(child.getValue(Child.class));
                    }

                    binding.txtNoKids.setVisibility(View.GONE);
                    recyclerViewChilds.setVisibility(View.VISIBLE);
                    initializeAdapter();


                } else {
                    binding.txtNoKids.setVisibility(View.VISIBLE);
                    recyclerViewChilds.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.txtNoKids.setVisibility(View.VISIBLE);
                recyclerViewChilds.setVisibility(View.GONE);
            }
        });
    }

    public void initializeAdapter() {
        ChildAdapter childAdapter = new ChildAdapter(this, childs);
        childAdapter.setOnChildClickListener(this);
        recyclerViewChilds.setAdapter(childAdapter);
    }

    public void getParentData(String parentEmail) {
        Query query = databaseReference.child("parents").orderByChild("email").equalTo(parentEmail);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                    Parent parent = nodeShot.getValue(Parent.class);
                    String parentName = Objects.requireNonNull(parent).getName();
                    String profileImageUrl = parent.getProfileImage();
                    Picasso.get().load(profileImageUrl).placeholder(R.drawable.ic_profile_image).error(R.drawable.ic_profile_image).into(binding.imgParent);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.linearLayout.setVisibility(View.VISIBLE);
                    toolbar.setVisibility(View.VISIBLE);
                    binding.txtParentName.setText(parentName);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onItemClick(int position) {
        Child child = childs.get(position);
        if (!child.isAppDeleted()) {
            Intent intent = new Intent(this, ChildDetailsActivity.class);
            intent.putExtra(APPS_EXTRA, child.getApps());
            intent.putExtra(PARENT_EMAIL_EXTRA, user.getEmail());
            intent.putExtra(CHILD_NAME_EXTRA, child.getName());
            intent.putExtra(CHILD_EMAIL_EXTRA, child.getEmail());
            intent.putExtra(CHILD_MESSAGES_EXTRA, child.getMessages());
            intent.putExtra(CHILD_CALLS_EXTRA, child.getCalls());
            intent.putExtra(Constant.CHILD_CONTACTS_EXTRA, child.getContacts());
            startActivity(intent);
        }
    }

    @Override
    public void onWebFilterClick(boolean checked, User child) {
        String childEmail = child.getEmail();
        if (checked) {
            Toast.makeText(this, getString(R.string.web_filter_enabled), Toast.LENGTH_SHORT).show();
            Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                        nodeShot.getRef().child("webFilter").setValue(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(this, getString(R.string.web_filter_disabled), Toast.LENGTH_SHORT).show();
            Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                        nodeShot.getRef().child("webFilter").setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public void onBtnLockClick(boolean checked, User child) {
        childEmail = child.getEmail();
        if (checked) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            PhoneLockDialogFragment phoneLockDialogFragment = new PhoneLockDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.CHILD_NAME_EXTRA, child.getName());
            phoneLockDialogFragment.setArguments(bundle);
            phoneLockDialogFragment.setCancelable(false);
            phoneLockDialogFragment.show(fragmentManager, "PhoneLockDialogFragment");
        } else {
            Toast.makeText(this, getString(R.string.phone_unlocked), Toast.LENGTH_SHORT).show();
            ScreenLock screenLock = new ScreenLock(0, 0, false);
            updatePhoneLock(screenLock);
        }
    }

    @Override
    public void onLockPhoneSet(int hours, int minutes) {
        if (hours == 0 && minutes == 0) {
            Toast.makeText(this, getString(R.string.phone_locked), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.lock_timer_set_after) + " " + hours + " " + getString(R.string.hours) + " " + getString(R.string.and) + " " + minutes + " " + getString(R.string.minutes), Toast.LENGTH_SHORT).show();
        }
        ScreenLock screenLock = new ScreenLock(hours, minutes, true);
        updatePhoneLock(screenLock);
    }

    @Override
    public void onLockCanceled() {
        initializeAdapter();
    }


    private void updatePhoneLock(final ScreenLock screenLock) {
        Query childQuery = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
        childQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                    String uid = nodeShot.getKey();
                    databaseReference.child("childs").child(Objects.requireNonNull(uid)).child("screenLock").setValue(screenLock);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
