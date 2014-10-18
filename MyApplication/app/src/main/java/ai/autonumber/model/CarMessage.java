package ai.autonumber.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


public class CarMessage implements Serializable {
    private String id;
    private User user;
    private String time;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public static CarMessage fromJson(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            CarMessage carMessage = new CarMessage();
            carMessage.id = jsonObject.getString("id");
            carMessage.user = User.fromJson(jsonObject.getString("user"));
            carMessage.setTime(jsonObject.getString("time"));
            return carMessage;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
