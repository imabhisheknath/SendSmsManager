package com.hoyo.smsmanager;

import android.content.ContentValues;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SMSManager smsManager = new SMSManager(getApplicationContext()) {
            @Override
            public void onsendResponse(boolean status, ContentValues contentValues) {

                Log.e("sentmsg", status + "");
                Log.e("sentmsg", contentValues.get(Key.USER_NUMBER) + "\t" + contentValues.get(Key.RESPONSE_MSG));
            }

            @Override
            public void onDeliverResponse(boolean status, ContentValues contentValues) {
                Log.e("recv", status + "");
                Log.e("recv", contentValues.get(Key.USER_NUMBER) + "\t" + contentValues.get(Key.RESPONSE_MSG));
            }
        };

        smsManager.sendMessage("", "");

        //smsManager.unRegister();

    }
}
