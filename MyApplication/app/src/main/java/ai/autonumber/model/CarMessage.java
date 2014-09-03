package ai.autonumber.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


public class CarMessage implements Serializable {
    private String id;
    private String userId;
    private Bitmap bitmap;
    private String time;
    private String userName;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static CarMessage fromJson(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            CarMessage carMessage = new CarMessage();
            carMessage.id = jsonObject.getString("id");
            carMessage.userName = jsonObject.getString("userName");

            try {
                byte[] decodedByte = Base64.decode(jsonObject.getString("result"), Base64.DEFAULT);
                carMessage.setBitmap(BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length));
            } catch (Exception e) {
                carMessage.setBitmap(null);
            }

            carMessage.setUserId(jsonObject.getString("userId"));
            carMessage.setTime(jsonObject.getString("time"));
            return carMessage;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
