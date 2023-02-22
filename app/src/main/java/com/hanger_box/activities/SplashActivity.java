package com.hanger_box.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.hanger_box.common.Common.cm;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
                    intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
            }
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        Common.currentActivity = this;
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            }
//        }, 2000);
        Common.addFavorite = true;
        Common.initMyItem = true;
        Common.initLibrary = true;
        getCountries();
    }

    private void getCountries() {
        try {
            JSONObject obj = new JSONObject(Common.cm.loadJSONFromAsset("country.json"));
            JSONArray m_jArry = obj.getJSONArray("countries");
            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> m_li;

            ArrayList temp = new ArrayList();
            ArrayList temp_en = new ArrayList();
            for (int i = 0; i < m_jArry.length(); i++) {
                try {
                    JSONObject item = m_jArry.getJSONObject(i);
                    String name_en = item.getString("name");
                    JSONObject trans = item.getJSONObject("translations");
                    String name_ja = trans.getString("ja");
                    if (!name_ja.equals("null")) temp.add(name_ja);
                    else temp.add(name_en);
                    temp_en.add(name_en);
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
            Common.countries = new CharSequence[temp.size()];
            Common.countries_en = new CharSequence[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                Common.countries[i] = temp.get(i).toString();
                Common.countries_en[i] = temp_en.get(i).toString();
            }
            getCategories();
        } catch (JSONException e) {
            getCategories();
            e.printStackTrace();
        }

    }

    private void getCategories() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.CATEGORY_ITEMS_URL;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                        gotoNext();
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject result = new JSONObject(mMessage);
                            try {
                                cm.showAlertDlg("", result.getString("message"), null, null);
                                gotoNext();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                JSONArray catObjs = result.getJSONArray("data");
                                if (catObjs != null) {
                                    Common.categories = new CharSequence[catObjs.length()];
                                    Common.categories_en = new CharSequence[catObjs.length()];
                                    for (int i=0; i<catObjs.length(); i++) {
                                        try {
                                            JSONObject object = (JSONObject) catObjs.get(i);
                                            Common.categories[i] = object.getString("name");
                                            Common.categories_en[i] = object.getString("slug");
                                        } catch (JSONException e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                    gotoNext();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void gotoNext() {
        String userInfo = LocalStorageManager.getObjectFromLocal("account");
        if (userInfo == null) {
            Common.setLocale();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }else {
            Common.me = new UserModel(cm.convertToHashMapFromString(userInfo));
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("back", "top");
            startActivity(intent);
        }
        finish();
    }

}