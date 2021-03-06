package ai.autonumber.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import ai.autonumber.activiti.AutoNumberChatActivity;
import ai.autonumber.R;
import ai.autonumber.state.AppStateHolder;

/**
 * Created by Andrew on 26.08.2014.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String CHAT_MESSAGE_TOKEN = "chat-message";
    public static final String NEW_CAR_MESSAGE_TOKEN = "new-car";
    public static final String LAST_CAR_MESSAGE_TOKEN = "last-car";
    public static final String CARS_MESSAGE_TOKEN = "cars";
    public static final String CURRENT_USER_MESSAGE_TOKEN = "current_user";
    public static final String USER_INFO_CHANGED_MESSAGE_TOKEN = "user_info_changed";

    @Override
    protected void onHandleIntent(final Intent intent) {
        final Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);


        // has effect of unparcelling Bundle
/*
 * Filter messages based on message type. Since it is likely that GCM
 * will be extended in the future with new message types, just ignore
 * any message types you're not interested in, or that you don't
 * recognize.
 */
        if (!extras.isEmpty()) if (GoogleCloudMessaging.
                MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            sendNotification("Send error: " + extras.toString());
        } else if (GoogleCloudMessaging.
                MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages on server: " +
                    extras.toString());
            // If it's a regular GCM message, do some work.
        } else if (GoogleCloudMessaging.
                MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            Intent activitiIntent = new Intent(GoogleCloudMessageActivity.INTENT_ACTION);
            //put whatever data you want to send, if any


            Object chatMessageObject = extras.get(CHAT_MESSAGE_TOKEN);
            if (chatMessageObject != null)
                activitiIntent.putExtra(CHAT_MESSAGE_TOKEN, chatMessageObject.toString());
            Object searchCarObject = extras.get(NEW_CAR_MESSAGE_TOKEN);
            if (searchCarObject != null)
                activitiIntent.putExtra(NEW_CAR_MESSAGE_TOKEN, searchCarObject.toString());
            searchCarObject = extras.get(LAST_CAR_MESSAGE_TOKEN);
            if (searchCarObject != null)
                activitiIntent.putExtra(LAST_CAR_MESSAGE_TOKEN, searchCarObject.toString());
            final Object currentUserObject = extras.get(CURRENT_USER_MESSAGE_TOKEN);
            if (currentUserObject != null)
                activitiIntent.putExtra(CURRENT_USER_MESSAGE_TOKEN, currentUserObject.toString());
            Object userInfoChangedUserObject = extras.get(USER_INFO_CHANGED_MESSAGE_TOKEN);
            if (userInfoChangedUserObject != null)
                activitiIntent.putExtra(USER_INFO_CHANGED_MESSAGE_TOKEN, userInfoChangedUserObject.toString());
            Object carsMessageObject = extras.get(CARS_MESSAGE_TOKEN);
            if (carsMessageObject != null)
                activitiIntent.putExtra(CARS_MESSAGE_TOKEN, carsMessageObject.toString());
            //send broadcast
            getApplicationContext().sendBroadcast(activitiIntent);

            // This loop represents the service doing some work.
            // Post notification of received message.
            if (!AppStateHolder.isMainActivityVisible())
                if (extras.containsKey(CHAT_MESSAGE_TOKEN) || extras.containsKey(NEW_CAR_MESSAGE_TOKEN))
                    sendNotification("Пришло сообщение");
            Log.i(TAG, "Received: " + extras.toString());
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        final NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AutoNumberChatActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_gcm)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
