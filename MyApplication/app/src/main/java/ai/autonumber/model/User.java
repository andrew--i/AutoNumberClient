package ai.autonumber.model;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


public class User implements Serializable {
  private String id;
  private String regId;
  private String deviceName;
  private String humanName;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRegId() {
    return regId;
  }

  public void setRegId(String regId) {
    this.regId = regId;
  }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getHumanName() {
        return humanName;
    }

    public void setHumanName(String humanName) {
        this.humanName = humanName;
    }

    public static User fromJson(String text) {
        try {
            JSONObject jsonObject = new JSONObject(new String(Base64.decode(text, Base64.DEFAULT)));
            User user = new User();
            user.id = jsonObject.getString("id");
            user.deviceName = jsonObject.getString("name");
            user.humanName = new String(Base64.decode(jsonObject.getString("readableName"), Base64.DEFAULT));
            user.regId = jsonObject.getString("regId");
            return user;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getViewName() {
        return humanName == null || humanName.isEmpty() ? deviceName : humanName;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User ? ((User) o).getId().equals(getId()) : super.equals(o);
    }
}
