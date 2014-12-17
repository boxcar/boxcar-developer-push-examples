package com.zeropush.zeropush_gcm_demo;

import android.annotation.TargetApi;
import android.os.StrictMode;
import android.os.Build;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.zeropush.sdk.ZeroPush;
import com.zeropush.sdk.ZeroPushResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


public class Notifications extends Activity {

    private ZeroPush zeroPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        zeroPush = new ZeroPush("zeropush-app-token", "gcm-project-number", this);
        zeroPush.verifyCredentials(new ZeroPushResponseHandler(){
            @Override
            public void handle(JSONObject response, int statusCode, Error error) {
                if(error != null) {
                    Log.e("DemoApp", error.getMessage());
                    return;
                }
                Log.d("DemoApp", response.toString());
            }
        });

        zeroPush.registerForRemoteNotifications();
        activateStrictMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //zeroPush.registerForRemoteNotifications();
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
