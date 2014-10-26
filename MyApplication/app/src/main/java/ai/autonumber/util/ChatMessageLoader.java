package ai.autonumber.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.autonumber.adapter.LazyChatItemsLoadAdapter;
import ai.autonumber.cache.ChatMessagesCache;
import ai.autonumber.gcm.ServerUtilities;
import ai.autonumber.model.ChatMessage;
import ai.autonumber.model.User;

/**
 * Created by Andrew on 26.10.2014.
 */
public class ChatMessageLoader {

    private ExecutorService executorService;
    private Map<View, String> rowViews = Collections.synchronizedMap(new WeakHashMap<View, String>());

    private ChatMessagesCache chatMessagesCache;
    private LazyChatItemsLoadAdapter adapter;
    private Handler handler = new Handler();

    public ChatMessageLoader(ChatMessagesCache chatMessagesCache, LazyChatItemsLoadAdapter adapter) {
        this.chatMessagesCache = chatMessagesCache;
        this.adapter = adapter;
        executorService = Executors.newFixedThreadPool(5);
    }

    public void loadMessage(String messageId, View rowView) {
        rowViews.put(rowView, messageId);
        ChatMessage chatMessage = chatMessagesCache.getMessage(messageId);
        if (chatMessage != null) {
            adapter.updateRow(rowView, chatMessage);
        } else {
            addMessageToQueue(messageId, rowView);
        }
    }

    private void addMessageToQueue(String messageId, View rowView) {
        MessageToLoad messageToLoad = new MessageToLoad(messageId, rowView);
        executorService.submit(new MessageLoader(messageToLoad));
    }

    public void addMessage(ChatMessage message) {
        chatMessagesCache.addMessage(message);
    }

    public void updateMessages(User user) {
        chatMessagesCache.updateMessages(user);
    }

    private class MessageToLoad {
        public String messageId;
        public View rowView;

        public MessageToLoad(String messageId, View rowView) {
            this.messageId = messageId;
            this.rowView = rowView;
        }
    }

    private class MessageLoader implements Runnable {
        private MessageToLoad messageToLoad;

        public MessageLoader(MessageToLoad messageToLoad) {
            this.messageToLoad = messageToLoad;
        }

        @Override
        public void run() {
            //Check if image already downloaded
            if (isRowViewReused(messageToLoad))
                return;
            try {
                ChatMessage chatMessage = getChatMessage(messageToLoad.messageId);
                addMessage(chatMessage);
                if (isRowViewReused(messageToLoad))
                    return;
                ChatMessageDisplayer chatMessageDisplayer = new ChatMessageDisplayer(chatMessage, messageToLoad);
                handler.post(chatMessageDisplayer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private ChatMessage getChatMessage(String messageId) throws IOException {
            String url = ServerUtilities.SERVER_URL + "/message?id=" + messageId;
            URL messageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) messageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            StreamUtils.copyStream(is, os);
            String message = new String(Base64.decode(new String(os.toByteArray()), Base64.DEFAULT));
            return ChatMessage.fromJson(message);
        }

        private boolean isRowViewReused(MessageToLoad messageToLoad) {
            String messageId = rowViews.get(messageToLoad.rowView);
            return messageId == null || !(messageId.equals(messageToLoad.messageId));
        }

        private class ChatMessageDisplayer implements Runnable {
            private ChatMessage chatMessage;
            private MessageToLoad messageToLoad;

            public ChatMessageDisplayer(ChatMessage chatMessage, MessageToLoad messageToLoad) {
                this.chatMessage = chatMessage;
                this.messageToLoad = messageToLoad;
            }

            @Override
            public void run() {
                if (isRowViewReused(messageToLoad))
                    return;
                if (chatMessage != null) {
                    adapter.updateRow(messageToLoad.rowView, chatMessage);
                }
            }
        }
    }
}
