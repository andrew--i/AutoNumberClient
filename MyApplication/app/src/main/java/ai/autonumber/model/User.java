package ai.autonumber.model;

import java.io.Serializable;

/**
 * Created by Andrew on 27.08.2014.
 */
public class User implements Serializable {
  private String id;
  private String regId;
  private String name;


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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
