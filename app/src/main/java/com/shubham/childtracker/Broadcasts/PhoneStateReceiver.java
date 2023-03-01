package com.shubham.childtracker.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shubham.childtracker.R;
import com.shubham.childtracker.models.Call;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.DateUtils;

public class PhoneStateReceiver extends BroadcastReceiver {
    public static final String TAG = "PhoneStateReceiver";
    private final DatabaseReference databaseReference;
    private final FirebaseUser user;
    private Context context;
    private double startCallTime;
    private Call call;

    public PhoneStateReceiver(FirebaseUser user) {
        this.user = user;
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            Log.i(TAG, "onReceive: phoneState: " + phoneState);
            String uid = user.getUid();

            String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            String contactName = getContactName(phoneNumber);

            String callTime = DateUtils.getCurrentDateString();

            if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                startCallTime = System.currentTimeMillis();

                call = new Call(Constant.INCOMING_CALL, phoneNumber, contactName, callTime, null);

            } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                startCallTime = System.currentTimeMillis();

                call = new Call(Constant.OUTGOING_CALL, phoneNumber, contactName, callTime, null);


            } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                double endCallTime = System.currentTimeMillis();
                double callDuration = (endCallTime - startCallTime) / 1000;

                call.setCallDurationInSeconds(String.valueOf(callDuration));
                databaseReference.child("childs").child(uid).child("calls").push().setValue(call);
                //TODO:: written 4 times

            }


        }

    }

    private String getContactName(String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        String contactName = context.getResources().getString(R.string.unknown_number);
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }
}
