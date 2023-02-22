package com.hanger_box.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

public class Category implements Serializable {

    String id = "";
    String tag = "";

    HashMap map;

    public Category(JSONObject userInfo) {
        HashMap userMap = new HashMap();
        try {
            userMap.put("id", userInfo.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("tag", userInfo.getString("tag"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setMap(userMap);
    }

    public Category(HashMap map) {
        setMap(map);
    }

    public HashMap getMap() {
        return map;
    }
    public void setMap(HashMap map) {
        this.map = map;
        this.id = (String) map.get("id");
        this.tag = (String) map.get("tag");
    }

    public void setId(String val) {
        this.id = val;
    }
    public String getId() {
        return id;
    }

    public void setTag(String val) {
        this.tag = val;
    }
    public String getTag() {
        return tag;
    }
}


