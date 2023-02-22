package com.hanger_box.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class LibraryModel implements Serializable {

    String id = "";
    String image = "";
    String userId = "";
    String hostId = "";
    UserModel user;
    ItemModel item;
    ArrayList tags;
    String codiImage1 = "";
    String codiComment1 = "";
    String codiImage2 = "";
    String codiComment2 = "";
    int showNum = 0;

    HashMap map;

    public LibraryModel(JSONObject userInfo) {
        HashMap userMap = new HashMap();
        try {
            userMap.put("id", userInfo.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("image", userInfo.getString("image"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("user_id", userInfo.getString("user_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("host_id", userInfo.getString("host_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject userObj = userInfo.getJSONObject("user");
            userMap.put("user", new UserModel(userObj));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject itemObj = userInfo.getJSONObject("item");
            userMap.put("item", new ItemModel(itemObj));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            ArrayList tags = new ArrayList();
            JSONArray tagObjs = userInfo.getJSONArray("tags");
            if (tagObjs != null) {
                for (int i=0; i<tagObjs.length(); i++) {
                    try {
                        JSONObject object = (JSONObject) tagObjs.get(i);
                        tags.add(new Tag(object));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            userMap.put("tags", tags);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("codi_image1", userInfo.getString("codi_image1"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("codi_comment1", userInfo.getString("codi_comment1"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("codi_image2", userInfo.getString("codi_image2"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("codi_comment2", userInfo.getString("codi_comment2"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("show_num", userInfo.getInt("show_num"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setMap(userMap);
    }

    public LibraryModel(HashMap map) {
        setMap(map);
    }

    public HashMap getMap() {
        return map;
    }
    public void setMap(HashMap map) {
        this.map = map;
        this.id = (String) map.get("id");
        this.image = (String) map.get("image");
        this.userId = (String) map.get("user_id");
        this.hostId = (String) map.get("host_id");
        this.user = (UserModel) map.get("user");
        this.item = (ItemModel) map.get("item");
        this.tags = (ArrayList) map.get("tags");
        this.codiImage1 = (String) map.get("codi_image1");
        this.codiComment1 = (String) map.get("codi_comment1");
        this.codiImage2 = (String) map.get("codi_image2");
        this.codiComment2 = (String) map.get("codi_comment2");
        this.showNum = (int) map.get("show_num");
    }

    public void setId(String val) {
        this.id = val;
    }
    public String getId() {
        return id;
    }

    public void setImage(String val) {
        this.image = val;
    }
    public String getImage() {
        return image;
    }

    public void setUserId(String val) {
        this.userId = val;
    }
    public String getUserId() {
        return userId;
    }

    public void setHostId(String val) {
        this.hostId = val;
    }
    public String getHostId() {
        return hostId;
    }

    public void setUser(UserModel val) {
        this.user = val;
    }
    public UserModel getUser() {
        return user;
    }

    public void setItem(ItemModel val) {
        this.item = val;
    }
    public ItemModel getItem() {
        return item;
    }

    public void setTags(ArrayList val) {
        this.tags = val;
    }
    public ArrayList getTags() {
        return tags;
    }

    public void setCodiImage1(String val) {
        this.codiImage1 = val;
    }
    public String getCodiImage1() {
        return codiImage1;
    }

    public void setCodiComment1(String val) {
        this.codiComment1 = val;
    }
    public String getCodiComment1() {
        return codiComment1.equals("null") ? "" : codiComment1;
    }

    public void setCodiImage2(String val) {
        this.codiImage2 = val;
    }
    public String getCodiImage2() {
        return codiImage2;
    }

    public void setCodiComment2(String val) {
        this.codiComment2 = val;
    }
    public String getCodiComment2() {
        return codiComment2.equals("null") ? "" : codiComment2;
    }

    public void setShowNum(int val) {
        this.showNum = val;
    }
    public int getShowNum() {
        return showNum;
    }
}

