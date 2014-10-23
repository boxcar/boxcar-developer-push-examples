package com.zeropush.sdk;

/**
 * Created by Stefan Natchev on 10/17/14.
 */


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.json.*;
import com.loopj.android.http.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;


/*
interface ZeroPushDelegate {
    public void registrationDidFail(Error error);
    public void subscribeDidFail(Error error);
    public void unsubscribeDidFail(Error error);
    public void setBadgeDidFail(Error error);
}
*/

public class ZeroPush {

//    public static final String ZeroPushAPIHost = "https://api.zeropush.com";
    public static final String ZeroPushAPIHost = "http://localhost:3000/api";
    public static final String Version = "0.0.1";
    static final String TAG = "ZeroPush-GCM-SDK";

    static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String UserAgent = TAG + "/" + Version;
    public static final AsyncHttpClient httpClient = new AsyncHttpClient();

    private static ZeroPush sharedInstance;

    GoogleCloudMessaging gcm;

    public static ZeroPush getInstance() {
        return sharedInstance;
    }

    private String apiKey;
    private String senderId;
    private String deviceToken;
    private Activity delegate;

    public ZeroPush(String apiKey, String senderId, Activity delegate){
        this.apiKey = apiKey;
        this.senderId = senderId;
        this.delegate = delegate;
        this.httpClient.setUserAgent(UserAgent);
        sharedInstance = this;
    }

    public void registerForRemoteNotifications() {
        if (!isGooglePlayServicesAvailable()){
            Log.e(TAG, "No valid Google Play Services APK found.");
            return;
        }

        gcm = GoogleCloudMessaging.getInstance(delegate.getApplicationContext());
        deviceToken = getDeviceToken();
        if(deviceToken.isEmpty()){
            doRegistrationInBackground();
        }
    }

    private void doRegistrationInBackground() {
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                String message = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(delegate.getApplicationContext());
                    }
                    deviceToken = gcm.register(senderId);
                    message = deviceToken;
                    setDeviceToken(deviceToken);
                    registerDeviceToken(deviceToken);
                } catch (IOException ex) {
                    message = ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                return message;
            }
        }.execute(null, null, null);
    }

    /**
     * Get the stored device token either from the instance variable or check in the stored preferences
     *
     * @return String the device token
     */
    public String getDeviceToken(){
        //is it in the instance variable?
        if (deviceToken != null && !deviceToken.isEmpty()) {
            return deviceToken;
        }

        //check is the app settings
        SharedPreferences preferences = delegate.getSharedPreferences(TAG, delegate.getApplicationContext().MODE_PRIVATE);
        deviceToken = preferences.getString(getDeviceTokenKey(), "");

        return deviceToken;
    }

    /**
     * Set the device token. Also stores in the application shared preferences.
     *
     * @param deviceToken
     */
    private void setDeviceToken(String deviceToken) {
        if(deviceToken != null) {
            this.deviceToken = deviceToken;
            SharedPreferences preferences = delegate.getSharedPreferences(TAG, delegate.getApplicationContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getDeviceTokenKey(), deviceToken);
            editor.commit();
        }
    }

    /**
     * Returns the key used to look up SharedPreferences for this module.
     *
     * @return The key used to look up saved preferences
     */
    private String getDeviceTokenKey() {
        Context context = delegate.getApplicationContext();
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return String.format("com.zeropush.api.deviceToken:%s", packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public void registerDeviceToken(String deviceToken) {
        registerDeviceTokenToChannel(deviceToken, null);
    }

    /**
     * Registers a device token with ZeroPush. If a channel is passed, then to subscribes the device token to that channel.
     *
     * @param deviceToken - Device token returned by the
     * @param channel - Optional channel to subscribe the device token
     */
    public void registerDeviceTokenToChannel(String deviceToken, String channel) {
        RequestParams params = new RequestParams();
        params.put("device_token", this.deviceToken);
        params.put("auth_token", this.apiKey);
        if(channel != null) {
            params.put("channel", channel);
        }

        httpClient.post(String.format("%s/register", ZeroPushAPIHost), params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //if delegate, call the error method, otherwise log it.
                Log.e(TAG, errorResponse.toString());
            }
        });
    }

    /**
     * Subscribe the device to a channel.
     *
     * @param channel
     */
    public void subscribeToChannel(String channel) {
        RequestParams params = new RequestParams("auth_token", this.apiKey, "device_token", this.deviceToken, "channel", channel);
        httpClient.post(
                String.format("%s/subscribe", ZeroPushAPIHost),
                params,
                new JsonHttpResponseHandler() {
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, errorResponse.toString());
                    }
                }
        );
    }

    /**
     * Unsubscribe the device from a channel
     *
     * @param channel
     */
    public void unsubscribeFromChannel(String channel) {
        RequestParams params = new RequestParams("auth_token", this.apiKey, "device_token", this.deviceToken, "channel", channel);
        httpClient.post(
                String.format("%s/unsubscribe", ZeroPushAPIHost),
                params,
                new JsonHttpResponseHandler() {
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, errorResponse.toString());
                    }
                }
        );
    }

    /**
     * Unsubscribe the device from all channels.
     *
     */
    public void unsubscribeFromAllChannels() {
        String url = String.format("%s/devices/%s", ZeroPushAPIHost, this.deviceToken);
        RequestParams params = new RequestParams("auth_token", this.apiKey, "channel_list", "");
        httpClient.put(
                url,
                params,
                new JsonHttpResponseHandler() {
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, errorResponse.toString());
                    }
                }
        );
    }

    public void getChannels() { }

    /**
     * Replace the current channel subscriptions with the provided list.
     *
     * @param channels
     */
    public void setChannels(List<String> channels) {
        String url = String.format("%s/devices/%s", ZeroPushAPIHost, this.deviceToken);
        RequestParams params = new RequestParams("auth_token", this.apiKey, "channel_list", join(channels.iterator(), ","));

        httpClient.put(
                url,
                params,
                new JsonHttpResponseHandler() {
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, errorResponse.toString());
                    }
                }
        );
    }

    private boolean isGooglePlayServicesAvailable() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(delegate);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, delegate, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                delegate.finish();
            }
            return false;
        }
        return true;
    }

    //Helper Method:
    private static String join(final Iterator<String> iterator, final String separator) {
        // handle null, zero and one elements before building a buffer
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        final String first = iterator.next();
        if (!iterator.hasNext()) {
            final String result = first;
            return result;
        }

        // two or more elements
        final StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            final Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }
}
