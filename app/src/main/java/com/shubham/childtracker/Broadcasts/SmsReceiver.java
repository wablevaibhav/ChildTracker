package com.shubham.childtracker.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shubham.childtracker.models.Message;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.DateUtils;

public class SmsReceiver extends BroadcastReceiver {
    public static final String TAG = "SmsReceiver";
    private final DatabaseReference databaseReference;
    private final FirebaseUser user;
    private Context context;


    public SmsReceiver(FirebaseUser user) {
        this.user = user;
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Bundle bundle = intent.getExtras();
        String senderPhoneNumber = null;
        StringBuilder message;

        if (bundle != null) {
            Object[] object1 = (Object[]) bundle.get("pdus");
            message = new StringBuilder();

            for (Object object2 : object1) {
                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) object2);
                senderPhoneNumber = currentMessage.getDisplayOriginatingAddress();
                String messageBody = currentMessage.getDisplayMessageBody().replace("\n", " ");
                message.append(messageBody);
            }
            String timeReceived = DateUtils.getCurrentDateString();
            String uid = user.getUid();

            uploadMessage(senderPhoneNumber, message.toString(), timeReceived, uid);

        }

    }

    private void uploadMessage(String senderPhoneNumber, String messageBody, String timeReceived, String uid) {
        Log.i(TAG, "uploadMessage: messageBody" + messageBody);
        Log.i(TAG, "uploadMessage: senderPhoneNumber" + senderPhoneNumber);
        Log.i(TAG, "uploadMessage: timeReceived" + timeReceived);

        Message message = new Message(senderPhoneNumber, messageBody, timeReceived, getContactName(senderPhoneNumber));
        databaseReference.child("childs").child(uid).child("messages").push().setValue(message);


    }

    private String getContactName(String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        String contactName = Constant.UNKNOWN_NUMBER;
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
