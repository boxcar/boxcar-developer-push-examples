package com.zeropush.zeropush_gcm_demo;

import android.content.Context;
import android.os.Bundle;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import io.boxcar.push.BXCConfig;
import io.boxcar.push.BoxcarAppDelegate;
import io.boxcar.push.ui.BaseUINotificationStrategy;
import io.boxcar.push.ui.MultipleUINotificationStrategy;
import io.boxcar.push.eventbus.event.NotificationReceivedEvent;
import io.boxcar.push.model.BXCNotification;

/**
 * This is the delegate class used by Boxcar SDK to get all configuration items
 * specific for your application.
 * It does also show how to handle a push from ZeroPush
 * @see #handleIncomingPush(Context, Bundle)
 *
 * @author jpcarlino@process-one.net
 */
public class BoxcarDemoDelegate extends BoxcarAppDelegate {

    static final String BOXCAR_CLIENT_ACCESS_KEY = "Your Boxcar Application Client Access Key";
    static final String BOXCAR_CLIENT_SECRET_KEY = "Your Boxcar Application Client Secret Key";
    static final String GCM_SENDER_ID = "Your GCM Sender Id";

    BXCConfig config;

    public BoxcarDemoDelegate(Context context) {
        // build notification strategy
        int icon = R.drawable.ic_launcher;
        String title = "Got it!";
        BaseUINotificationStrategy notificationStrategy =
                new MultipleUINotificationStrategy(icon, title,
                        Notifications.class, null);
        notificationStrategy.setVibrateOn(false);

        config = new BXCConfig(BOXCAR_CLIENT_ACCESS_KEY, BOXCAR_CLIENT_SECRET_KEY,
                notificationStrategy, GCM_SENDER_ID);
    }

    @Override
    public BXCConfig getConfig(Context context) {
        return config;
    }

    /**
     * Hook method provided to allow handling the raw push 
     * ({@link android.os.Bundle}) before any 
     * {@link io.boxcar.push.ui.BXCNotificationStrategy} instance.
     * By default it does nothing and flags to continue so the configured
     * notification strategy can handle it.
     * Note that if this method returns {@link NotificationChainFlag#STOP}
     * your artifacts will keep receiving events of type 
     * {@link NotificationReceivedEvent} (if these have registered for this
     * event type). However because the chain is cut earlier, the
     * {@link BXCNotification} instance contained in this event will be
     * <i>null</i>.
     * @param context current application context
     * @param payload the raw push as received from the push provider
     * @return a flag to control if further handling is desired via 
     * configured {@link io.boxcar.push.ui.BXCNotificationStrategy} 
     * ({@link NotificationChainFlag#CONTINUE) or if it should stop here
     * ({@link NotificationChainFlag#STOP}).
     */
    @Override
    protected NotificationChainFlag handleIncomingPush(final Context context,
                                                       Bundle payload) {
        if (isZeroPushContent(payload)) {
            prepareZeroPushNotification(context, payload);
            try {
                MigratorIntentService.startActionMigrate(context);
            } catch (Exception e) {
                Log.e("BoxcarDemoDelegate", "Unexpected error unregistering from ZeroPush", e);
            }
            return NotificationChainFlag.STOP;
        }
        return NotificationChainFlag.CONTINUE;
    }

    private boolean isZeroPushContent(Bundle payload) {
        // this is just an arbitrary example, you can check
        // any particularity of your payload to determine if it
        // comes from ZeroPush
        return (payload.getString("alert") != null);
    }

    private void prepareZeroPushNotification(Context context, Bundle payload) {
        Log.d("PushReceived", payload.toString());
        NotificationManager manager = (NotificationManager)context
            .getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
            new Intent(context, Notifications.class), 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle("Got it!")
                .setContentText(payload.toString())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        manager.notify(1, notification);
    }

}
