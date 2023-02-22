package com.hanger_box.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.cm;

public class ChangePasswordActivity extends AppCompatActivity {

    private LinearLayout loadingLayout;
    private EditText currentPwdExt, newPwdExt, renewPwdExt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Common.currentActivity = this;

        loadingLayout = findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);

        currentPwdExt = findViewById(R.id.current_password_ext);
        newPwdExt = findViewById(R.id.new_password_ext);
        renewPwdExt = findViewById(R.id.re_new_password_ext);

        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPwdExt.getText().toString().equals("")) {
                    cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_current_password), null, null);
                    return;
                }
                if (newPwdExt.getText().toString().equals("")) {
                    cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_new_password), null, null);
                    return;
                }
                if (newPwdExt.getText().toString().length() < 6) {
                    cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_password_num), null, null);
                    return;
                }
                if (!newPwdExt.getText().toString().equals(renewPwdExt.getText().toString())) {
                    cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_password_match), null, null);
                    return;
                }

                loadingLayout.setVisibility(View.VISIBLE);

                RequestBody requestBody = new FormBody.Builder()
                        .add("old_password", currentPwdExt.getText().toString())
                        .add("new_password", newPwdExt.getText().toString())
                        .add("new_password_confirmation", renewPwdExt.getText().toString())
                        .add("userID", Common.me.getId())
                        .add("lang", Common.lang)
                        .build();

                Request request = new Request.Builder()
                        .url(Config.SERVER_URL + Config.PWD_CHANG_URL)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();
                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingLayout.setVisibility(View.GONE);
                                cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                            }
                        });
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        final String mMessage = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingLayout.setVisibility(View.GONE);
                                        try {
                                            JSONObject result = new JSONObject(mMessage);
                                            try {
                                                cm.showAlertDlg("", result.getString("message"), null, null);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Common.me = new UserModel(result.getJSONObject("data"));
                                                finish();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.currentActivity = this;
    }
}