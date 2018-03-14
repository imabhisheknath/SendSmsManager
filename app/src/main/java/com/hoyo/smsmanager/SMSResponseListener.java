package com.hoyo.smsmanager;

/**
 * Created by Praba on 3/14/2018.
 */

public interface SMSResponseListener {
    void OnSMSSentSuccess(String number);
    void OnSMSDeliverySuccess(String number);
}
