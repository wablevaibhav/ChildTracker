package com.shubham.childtracker.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shubham.childtracker.R;
import com.shubham.childtracker.Activities.BlockedAppActivity;
import com.shubham.childtracker.Activities.ChildSignedInActivity;
import com.shubham.childtracker.Broadcasts.AppInstalledReceiver;
import com.shubham.childtracker.Broadcasts.AppRemovedReceiver;
import com.shubham.childtracker.Broadcasts.PhoneStateReceiver;
import com.shubham.childtracker.Broadcasts.ScreenTimeReceiver;
import com.shubham.childtracker.Broadcasts.SmsReceiver;
import com.shubham.childtracker.models.App;
import com.shubham.childtracker.models.Child;
import com.shubham.childtracker.models.Contact;
import com.shubham.childtracker.models.ScreenLock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.shubham.childtracker.NotificationChannelCreator.CHANNEL_ID;

public class MainForegroundService extends Service {
    public static final int NOTIFICATION_ID = 27;
    public static final String TAG = "MainServiceTAG";
    public static final String BLOCKED_APP_NAME_EXTRA = "com.shubham.childtracker.services.BLOCKED_APP_NAME_EXTRA";
    public static final int LOCATION_UPDATE_INTERVAL = 1;    //every 5 seconds
    public static final int LOCATION_UPDATE_DISPLACEMENT = 5;  //every 10 meters
    private ExecutorService executorService;
    private ArrayList<App> apps;
    private PhoneStateReceiver phoneStateReceiver;
    private SmsReceiver smsReceiver;
    private AppInstalledReceiver appInstalledReceiver;
    private AppRemovedReceiver appRemovedReceiver;
    private ScreenTimeReceiver screenTimeReceiver;
    private String uid;
    private String childEmail;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference("users");


    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        LockerThread thread = new LockerThread();
        executorService.submit(thread);
        new Thread(this::getInstalledApplications).start();
        Log.i(TAG, "onCreate: executed");
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        childEmail = Objects.requireNonNull(user).getEmail();
        uid = user.getUid();

        Intent notificationIntent = new Intent(this, ChildSignedInActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_kidsafe).setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);

        getUserLocation();

        ArrayList<Contact> contacts = getContacts();
        uploadContacts(contacts);

        Query appsQuery = databaseReference.child("childs").child(uid).child("apps");
        appsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    getApps();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query locationQuery = databaseReference.child("childs").child(uid).child("location");
        locationQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    setFence(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query screenTimeQuery = databaseReference.child("childs").child(uid).child("screenLock");
        screenTimeQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ScreenLock screenLock = dataSnapshot.getValue(ScreenLock.class);
                    Log.i(TAG, "onDataChangeX: hours: " + Objects.requireNonNull(screenLock).getHours());
                    Log.i(TAG, "onDataChangeX: minutes: " + screenLock.getMinutes());
                    Log.i(TAG, "onDataChangeX: isLocked: " + screenLock.isLocked());

                    if (screenLock.isLocked()) {
                        screenTimeReceiver = new ScreenTimeReceiver(screenLock);
                        IntentFilter screenTimeIntentFilter = new IntentFilter();
                        screenTimeIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
                        screenTimeIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                        registerReceiver(screenTimeReceiver, screenTimeIntentFilter);
                    } else {
                        if (screenTimeReceiver != null) {
                            unregisterReceiver(screenTimeReceiver);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        phoneStateReceiver = new PhoneStateReceiver(user);
        IntentFilter callIntentFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(phoneStateReceiver, callIntentFilter);

        smsReceiver = new SmsReceiver(user);
        IntentFilter smsIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, smsIntentFilter);

        appInstalledReceiver = new AppInstalledReceiver(user);
        IntentFilter appInstalledIntentFilter = new IntentFilter();
        appInstalledIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appInstalledIntentFilter.addDataScheme("package");
        registerReceiver(appInstalledReceiver, appInstalledIntentFilter);

        appRemovedReceiver = new AppRemovedReceiver(user);
        IntentFilter appRemovedIntentFilter = new IntentFilter();
        appRemovedIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appRemovedIntentFilter.addDataScheme("package");
        registerReceiver(appRemovedReceiver, appRemovedIntentFilter);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (phoneStateReceiver != null) {
            unregisterReceiver(phoneStateReceiver);
        }
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
        if (appInstalledReceiver != null) {
            unregisterReceiver(appInstalledReceiver);
        }
        if (appRemovedReceiver != null) {
            unregisterReceiver(appRemovedReceiver);
        }
        if (screenTimeReceiver != null) {
            unregisterReceiver(screenTimeReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getApps() {
        Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                    Child child = nodeShot.getValue(Child.class);
                    apps = Objects.requireNonNull(child).getApps();

                    Log.i(TAG, "onDataChange: child name: " + child.getName());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserLocation() {
        Log.i(TAG, "getUserLocation: executed");
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        LocationListener locationListener = location -> {
            if (location != null) {
                Log.i(TAG, "onLocationChanged: latitude: " + location.getLatitude());
                Log.i(TAG, "onLocationChanged: longitude: " + location.getLongitude());
                addUserLocationToDatabase(location, uid);
            } else {
                Log.i(TAG, "onLocationChanged: location is null");
            }
        };

        //these two statements will be only executed when the permission is granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
        }

    }

    private void addUserLocationToDatabase(Location location, String uid) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        HashMap<String, Object> update = new HashMap<>();
        update.put("latitude", latitude);
        update.put("longitude", longitude);
        databaseReference.child("childs").child(uid).child("location").updateChildren(update);
    }

    private void uploadContacts(ArrayList<Contact> contacts) {
        databaseReference.child("childs").child(uid).child("contacts").setValue(contacts);

    }

    private void setFence(DataSnapshot dataSnapshot) {
        final com.shubham.childtracker.models.Location childLocation = dataSnapshot.getValue(com.shubham.childtracker.models.Location.class);
        Log.i(TAG, "setFence: getLatitude " + Objects.requireNonNull(childLocation).getLatitude());
        Log.i(TAG, "setFence: getLongitude " + childLocation.getLongitude());
        Log.i(TAG, "setFence: isGeoFence " + childLocation.isGeoFence());
        Log.i(TAG, "setFence: isOutOfFence " + childLocation.isOutOfFence());
        Log.i(TAG, "setFence: getFenceCenterLatitude " + childLocation.getFenceCenterLatitude());
        Log.i(TAG, "setFence: getFenceCenterLongitude " + childLocation.getFenceCenterLongitude());
        Log.i(TAG, "setFence: getFenceDiameter " + childLocation.getFenceDiameter());

        if (childLocation.isGeoFence()) {
            Log.i(TAG, "setFence: true");
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i(TAG, "setFence: changed");
                    if (location != null) {
                        float[] distanceInMeters = new float[1];
                        Location.distanceBetween(childLocation.getFenceCenterLatitude(), childLocation.getFenceCenterLongitude(), location.getLatitude(), location.getLongitude(), distanceInMeters);

                        boolean outOfFence = distanceInMeters[0] > childLocation.getFenceDiameter();
                        if (outOfFence) {
                            Log.i(TAG, "setFence: OUT OF FENCE");
                            databaseReference.child("childs").child(uid).child("location").child("outOfFence").setValue(true);
                        } else {
                            databaseReference.child("childs").child(uid).child("location").child("outOfFence").setValue(false);
                        }
                    } else {
                        Log.i(TAG, "setFence: location is null");
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
            }


        }

    }

    @SuppressLint("Range")
    public ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (cursorInfo.moveToNext()) {
                        String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String contactNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Contact contact = new Contact(contactName, contactNumber);
                        contacts.add(contact);
                    }

                    cursorInfo.close();
                }
            }
            cursor.close();
        }
        return contacts;
    }

    private void getInstalledApplications() {
        PackageManager packageManager = getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);
        Collections.sort(applicationInfoList, new ApplicationInfo.DisplayNameComparator(packageManager));
        Iterator<ApplicationInfo> iterator = applicationInfoList.iterator();
        while (iterator.hasNext()) {
            ApplicationInfo applicationInfo = iterator.next();
            if (applicationInfo.packageName.contains("com.google") || applicationInfo.packageName.matches("com.android.chrome"))
                continue;
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                iterator.remove();
            }
        }
        prepareData(applicationInfoList, packageManager);
    }

    private void prepareData(List<ApplicationInfo> applicationInfoList, PackageManager packageManager/*, ArrayList<App> onlineAppsList*/) {
        ArrayList<App> appsList = new ArrayList<>();
        for (ApplicationInfo applicationInfo : applicationInfoList) {
            if (applicationInfo.packageName != null) {
                appsList.add(new App((String) applicationInfo.loadLabel(packageManager), applicationInfo.packageName, false));
            }
        }

        uploadApps(appsList);

    }

    private void uploadApps(ArrayList<App> appsList) {
        databaseReference.child("childs").child(uid).child("apps").setValue(appsList);
        Log.i(TAG, "uploadApps: done");
    }

    public String getTopAppPackageName() {
        String appPackageName = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                appPackageName = getLollipopForegroundAppPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appPackageName;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private String getLollipopForegroundAppPackageName() {
        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
            long milliSecs = 60 * 1000;
            Date date = new Date();
            List<UsageStats> foregroundApps = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, date.getTime() - milliSecs, date.getTime());
            if (foregroundApps.size() == 0) {
                Log.i(TAG, "getLollipopForegroundAppPackageName: queryUsageSize: empty");
            }


            long recentTime = 0;
            String recentPkg = "";
            for (UsageStats stats : foregroundApps) {

                if (stats.getLastTimeStamp() > recentTime) {
                    recentTime = stats.getLastTimeStamp();
                    recentPkg = stats.getPackageName();
                }

            }

            return recentPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return "";
    }

    class LockerThread implements Runnable {

        private Intent intent;

        public LockerThread() {
            intent = new Intent(MainForegroundService.this, BlockedAppActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        @Override
        public void run() {
            while (true) {

                if (apps != null) {

                    String foregroundAppPackageName = getTopAppPackageName();
                    Log.i(TAG, "run: foreground app: " + foregroundAppPackageName);

                    //TODO:: need to handle com.google.android.gsf &  com.sec.android.provider.badge
                    for (final App app : apps) {

                        if (foregroundAppPackageName.equals(app.getPackageName()) && app.isBlocked()) {
                            intent.putExtra(BLOCKED_APP_NAME_EXTRA, app.getAppName());
                            startActivity(intent);
                        }

                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}