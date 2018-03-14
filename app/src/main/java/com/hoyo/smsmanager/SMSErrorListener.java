package com.hoyo.smsmanager;

/**
 * Created by Praba on 3/14/2018.
 */

public interface SMSErrorListener {
    void OnSMSSentFailed(String number, String errorMessage, String errorCode, boolean isRetry);
    void OnSMSDeliverFailed(String number, String errorMessage, String errorCode, boolean isRetry);
}
