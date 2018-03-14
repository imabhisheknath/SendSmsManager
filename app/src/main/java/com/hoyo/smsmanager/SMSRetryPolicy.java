package com.hoyo.smsmanager;

/**
 * Created by Praba on 3/14/2018.
 */

public class SMSRetryPolicy {


    public static int DEFAULT_RETRY_COUNT =  0;
    public static int DEFAULT_RETRY_TIME = 10 * 1000; // 10 Seconds

    private int retryCount;
    private int retryTimeMilliSec;


    public SMSRetryPolicy() {
        this(DEFAULT_RETRY_COUNT,DEFAULT_RETRY_TIME);
    }

    public SMSRetryPolicy(int retryCount, int retryTimeMilliSec) {
        this.retryCount = retryCount;
        this.retryTimeMilliSec = retryTimeMilliSec;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryTimeMilliSec() {
        return retryTimeMilliSec;
    }

    public void setRetryTimeMilliSec(int retryTimeMilliSec) {
        this.retryTimeMilliSec = retryTimeMilliSec;
    }

    public void deduceRetryCount(){
        retryCount -= 1;
    }

    public static SMSRetryPolicy getDefaultRetryPolicy(){
        return new SMSRetryPolicy(DEFAULT_RETRY_COUNT,DEFAULT_RETRY_TIME);
    }

}
