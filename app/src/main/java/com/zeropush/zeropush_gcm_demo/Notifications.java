package com.zeropush.zeropush_gcm_demo;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.StrictMode;
import android.os.Build;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.GooglePlayServicesUtil;

import io.boxcar.push.Boxcar;
import io.boxcar.push.eventbus.event.RegistrationFailedEvent;
import io.boxcar.push.eventbus.event.RegistrationSuccessEvent;
import io.boxcar.push.gateway.GooglePlayPushException;

public class Notifications extends Activity {

    public static final String TAG;

    static {
        TAG = "DemoApp";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (MigratorIntentService.appUnregisteredFromZeroPush(this)) {
            DemoApplication.registerBoxcar(this);
        } else {
            try {
                MigratorIntentService.startActionMigrate(this);
            } catch (Exception e) {
                Log.e(TAG, "Failed to migrate app", e);
            }
        }

        activateStrictMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Boxcar.unregisterSubscriber(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Boxcar.registerSubscriber(this);
    }

    /**
     * This is a callback method used by Boxcar SDK to notify when a
     * registration request finished successfully.
     * @param event the registration event, including current token and
     *              deprecated channels (if any)
     */
    /*
     * We mark this method as 'unused' although this is not true. However
     * runtime code inspection can't detect this is method is invoked
     * dynamically (reflection) by EventBus
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(RegistrationFailedEvent event) {
        try {
            throw event.getError();
        } catch (GooglePlayPushException e) {
			/*
			 * We got an error during registration. If this is a Google
			 * Android device, check if Google Play APK is installed.
			 */
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(e.getErrorCode(), this, 9000);
            if (errorDialog != null) {
                errorDialog.show();
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error trying to register into Boxcar Universal Push Service", t);
        }
    }

    /**
     * This is a callback method used by Boxcar SDK to notify when a
     * registration request failed.
     * @param event the failure event
     */
    /*
     * We mark this method as 'unused' although this is not true. However
     * runtime code inspection can't detect this is method is invoked
     * dynamically (reflection) by EventBus
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(RegistrationSuccessEvent event) {
        Log.d(TAG, "Registered on Boxcar Universal Push Service with token " + event.getToken());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(9)
    private void activateStrictMode() {
        // Set the activity to Strict mode so that we get LogCat warnings when code misbehaves on the main thread.
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        }
    }
}
