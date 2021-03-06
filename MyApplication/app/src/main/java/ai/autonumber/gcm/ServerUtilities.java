/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.autonumber.gcm;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();
        public static final String SERVER_URL = "http://autonumber.herokuapp.com";
//    public static final String SERVER_URL = "http://192.168.1.5:8080";
    private static final String TAG = "ServerUtilities";

    /**
     * Register this account/device pair within the server.
     */
    public static void register(final String regId, String userName) {
        Log.i(TAG, "registering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL + "/register";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("user", userName);
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
                post(serverUrl, params);
                return;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
    }

    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final String regId) {
        Log.i(TAG, "unregistering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(serverUrl, params);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            Log.e(TAG, "Failed to unregister");

        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params   request parameters.
     * @throws java.io.IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200)
                throw new IOException("Post failed with error code " + status);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static void chatMessage(String message, String regid) throws IOException {

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("msg", Base64.encodeToString(message.getBytes(Charset.forName("utf-8")), Base64.DEFAULT));
        params.put("regId", regid);
        post(SERVER_URL + "/chat", params);
    }

    public static void restoreChatMessages(String regid, int lastMessageId) throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("lastMsgId", "" + lastMessageId);
        params.put("regId", regid);
        post(SERVER_URL + "/messages", params);
    }

    public static void restoreLastCarResult(String regid) throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("regId", regid);
        post(SERVER_URL + "/lastimage", params);
    }

    public static void getCurrentUser(String regid) throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("regId", regid);
        post(SERVER_URL + "/current_user", params);
    }

    public static void changeCurrentUserName(String userId, String humanName) throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("userId", userId);
        params.put("humanName", Base64.encodeToString(humanName.getBytes(Charset.forName("utf-8")), Base64.DEFAULT));
        post(SERVER_URL + "/change_user_name", params);
    }

    public static void restoreAllPhotos(String regId) throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        post(SERVER_URL + "/imagesids", params);
    }
}
