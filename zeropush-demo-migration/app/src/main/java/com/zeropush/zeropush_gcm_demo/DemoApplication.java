package com.zeropush.zeropush_gcm_demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.boxcar.push.BXCException;
import io.boxcar.push.Boxcar;
import io.boxcar.push.registration.BXCPendingOperationException;

public class DemoApplication extends Application {

    private static final String TAG = "DemoApp";

    @Override
    public void onCreate() {
        super.onCreate();
        startBoxcar();
        Log.d(TAG, "Demo application started");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Boxcar.unregisterSubscriber(this);
    }

    private void startBoxcar() {
        Boxcar.init(this, new BoxcarDemoDelegate(this));
    }

    public static void registerBoxcar(Context context) {
        try {
            Log.v("DemoApplication", "Registering on Boxcar Universal Push Service");
            List<String> channels = new ArrayList<String>();
            channels.add("test-channel");
            Boxcar.register(context, channels, null);
        } catch (BXCPendingOperationException e) {
            Log.e("PushReceived", "There is a pending operation. We must wait to the callback.", e);
        } catch (BXCException e) {
            Log.e("PushReceived", "Error registering on Boxcar Universal Push Service", e);
        }
    }

}
