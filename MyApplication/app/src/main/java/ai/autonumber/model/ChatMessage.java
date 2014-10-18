package ai.autonumber.model;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;

import ai.autonumber.state.AppStateHolder;


public class ChatMessage implements Serializable {
    private String text;
    private String messageId;
    private String time;
    private BigInteger messageIdAsBitInt;
    private User user;

    public User getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getTime() {
        return time;
    }

    public boolean isLeft() {
        return  user != null && user.equals(AppStateHolder.currentUser);
    }

    public static ChatMessage fromJson(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.messageId = jsonObject.getString("messageId");
            chatMessage.messageIdAsBitInt = new BigInteger(chatMessage.messageId);
            chatMessage.user = User.fromJson(jsonObject.getString("user"));
            try {
                byte[] decode = Base64.decode(jsonObject.getString("text").getBytes(), Base64.DEFAULT);
                chatMessage.text = new String(decode);
            } catch (Exception e) {
                chatMessage.text = jsonObject.getString("text");
            }
            chatMessage.time = jsonObject.getString("time");
            return chatMessage;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BigInteger getMessageIdAsBitInt() {
        return messageIdAsBitInt;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
