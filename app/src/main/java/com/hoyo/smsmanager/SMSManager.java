package com.hoyo.smsmanager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

/*
    ToDo:
    1. Add the Error Messages To Strings.
    2. Add The Messages in different languages.
    3. Check for Grammatical Errors.
 */

public class SMSManager {

    /*
        ERROR Codes
     */

    // Sent Error Codes
    public static String E_SENT_GENERIC = "E0111";
    public static String E_SENT_RADIO_OFF = "E0112";
    public static String E_SENT_NULL_PDU = "E0113";
    public static String E_SENT_NO_SERVICE = "E0114";
    public static String E_SENT_STATUS_UNKNOWN = "E011000";

    // Delivery Error Codes
    public static String E_DELIVERY_FAILED = "E0120";
    public static String E_DELIVERY_STATUS_UNKNOWN = "E012000";

    // General Error Codes
    public static String E_SMS_UNKNOWN = "E013000";



    /*
        ******************************
     */
    private static String LOG_TITLE = SMSManager.class.getSimpleName();
    private Context context;
    private String SMS_SENT = "SMS_SENT";
    private String SMS_DELIVERED = "SMS_DELIVERED";
    private int flag = 0;
    private boolean isDebug = false;

    private SMSResponseListener mSmsResponseListener;
    private SMSErrorListener mSmsErrorListener;

    private BroadcastReceiver msgStatusReceiver;
    private SMSRetryHandler mSmsHandler;

    /**
     *
     * @param context : Activity Context
     */
    public SMSManager(Context context) {

        debugLOG("Constructor Begins ******************");
        this.context = context;
        this.initSMSHandler();

        msgStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                debugLOG("Broadcast Receiver : OnReceive");

                String action = intent.getAction();
                String number = intent.getStringExtra("extra_num");
                String message = intent.getStringExtra("extra_message");
                int retryCount = intent.getIntExtra("extra_retry_count",SMSRetryPolicy.DEFAULT_RETRY_COUNT);
                int retryTime = intent.getIntExtra("extra_retry_time",SMSRetryPolicy.DEFAULT_RETRY_TIME);
                SMSRetryPolicy smsRetryPolicy = new SMSRetryPolicy(retryCount,retryTime);

                debugLOG("Broadcast Receiver : Status of "+number);

                if (action == null) {

                    debugLOG("Broadcast Receiver : No Action Found");

                    if(mSmsErrorListener!=null){
                        mSmsErrorListener.OnSMSSentFailed(number,"Unknown Internal Error",E_SMS_UNKNOWN,(retryCount>0));
                    }

                    if(mSmsHandler!=null) {
                        mSmsHandler.handle(number,message,new SMSRetryPolicy(retryCount,retryTime));
                    }

                    return;
                }

                if (action.equalsIgnoreCase("SMS_SENT")) {

                    debugLOG("Broadcast Receiver : Action SMS_SENT");

                    switch (getResultCode()) {
                        case Activity.RESULT_OK:

                            debugLOG("Broadcast Receiver : SMS_SENT - Success");

                            if(mSmsResponseListener!=null){
                                mSmsResponseListener.OnSMSSentSuccess(number);
                            }
                            break;
                        default:
                            handleSentError(getResultCode(),number,message,smsRetryPolicy);
                    }
                }


                if (action.equalsIgnoreCase("SMS_DELIVERED")) {

                    debugLOG("Broadcast Receiver : Action SMS_DELIVERED");

                    switch (getResultCode()) {
                        case Activity.RESULT_OK:

                            debugLOG("Broadcast Receiver : SMS_DELIVERED Success");

                            if(mSmsResponseListener!=null){
                                mSmsResponseListener.OnSMSDeliverySuccess(number);
                            }
                            break;

                        default:
                            handleDeliveryError(getResultCode(),number,message,smsRetryPolicy);
                    }

                }


            }
        };

        debugLOG("Registering Receivers");

        context.registerReceiver(msgStatusReceiver, new IntentFilter(SMS_SENT));
        context.registerReceiver(msgStatusReceiver, new IntentFilter(SMS_DELIVERED));
        debugLOG("Constructor Finished ******************");

    }

    private void initSMSHandler(){
        mSmsHandler = new SMSRetryHandler() {
            @Override
            public void onRetry(String mobileNumber, String message, SMSRetryPolicy smsRetryPolicy) {
                sendMessage(mobileNumber,message,smsRetryPolicy);
            }
        };
    }


    /**
     *
     * @param mobileNum : 10 digit vaild mobile number
     * @param message : Text Message to be sent
     */
    public SMSManager sendMessage(String mobileNum, String message) {
        return this.sendMessage(mobileNum,message,SMSRetryPolicy.getDefaultRetryPolicy());
    }

    /**
     *
     * @param mobileNum
     * @param message
     * @param retryPolicy
     * @return
     */

    public SMSManager sendMessage(String mobileNum, String message, SMSRetryPolicy retryPolicy) {

        debugLOG("Sending SMS ******************");
        flag++;

        if(retryPolicy==null){
            retryPolicy = SMSRetryPolicy.getDefaultRetryPolicy();
        } else {
            debugLOG("Remaining Retry Attempts : "+retryPolicy.getRetryCount());
        }


        SmsManager smsManager = SmsManager.getDefault();
        Intent sentIntent = new Intent(SMS_SENT);
        Intent receiveIntent = new Intent(SMS_DELIVERED);

        sentIntent.putExtra("extra_num", mobileNum);
        sentIntent.putExtra("extra_message", message);
        sentIntent.putExtra("extra_retry_count", retryPolicy.getRetryCount());
        sentIntent.putExtra("extra_retry_time", retryPolicy.getRetryTimeMilliSec());

        receiveIntent.putExtra("extra_num", mobileNum);
        receiveIntent.putExtra("extra_message", message);
        receiveIntent.putExtra("extra_retry_count", retryPolicy.getRetryCount());
        receiveIntent.putExtra("extra_retry_time", retryPolicy.getRetryTimeMilliSec());

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, flag, sentIntent, FLAG_ONE_SHOT);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(context, flag, receiveIntent, FLAG_ONE_SHOT);
        smsManager.sendTextMessage(mobileNum, null, message, sentPendingIntent, deliveredPendingIntent);

        debugLOG("Sending SMS Finished ******************");

        return this;
    }

    private void handleSentError(int errorCode, String number, String message, SMSRetryPolicy smsRetryPolicy){

        boolean isRetry = (smsRetryPolicy.getRetryCount()>0);

        switch (errorCode) {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:

                debugLOG("Broadcast Receiver : SMS_SENT - Error: Generic Failure . " + E_SENT_GENERIC);

                if (mSmsErrorListener != null) {
                    mSmsErrorListener.OnSMSSentFailed(number, "Generic Failure", E_SENT_GENERIC,isRetry);
                }
                break;

            case SmsManager.RESULT_ERROR_NO_SERVICE:

                debugLOG("Broadcast Receiver : SMS_SENT - Error: No Service . " + E_SENT_NO_SERVICE);

                if (mSmsErrorListener != null) {
                    mSmsErrorListener.OnSMSSentFailed(number, "Service Not Available", E_SENT_NO_SERVICE,isRetry);
                }
                break;

            case SmsManager.RESULT_ERROR_NULL_PDU:

                debugLOG("Broadcast Receiver : SMS_SENT - Error: Null PDU . " + E_SENT_NULL_PDU);

                if (mSmsErrorListener != null) {
                    mSmsErrorListener.OnSMSSentFailed(number, "No PDU Provided", E_SENT_NULL_PDU,isRetry);
                }
                break;

            case SmsManager.RESULT_ERROR_RADIO_OFF:

                debugLOG("Broadcast Receiver : SMS_SENT - Error: Radio Off . " + E_SENT_RADIO_OFF);

                if (mSmsErrorListener != null) {
                    mSmsErrorListener.OnSMSSentFailed(number, "Radio is OFF", E_SENT_RADIO_OFF,isRetry);
                }
                break;

            default:
                debugLOG("Broadcast Receiver : SMS_SENT - Error: Unknown Error + "+errorCode + " . "+ E_SENT_STATUS_UNKNOWN);

                if(mSmsErrorListener!=null){
                    mSmsErrorListener.OnSMSSentFailed(number,"Unknown Internal Error while sending SMS - "+errorCode, E_SENT_STATUS_UNKNOWN,isRetry);
                }
        }

        if (mSmsHandler != null) {
            mSmsHandler.handle(number, message, smsRetryPolicy);
        }

    }

    private void handleDeliveryError(int errorCode, String number, String message, SMSRetryPolicy smsRetryPolicy){

        boolean isRetry = (smsRetryPolicy.getRetryCount()>0);

        switch (errorCode){
            case Activity.RESULT_CANCELED:

                debugLOG("Broadcast Receiver : SMS_DELIVERED - Error: Radio Off . "+E_DELIVERY_FAILED);

                if(mSmsErrorListener!=null){
                    mSmsErrorListener.OnSMSDeliverFailed(number,"Delivery Failed",E_DELIVERY_FAILED,isRetry);
                }
                break;

            default:

                debugLOG("Broadcast Receiver : SMS_DELIVERED - Error: Unknown Error + "+errorCode + " . "+ E_DELIVERY_STATUS_UNKNOWN);

                if(mSmsErrorListener!=null){
                    mSmsErrorListener.OnSMSDeliverFailed(number,"Unknown Internal Error while SMS Delivery - "+errorCode, E_DELIVERY_STATUS_UNKNOWN,isRetry);
                }
        }
        // ToDo; Decide whether the sms retry has to be handled for delivery
        // Delivery Failed Retry Policy Need to considered

        if (mSmsHandler != null) {
            mSmsHandler.handle(number, message, smsRetryPolicy);
        }

    }


    public void unRegister() {
        try {
            debugLOG("Unregister Receiver");
            context.unregisterReceiver(msgStatusReceiver);
        } catch (Exception e){
            // Just in case where Receiver is already unregistered.
            // No need to handle it.
        }
    }


    public void onPause(){
        debugLOG("OnPause");
        this.unRegister();

        if(mSmsHandler!=null){
            mSmsHandler.remove();
        }
    }

    /**
     *
     * @param responseListener : Interface for Success Responses
     */
    public SMSManager setResponseListener(SMSResponseListener responseListener){
        debugLOG("Set Response Listener: "+responseListener);
        this.mSmsResponseListener = responseListener;
        return this;
    }

    /**
     *
     * @param errorListener : Interface for Error Responses
     */

    public SMSManager setErrorListener(SMSErrorListener errorListener){
        debugLOG("Set Error Listener: "+errorListener);
        this.mSmsErrorListener = errorListener;
        return this;
    }

    /**
     *
     * @param status : True - Enable Debug; False - Disable Debug
     */
    public SMSManager setDebugEnabled(boolean status){
        debugLOG("Enable Debug: "+status);
        this.isDebug = status;
        return this;
    }


    /**
     *
     * @param message : debug message
     */
    private void debugLOG(String message){
        if(isDebug){
            Log.e(LOG_TITLE,""+message);
        }
    }
}
