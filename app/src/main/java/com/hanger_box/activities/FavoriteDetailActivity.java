package com.hanger_box.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.utils.GlideImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

public class FavoriteDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ITEM_SELECT_ACTIVITY_ID = 2000;

    private TextView titleTxt;
    private LinearLayout loadingLayout;
    private ImageView itemImage1, itemImage2;
    private LinearLayout deleteBtn;
    private ProgressBar loadingProgress1, loadingProgress2;

    private String selectedItem = "";
    private HashMap item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_detail);

        currentActivity = this;

        titleTxt = findViewById(R.id.titleTxt);
        loadingLayout = findViewById(R.id.loading_layout);

        itemImage1 = findViewById(R.id.top_item);
        itemImage1.setOnClickListener(this);
        loadingProgress1 = findViewById(R.id.loading_progress1);
        loadingProgress1.setVisibility(View.GONE);
        loadingProgress2 = findViewById(R.id.loading_progress2);
        loadingProgress2.setVisibility(View.GONE);

        itemImage2 = findViewById(R.id.bottom_item);
        itemImage2.setOnClickListener(this);

        deleteBtn = findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(this);

        findViewById(R.id.back_btn).setOnClickListener(this);

        item = (HashMap) getIntent().getExtras().getSerializable("favorite_item");
        itemRefresh();

        loadingLayout.setVisibility(View.GONE);
    }

    private void itemRefresh() {
        RequestOptions options = new RequestOptions()
                .priority(Priority.HIGH);
        ItemModel item1 = (ItemModel) item.get("item_1");
        itemImage1.setImageBitmap(null);
        if (item1 != null) {
            loadingProgress1.setVisibility(View.VISIBLE);
            new GlideImageLoader(itemImage1,
                    loadingProgress1).load(item1.getImage(), options);
        }else {
            loadingProgress1.setVisibility(View.GONE);
        }
        ItemModel item2 = (ItemModel) item.get("item_2");
        itemImage2.setImageBitmap(null);
        if (item2 != null) {
            loadingProgress2.setVisibility(View.VISIBLE);
            new GlideImageLoader(itemImage2,
                    loadingProgress2).load(item2.getImage(), options);
        }else {
            loadingProgress2.setVisibility(View.GONE);
        }
    }

    private void deleteFavorite() {
        loadingLayout.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.DELETE_FAVORITE_URL + "/" + item.get("id");

        RequestBody requestBody = new FormBody.Builder()
                .add("userID", Common.me.getId())
                .add("lang", Common.lang)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                            try {
                                cm.showAlertDlg("", result.getString("message"), null, null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("item", item);
                                returnIntent.putExtra("delete_flag", true);
                                setResult(Activity.RESULT_OK,returnIntent);
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

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.back_btn:
                if (!selectedItem.equals("")) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("item", item);
                    returnIntent.putExtra("delete_flag", false);
                    setResult(Activity.RESULT_OK,returnIntent);
                }
                finish();
                break;
            case R.id.delete_btn:
                Common.cm.showAlertDlg(getString(R.string.alert_delete_favorite_title), getString(R.string.alert_delete_favorite_msg),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFavorite();
                            }
                        }, null);
                break;
            case R.id.top_item:
                selectedItem = "item_1";
                ItemModel item1 = (ItemModel) item.get("item_1");
                if (item1 != null) {
                    intent = new Intent(currentActivity, AddItemActivity.class);
                    intent.putExtra("from", "detail_item");
                    intent.putExtra("from_favorite", true);
                    intent.putExtra("item", item1);
                    startActivityForResult(intent, ITEM_SELECT_ACTIVITY_ID);
                }
                break;
            case R.id.bottom_item:
                selectedItem = "item_2";
                ItemModel item2 = (ItemModel) item.get("item_2");
                if (item2 != null) {
                    intent = new Intent(currentActivity, AddItemActivity.class);
                    intent.putExtra("from", "detail_item");
                    intent.putExtra("from_favorite", true);
                    intent.putExtra("item", item2);
                    startActivityForResult(intent, ITEM_SELECT_ACTIVITY_ID);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentActivity = this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ITEM_SELECT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    ItemModel updatedItem = (ItemModel) data.getSerializableExtra("item");
                    item.put(selectedItem, updatedItem);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            itemRefresh();
                        }
                    }, 500);
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
        }
    }
}