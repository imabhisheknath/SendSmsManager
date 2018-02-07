# SendSmsManager
--------------

## send sms to any number and get back the status associate to the number.
---------------------------------------------------------------
How to Use
----------
//add permission in manifest

``` xml
 <uses-permission android:name="android.permission.SEND_SMS" />
```

```java

//add to module app
 compile 'com.github.imabhisheknath:SendSmsManager:v1.0-beta'
 
 //initialize the SmsManager
 
  SMSManager smsManager = new SMSManager(getApplicationContext()) {
            @Override
            public void onsendResponse(boolean status, ContentValues contentValues) {


                //status of sending message response along with the number and message
                //use can refer KEY class from main module 

                Log.e("sentmsg", status + "");
                Log.e("sentmsg", contentValues.get(Key.USER_NUMBER) + "\t" + contentValues.get(Key.RESPONSE_MSG));
            }

            @Override
            public void onDeliverResponse(boolean status, ContentValues contentValues) {
                
                  //status of deliver message response along with the number and message
                  //use can refer KEY class from main module 
                
                Log.e("recv", status + "");
                Log.e("recv", contentValues.get(Key.USER_NUMBER) + "\t" + contentValues.get(Key.RESPONSE_MSG));
            }
        };
        
        
        //start sending message by
         smsManager.sendMessage("number", "message");
         
         //stop message service by
         smsManager.unRegister();


```
