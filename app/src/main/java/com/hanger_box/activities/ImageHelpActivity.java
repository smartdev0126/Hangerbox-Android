package com.hanger_box.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanger_box.R;
import com.hanger_box.common.Common;

public class ImageHelpActivity extends AppCompatActivity {

    private TextView browserTxt1, browserTxt2, saveImgTxt, copyImgTxt;
    private ImageView browserImg, saveImg, copyImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_help);

        Common.currentActivity = this;

        browserTxt1 = findViewById(R.id.browser_install1_txt);
        browserTxt2 = findViewById(R.id.browser_install2_txt);
        saveImgTxt = findViewById(R.id.save_image_txt);
        copyImgTxt = findViewById(R.id.copy_image_txt);

        browserImg = findViewById(R.id.browser_install_img);
        saveImg = findViewById(R.id.save_image_img);
        copyImg = findViewById(R.id.copy_image_img);

        if (!(Common.me.getCountryName().equals("日本") || Common.me.getCountryName().equals(""))) {
            browserImg.setImageDrawable(getResources().getDrawable(R.drawable.help_en_1));
            saveImg.setImageDrawable(getResources().getDrawable(R.drawable.help_en_2));
            copyImg.setImageDrawable(getResources().getDrawable(R.drawable.help_en_3));
        }
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}