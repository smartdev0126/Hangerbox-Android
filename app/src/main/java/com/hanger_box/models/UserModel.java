package com.hanger_box.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

public class UserModel implements Serializable {

    String id = "";
    String name = "";
    String countryName = "";
    String email = "";
    String profile = "";
    String avatar = "";
    String type = "";

    HashMap map;

    public UserModel(JSONObject userInfo) {
        HashMap userMap = new HashMap();
        try {
            userMap.put("id", userInfo.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("email", userInfo.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("name", userInfo.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("country_name", userInfo.getString("country_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("profile", userInfo.getString("profile"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("avatar", userInfo.getString("avatar"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("type", userInfo.getString("type"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setMap(userMap);
    }

    public UserModel(HashMap map) {
        setMap(map);
    }

    public HashMap getMap() {
        return map;
    }
    public void setMap(HashMap map) {
        this.map = map;
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.countryName = (String) map.get("country_name");
        this.email = (String) map.get("email");
        this.profile = (String) map.get("profile");
        this.avatar = (String) map.get("avatar");
        this.type = (String) map.get("type");
    }

    public void setId(String val) {
        this.id = val;
    }
    public String getId() {
        return id;
    }

    public void setName(String val) {
        this.name = val;
    }
    public String getName() {
        return name.equals("null") ? "" : name;
    }

    public void setCountryName(String val) {
        this.countryName = val;
    }
    public String getCountryName() {
        return countryName.equals("null") ? "" : countryName;
    }

    public void setEmail(String val) {
        this.email = val;
    }
    public String getEmail() {
        return email;
    }

    public void setProfile(String val) {
        this.profile = val;
    }
    public String getProfile() {
        return profile.equals("null") ? "" : profile;
    }

    public void setAvatar(String val) {
        this.avatar = val;
    }
    public String getAvatar() {
        if(avatar == null) avatar = new String("");
        return avatar;
    }

    public void setType(String val) {
        this.type = val;
    }
    public String getType() {
        return type.equals("null") ? "" : type;
    }
}

