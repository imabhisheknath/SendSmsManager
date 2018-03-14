package com.hoyo.smsmanager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Praba on 3/14/2018.
 */

public abstract class SMSRetryHandler {

    private static String MOBILE_NUMBER = "mobile_number";
    private static String MESSAGE = "message";
    private static String RETRY_POLICY_COUNT = "retry_policy_count";
    private static String RETRY_POLICY_TIME = "retry_policy_time";

    public abstract void onRetry(String mobileNumber, String message, SMSRetryPolicy smsRetryPolicy);

    public SMSRetryHandler(){

    }

    public SMSRetryHandler handle(String mMobileNumber, String mMessage, SMSRetryPolicy mSmsRetryPolicy){

        if(mMobileNumber!= null && mMobileNumber.length()>0
                && mMessage!= null && mMessage.length()>0
                && mSmsRetryPolicy!=null) {

            if(mSmsRetryPolicy.getRetryCount()>0){
                Message msg = smsHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(MOBILE_NUMBER,mMobileNumber);
                bundle.putString(MESSAGE,mMessage);
                bundle.putInt(RETRY_POLICY_COUNT,mSmsRetryPolicy.getRetryCount());
                bundle.putInt(RETRY_POLICY_TIME,mSmsRetryPolicy.getRetryTimeMilliSec());
                msg.setData(bundle);
                smsHandler.sendMessageDelayed(msg,mSmsRetryPolicy.getRetryTimeMilliSec());
            } // No Need to handle else part

        } // No need to handle the else part
        return this;
    }

    @SuppressLint("HandlerLeak")
    private Handler smsHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String mobileNumber = bundle.getString(MOBILE_NUMBER);
            String message = bundle.getString(MESSAGE);
            int retryCount = bundle.getInt(RETRY_POLICY_COUNT,SMSRetryPolicy.DEFAULT_RETRY_COUNT);
            int retryTime = bundle.getInt(RETRY_POLICY_TIME,SMSRetryPolicy.DEFAULT_RETRY_TIME);

            // call the Retry method

            onRetry(mobileNumber,message,new SMSRetryPolicy(retryCount,retryTime));

            // Update Retry Count
            bundle.putInt(RETRY_POLICY_COUNT,--retryCount);
            msg.setData(bundle);

            // If retryCount is Not Zero Try Again
            if(retryCount>0) {
                sendMessageDelayed(msg, retryTime);
            }
        }
    };

    public void remove(){
        if(smsHandler!=null){
            smsHandler.removeCallbacksAndMessages(null);
        }
    }

}
