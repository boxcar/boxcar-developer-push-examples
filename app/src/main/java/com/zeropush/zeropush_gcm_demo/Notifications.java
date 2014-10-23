package com.zeropush.zeropush_gcm_demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.zeropush.sdk.ZeroPush;


public class Notifications extends Activity {

    private ZeroPush zeroPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        zeroPush = new ZeroPush("zeropush-app-token", "gcm-project-number", this);
        zeroPush.registerForRemoteNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        zeroPush.registerForRemoteNotifications();
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
}
