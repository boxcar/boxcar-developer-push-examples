package com.zeropush.zeropush_gcm_demo;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@link IntentService} subclass helper for handling unregister requests
 * to ZeroPush API and registrations to Boxcar Push Service.
 * We use an {@link IntentService} to reduce the likelihood of this
 * process being killed in the middle of the migration. Especially when
 * this migration is done upon push reception.
 *
 * @author jpcarlino@process-one.net
 */
public class MigratorIntentService extends IntentService {


    static final Integer[] PREVIOUS_APP_VERSION_CODES = new Integer[]{21, 20, 1};
    static final String ZEROPUSH_APP_TOKEN = "Your ZeroPush Application Token";
    static final String ZEROPUSH_SERVER_TOKEN = "Your ZeroPush Server Token";

    static final String ACTION_UNREGISTER_ZEROPUSH = "com.zeropush.zeropush_gcm_demo.action.UNREGISTER_ZERO_PUSH";
    static final String EXTRA_TOKEN = "com.zeropush.zeropush_gcm_demo.extra.TOKEN";
    static final String TAG = "MigratorIntentService";

    private interface SimpleCallback {
        void success();
        void failed(Throwable t);
    }

    /**
     * Tries to find the token that is registered on ZeroPush service,
     * unregisters from this platform using it's REST API and initiates
     * a new registration using Boxcar Android SDK.
     *
     * @see IntentService
     */
    public static void startActionMigrate(Context context)
            throws Exception {
        String token = findZeroPushToken(context,
                Arrays.asList(PREVIOUS_APP_VERSION_CODES));
        if (token == null) {
            throw new Exception("Token not found");
        }
        Log.v(TAG, "Found token: " + token);

        Intent intent = new Intent(context, MigratorIntentService.class);
        intent.setAction(ACTION_UNREGISTER_ZEROPUSH);
        intent.putExtra(EXTRA_TOKEN, token);
        context.startService(intent);
    }

    /**
     * Returns true if this application was successfully unregistered from ZeroPush
     * using its REST API.
     * @param context
     * @return
     */
    public static boolean appUnregisteredFromZeroPush(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME,
                context.getApplicationContext().MODE_PRIVATE);
        return preferences.getBoolean(SHARED_PREFS_UNREG_KEY, false);
    }

    public MigratorIntentService() {
        super("MigratorIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UNREGISTER_ZEROPUSH.equals(action)) {
                final String token = intent.getStringExtra(EXTRA_TOKEN);
                handleActionUnregisterZeroPush(token);
            }
        }
    }

    private static RetryPolicy retryPolicy;
    private static RequestQueue requestQueue;

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUnregisterZeroPush(String token) {
        try {
            unregister(this, ZEROPUSH_SERVER_TOKEN, token, new SimpleCallback() {
                @Override
                public void success() {
                    setUnregisteredFromZeroPush(MigratorIntentService.this);
                    Log.v(TAG, "Device unregistered from ZeroPush, now registering on Boxcar...");
                    DemoApplication.registerBoxcar(MigratorIntentService.this);
                }

                @Override
                public void failed(Throwable t) {
                    Log.e(TAG, "Error unregistering device from ZeroPush", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
        }
    }


    /**
     * The default socket timeout in milliseconds
     */
    private static final int DEFAULT_TIMEOUT_MS = 5000;
    /**
     * The default number of retries
     */
    private static final int DEFAULT_MAX_RETRIES = 3;
    /**
     * The default backoff multiplier
     */
    private static final float DEFAULT_BACKOFF_MULT = 1f;

    /**
     * Makes a synchronous unregister request to ZeroPush API using this
     * service thread
     * @param context
     * @param authToken
     * @param token
     * @param localCallback
     * @throws Exception
     */
    private static void unregister(Context context, String authToken, String token,
                                   final SimpleCallback localCallback) throws Exception {

        if (requestQueue == null) {
            requestQueue = newVolleyRequestQueue(context);
            retryPolicy = new DefaultRetryPolicy(
                    DEFAULT_TIMEOUT_MS,
                    DEFAULT_MAX_RETRIES,
                    DEFAULT_BACKOFF_MULT);
        }

        List<NameValuePair> loginParams=new ArrayList<NameValuePair>();
        loginParams.add(new BasicNameValuePair("auth_token",authToken));
        loginParams.add(new BasicNameValuePair("device_token",token));

        URI uri= URIUtils.createURI("https", "api.zeropush.com", -1, "/unregister",
                URLEncodedUtils.format(loginParams, "UTF-8"), null);

        RequestFuture<String> futureListener = RequestFuture.newFuture();

        StringRequest request = new StringRequest(Request.Method.DELETE, uri.toString(),
                futureListener, futureListener){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accept", "application/json");
                return params;
            }
        };

        Log.v(TAG, "Deleting device from ZeroPush Service: " + uri.toString());

        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);

        try {
            futureListener.get(10, TimeUnit.SECONDS);
            localCallback.success();
        } catch (InterruptedException e) {
            Log.e(TAG, "ZeroPush API call interrupted.", e);
            localCallback.failed(e);
        } catch (ExecutionException e) {
            Log.e(TAG, "ZeroPush API call failed.", e);
            localCallback.failed(e);
        } catch (TimeoutException e) {
            Log.e(TAG, "ZeroPush API call timed out.", e);
            localCallback.failed(e);
        }

    }

    private static RequestQueue newVolleyRequestQueue(Context context) {
        File cacheDir = new File(context.getCacheDir(), "zeropushhelper");

        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }

        HttpStack stack;
        if (Build.VERSION.SDK_INT >= 9) {
            stack = new HurlStack();
        } else {
            // Prior to Gingerbread, HttpUrlConnection was unreliable.
            // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
            stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);

        queue.start();

        return queue;
    }

    private static final String SHARED_PREFS_NAME = "ZeroPush-Demo";
    private static final String SHARED_PREFS_UNREG_KEY = "com.zeropush.zeropush_gcm_demo.unregistered";

    private void setUnregisteredFromZeroPush(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME,
                context.getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SHARED_PREFS_UNREG_KEY, true);
        editor.apply();
    }

    private static String findZeroPushToken(Context context,
                                           List<Integer> knownVersions) {
        Collections.sort(knownVersions);
        ListIterator<Integer> li = knownVersions.listIterator(knownVersions.size());
        boolean found = false;
        String tokenKey;
        String token = null;
        while (li.hasPrevious() && !found) {
            tokenKey = getDeviceTokenKey(context, li.previous());
            if (tokenKey != null) {
                SharedPreferences preferences = context.getSharedPreferences("ZeroPush-GCM-SDK",
                        context.getApplicationContext().MODE_PRIVATE);
                token = preferences.getString(tokenKey, "");
                found = ((token != null) && (!token.isEmpty()));
            }
        }

        if (found) {
            return token;
        } else {
            return null;
        }
    }

    /**
     * Returns the key used to look up SharedPreferences for this module.
     *
     * @return The key used to look up saved preferences
     */
    private static String getDeviceTokenKey(Context context, int versionCode) {
        return String.format("com.zeropush.api.deviceToken:%s", versionCode);
    }

}
