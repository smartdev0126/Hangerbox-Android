package com.hanger_box.common;

import android.content.SharedPreferences;

import com.hanger_box.R;

/**
 *
 * アプリ内のデータの保存を管理するクラス
 *
 */

public class LocalStorageManager {

    public static void saveObjectToLocal(String val, String key) {
        SharedPreferences sharedPreferences = Common.myApp.getSharedPreferences(String.valueOf(R.string.app_name), Common.myApp.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static String getObjectFromLocal(String key) {
        SharedPreferences sharedPreferences = Common.myApp.getSharedPreferences(String.valueOf(R.string.app_name), Common.myApp.MODE_PRIVATE);
        String syncState = sharedPreferences.getString(key, null);
        return syncState;
    }
}

