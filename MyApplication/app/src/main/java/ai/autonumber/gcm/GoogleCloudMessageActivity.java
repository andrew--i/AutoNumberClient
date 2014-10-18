package ai.autonumber.gcm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import ai.autonumber.controller.Controller;
import ai.autonumber.model.CarMessage;
import ai.autonumber.model.ChatMessage;
import ai.autonumber.model.User;
import ai.autonumber.state.AppStateHolder;


public abstract class GoogleCloudMessageActivity extends Activity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GoogleCloudMessage";
    private GoogleCloudMessaging gcm;
    public String regid;
    private String deviceUserName;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    protected Context context;
    private static final String SENDER_ID = "1015113832014";
    public static final String INTENT_ACTION = TAG + SENDER_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
        if (accounts.length == 0) {
            Toast.makeText(context, "Не удалось определить имя пользователя", Toast.LENGTH_SHORT).show();
            deviceUserName = "unknown_user";
        } else
            deviceUserName = accounts[0].name;
        // Check device for Play Services APK.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);
            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                Controller.runAsync(new Controller.Action() {
                    @Override
                    public void doAction() throws IOException {
                        ServerUtilities.getCurrentUser(regid);
                    }
                });
            }
        } else
            Log.i(TAG, "No valid Google Play Services APK found.");
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(getMainActivityClassName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {

        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                try {
                    regid = gcm.register(SENDER_ID);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                sendRegistrationIdToBackend();

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the regID - no need to register again.
                storeRegistrationId(context, regid);
                return null;
            }
        }).execute();
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        ServerUtilities.register(regid, deviceUserName);
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    protected abstract String getMainActivityClassName();

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        context.registerReceiver(mMessageReceiver, new IntentFilter(INTENT_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        context.unregisterReceiver(mMessageReceiver);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            String message = intent.getStringExtra(GcmIntentService.CHAT_MESSAGE_TOKEN);
            if (message != null) {
                ChatMessage chatMessage = ChatMessage.fromJson(message);
                handleChatMessage(chatMessage);
            }

            String newCarMessage = intent.getStringExtra(GcmIntentService.NEW_CAR_MESSAGE_TOKEN);
            if (newCarMessage != null) {
                CarMessage carMessage = CarMessage.fromJson(newCarMessage);
                if (carMessage != null)
                    handleNewCarMessage(carMessage);
            }
            newCarMessage = intent.getStringExtra(GcmIntentService.LAST_CAR_MESSAGE_TOKEN);
            if (newCarMessage != null) {
                CarMessage carMessage = CarMessage.fromJson(newCarMessage);
                if (carMessage != null)
                    handleLastCarMessage(carMessage);
            }

            String currentUserMessage = intent.getStringExtra(GcmIntentService.CURRENT_USER_MESSAGE_TOKEN);
            if (currentUserMessage != null) {
                User user = User.fromJson(currentUserMessage);
                if (user != null) {
                    AppStateHolder.currentUser = user;
                    handleChangeCurrentUser();
                }
            }

            String userInfoChangedMessage = intent.getStringExtra(GcmIntentService.USER_INFO_CHANGED_MESSAGE_TOKEN);
            if (userInfoChangedMessage != null) {
                User user = User.fromJson(userInfoChangedMessage);
                if (user != null) {
                    if (user.getRegId().equals(regid)) {
                        AppStateHolder.currentUser = user;
                        handleChangeCurrentUser();
                    }
                    handleChangeUserInfo(user);
                }
            }
        }
    };

    protected abstract void handleLastCarMessage(CarMessage carMessage);

    protected abstract void handleNewCarMessage(CarMessage carMessage);

    protected abstract void handleChatMessage(ChatMessage chatMessage);

    protected abstract void handleChangeCurrentUser();

    protected abstract void handleChangeUserInfo(User user);

}
