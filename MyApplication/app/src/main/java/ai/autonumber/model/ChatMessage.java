package ai.autonumber.model;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

/**
 * Created by Andrew on 27.08.2014.
 */
public class ChatMessage implements Serializable {
    private String userId;
    private String text;
    private String messageId;
    private String time;
    public boolean left;
    private BigInteger messageIdAsBitInt;
    private String userName;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public static ChatMessage fromJson(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessageId(jsonObject.getString("messageId"));
            chatMessage.messageIdAsBitInt = new BigInteger(chatMessage.messageId);
            chatMessage.userName = jsonObject.getString("userName");

            try {
                byte[] decode = Base64.decode(jsonObject.getString("text").getBytes(), Base64.DEFAULT);
                chatMessage.setText(new String(decode));
            } catch (Exception e) {
                chatMessage.setText(jsonObject.getString("text"));
            }


            chatMessage.setUserId(jsonObject.getString("userId"));
            chatMessage.setTime(jsonObject.getString("time"));
            return chatMessage;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BigInteger getMessageIdAsBitInt() {
        return messageIdAsBitInt;
    }
}
