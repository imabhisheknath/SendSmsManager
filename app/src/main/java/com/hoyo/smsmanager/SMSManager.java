package com.hoyo.smsmanager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;

import static android.app.PendingIntent.FLAG_ONE_SHOT;


public abstract class SMSManager {
    private Context context;
    private String SMS_SENT = "SMS_SENT";
    private String SMS_DELIVERED = "SMS_DELIVERED";
    private int flag = 0;

    private BroadcastReceiver msgStatusReceiver;

    public SMSManager(Context context) {
        this.context = context;
        msgStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String number = intent.getStringExtra("extra_num");

                if (action == null) {
                    ContentValues contentValues;
                    contentValues = parseResponse("Some error Occurred", number);
                    onsendResponse(false, contentValues);
                    return;
                }

                if (action.equalsIgnoreCase("SMS_SENT")) {
                    ContentValues contentValues;
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:

                            contentValues = parseResponse("message sent", number);
                            onsendResponse(true, contentValues);
                            break;

                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            contentValues = parseResponse("Generic fail", number);
                            onsendResponse(false, contentValues);

                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:

                            contentValues = parseResponse("No service available", number);
                            onsendResponse(false, contentValues);

                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            contentValues = parseResponse("No pdu provided", number);
                            onsendResponse(false, contentValues);

                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:

                            contentValues = parseResponse("Radio was explicitly turned off", number);
                            onsendResponse(false, contentValues);

                            break;


                        default:
                            contentValues = parseResponse("no available", number);
                            onsendResponse(false, contentValues);
                    }
                }


                if (action.equalsIgnoreCase("SMS_DELIVERED")) {

                    ContentValues contentValues;

                    switch (getResultCode()) {
                        case Activity.RESULT_OK:

                            contentValues = parseResponse("Deliver Message", number);
                            onDeliverResponse(true, contentValues);
                            break;

                        case Activity.RESULT_CANCELED:
                            contentValues = parseResponse("Message Delivery Failed", number);
                            onDeliverResponse(false, contentValues);
                            break;

                        default:
                            contentValues = parseResponse("no available", number);
                            onDeliverResponse(false, contentValues);
                    }

                }


            }
        };


        context.registerReceiver(msgStatusReceiver, new IntentFilter(SMS_SENT));
        context.registerReceiver(msgStatusReceiver, new IntentFilter(SMS_DELIVERED));


    }


    public abstract void onsendResponse(boolean status, ContentValues contentValues);

    public abstract void onDeliverResponse(boolean status, ContentValues contentValues);


    public void sendMessage(String mobilenum, String msg) {
        Log.d("sad", mobilenum);
        flag++;

        SmsManager smsManager = SmsManager.getDefault();
        Intent sentintent = new Intent(SMS_SENT);
        Intent receiveintent = new Intent(SMS_DELIVERED);

        sentintent.putExtra("extra_num", mobilenum);
        receiveintent.putExtra("extra_num", mobilenum);

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, flag, sentintent, FLAG_ONE_SHOT);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(context, flag, receiveintent, FLAG_ONE_SHOT);
        smsManager.sendTextMessage(mobilenum, null, msg, sentPendingIntent, deliveredPendingIntent);

    }


    public void unRegister() {

        context.unregisterReceiver(msgStatusReceiver);
    }


    private ContentValues parseResponse(String response_msg, String number) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Key.USER_NUMBER, number);
        contentValues.put(Key.RESPONSE_MSG, response_msg);
        return contentValues;

    }
}
