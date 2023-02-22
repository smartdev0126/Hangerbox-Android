package com.hanger_box.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

public class ItemModel implements Serializable {

    String id = "";
    String image = "";
    String shopUrl = "";
    String affiliateUrl = "";
    String brand = "";
    String categoryId = "";
    String price = "";
    String currency = "";
    String comment = "";

    HashMap map;

    public ItemModel(JSONObject userInfo) {
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
            userMap.put("shop_url", userInfo.getString("shop_url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("affiliate_url", userInfo.getString("affiliate_url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("brand", userInfo.getString("brand"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("category_id", userInfo.getString("category_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("price", userInfo.getString("price"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("currency", userInfo.getString("currency"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userMap.put("comment", userInfo.getString("comment"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setMap(userMap);
    }

    public ItemModel(HashMap map) {
        setMap(map);
    }

    public HashMap getMap() {
        return map;
    }
    public void setMap(HashMap map) {
        this.map = map;
        this.id = (String) map.get("id");
        this.image = (String) map.get("image");
        this.shopUrl = (String) map.get("shop_url");
        this.affiliateUrl = (String) map.get("affiliate_url");
        this.brand = (String) map.get("brand");
        this.categoryId = (String) map.get("category_id");
        this.price = (String) map.get("price");
        this.currency = (String) map.get("currency");
        this.comment = (String) map.get("comment");
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

    public void setShopUrl(String val) {
        this.shopUrl = val;
    }
    public String getShopUrl() {
        return shopUrl.equals("null") ? "" : shopUrl;
    }

    public void setAffiliateUrl(String val) {
        this.affiliateUrl = val;
    }
    public String getAffiliateUrl() {
        return affiliateUrl.equals("null") ? "" : affiliateUrl;
    }

    public void setBrand(String val) {
        this.brand = val;
    }
    public String getBrand() {
        return brand.equals("null") ? "" : brand;
    }

    public void setCategoryId(String val) {
        this.categoryId = val;
    }
    public String getCategoryId() {
        return categoryId;
    }

    public void setPrice(String val) {
        this.price = val;
    }
    public String getPrice() {
        return price.equals("null") ? "" : price;
    }

    public void setCurrency(String val) {
        this.currency = val;
    }
    public String getCurrency() {
        return currency;
    }

    public void setComment(String val) {
        this.comment = val;
    }
    public String getComment() {
        return comment.equals("null") ? "" : comment;
    }
}
