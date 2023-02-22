package com.hanger_box.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.hanger_box.R;
import com.hanger_box.common.Common;

public class PrivacyActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        webView = (WebView) findViewById(R.id.webView);
        if (Common.lang.equals("ja")) {
            webView.loadUrl("file:///android_asset/privacy.html");
        }else {
            webView.loadUrl("file:///android_asset/privacy_en.html");
        }

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}