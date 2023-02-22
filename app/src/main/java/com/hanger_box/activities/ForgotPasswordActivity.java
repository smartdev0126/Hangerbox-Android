package com.hanger_box.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;

import static com.hanger_box.common.Common.cm;

public class ForgotPasswordActivity extends AppCompatActivity {

    private LinearLayout loadingLayout;
    private EditText emailExt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Common.currentActivity = this;

        loadingLayout = findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);
        emailExt = findViewById(R.id.email_txt);

        findViewById(R.id.reset_pwd_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailExt.getText().toString().equals("")) {
                    cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_input_email), null, null);
                    return;
                }
                loadingLayout.setVisibility(View.VISIBLE);

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                final String path = Config.SERVER_URL + Config.CHANG_PWD_URL;
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                // Add form parameter
                formBodyBuilder.add("email", emailExt.getText().toString());
                formBodyBuilder.add("lang", Common.lang);
                // Build form body.
                FormBody formBody = formBodyBuilder.build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(path)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .post(formBody)
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
                        String mMessage = e.getMessage().toString();
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        final String mMessage = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingLayout.setVisibility(View.GONE);
                                try {
                                    JSONObject result = new JSONObject(mMessage);
                                    cm.showAlertDlg("", result.getString("message"), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }, null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
}