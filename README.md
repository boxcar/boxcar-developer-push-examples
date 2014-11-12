ZeroPush-Android-Demo
=====================

A demo project showing how to integrate with the ZeroPush API.

Background
---

ZeroPush uses GCM's CCS(XMPP) gateway for sending push notifications. The XMPP
protocol has a number of advantages. We maintain a persistent connection to GCM
so there is low overhead when making many requests.  This also allows us to
perform fast broadcasts to all of your registered devices and are not
limited to 10,000. We use the same efficient connection handling model we employ
when sending push notifications through the APNS gateway.

Prerequisites
---

Get a Project Number and API Key from Google Developers Console. Follow these
[instructions](https://zeropush.com/documentation/getting_started-gcm).

Setup
---

1. Create an Android app on the ZeroPush site and configure it with the API Key and Project Number.

1. Clone this repo so you can include the `zeropush-sdk` module

1. Include the `zeropush-sdk` module in your project. In Android Studio:
  *File -> Import Module...*. When you reference the module, Android Studio should
  offer to add the dependency.

1. Call setup method in main activity.
  ```java
  private ZeroPush zeroPush;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_notifications);
      zeroPush = new ZeroPush("zeropush-app-token", "gcm-project-number", this);
      zeroPush.registerForRemoteNotifications();
  }
  ```

  You will probably want to register again in the `onResume` method as
  well. This ensures that if the user returns to the running app through some other
  means, such as through the back button, the registration is still performed.

1. Add a receiver to handle the received push notifications and subclass ZeroPushBroadcastReceiver:
  ```java
  public class IntentReceiver extends ZeroPushBroadcastReceiver {
    @Override
    public void onPushReceived(Context context, Intent intent, Bundle extras) {
        Log.d("PushReceived", extras.toString());
    }
  }
  ```


1. Add permissions to `AndroidManifest.xml`. Now that we have a class to act
   as the receiver, we need to configure it inside of `<application>`. Add the following declarations:
  ```xml
  <application>
    ...
    <meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />
    <receiver
        android:name=".IntentReceiver"
        android:permission="com.google.android.c2dm.permission.SEND" >
        <intent-filter>
            <action android:name="com.google.android.c2dm.intent.RECEIVE" />
            <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
            <category android:name="com.zeropush.zeropush_gcm_demo" />
        </intent-filter>
    </receiver>
    
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />

    <permission android:name="com.zeropush.zeropush_gcm_demo.permission.C2D_MESSAGE"
        android:protectionLevel="signature" />
    <uses-permission android:name="com.zeropush.zeropush_gcm_demo.permission.C2D_MESSAGE" />
  </application>
  ```
  Remember to replace `com.zeropush.zeropush_gcm_demo` with the package of your app.

Handle a Push Notification
---

1. Create a simple notification to display
  ```java
  public class IntentReceiver extends ZeroPushBroadcastReceiver {
      @Override
      public void onPushReceived(Context context, Intent intent, Bundle extras) {

          NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
          PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, YourMainActivity.class), 0);

          Notification notification = new Notification.Builder(context)
                  .setContentTitle("Got it!")
                  .setContentText(extras.toString())
                  .setSmallIcon(R.drawable.ic_launcher)
                  .setContentIntent(pendingIntent)
                  .build();

          manager.notify(1, notification);
      }
  }

  ```


Send a broadcast
---

We support all of the CCS parameters documented here: https://developer.android.com/google/gcm/server.html#params

```shell
curl https://api.zeropush.com/broadcast \
  -d auth_token=auth_token \
  -d data[alert]=hello \
  -d data[title]=Test \
  -d collapse_key=friend_request \
  -d delay_while_idle=false \
  -d time_to_live=40320 \
```

