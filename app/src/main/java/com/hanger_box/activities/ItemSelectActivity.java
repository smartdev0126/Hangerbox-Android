package com.hanger_box.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanger_box.R;
import com.hanger_box.adapters.MyRecyclerViewAdapter;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.utils.DialogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.content.ContentValues.TAG;
import static com.hanger_box.common.Common.categories;
import static com.hanger_box.common.Common.categories_en;
import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

public class ItemSelectActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    private RelativeLayout loadingView;
    MyRecyclerViewAdapter adapter;
    GridLayoutManager mLayoutManager;
    private LinearLayout loadingLayout, moreLayout;
    private TextView categoryTxt, moreBtn, emptyTitle;

    private ArrayList<ItemModel> items;
    private int currentPage = 1;
    private int lastPage = 1;
    private int selectedCatId = 0;
    private Boolean loading = false;
    private CharSequence [] searchCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_select);

        Common.currentActivity = this;

        items = new ArrayList<>();

        loadingLayout = findViewById(R.id.loading_layout);
        moreLayout = findViewById(R.id.more_layout);
        moreLayout.setVisibility(View.GONE);

        categoryTxt = findViewById(R.id.category_txt);
        emptyTitle = findViewById(R.id.empty_items_title);
        emptyTitle.setVisibility(View.GONE);
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.grid_view);
        int numberOfColumns = 2;
        mLayoutManager = new GridLayoutManager(Common.currentActivity, numberOfColumns);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new MyRecyclerViewAdapter(Common.currentActivity, items);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && dy > 0)
                {
                    if (currentPage < lastPage)
                        moreBtnShow();
                    else
                        moreBtnHide();
                }
                if (dy < 0)
                {
                    moreBtnHide();
                }
            }
        });

        getMyItems(currentPage);

        categoryTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categories != null) {
                    searchCategories = new CharSequence[categories.length+1];
                    searchCategories[0] = getString(R.string.ALL);
                    for (int i=0; i<categories.length; i++) {
                        if (Common.lang.equals("ja")) {
                            searchCategories[i+1] = categories[i];
                        }else {
                            searchCategories[i+1] = categories_en[i];
                        }
                    }
                    DialogManager.showRadioDialog(currentActivity, null,
                            searchCategories, selectedCatId, null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    categoryTxt.setText(searchCategories[which]);
                                    selectedCatId = which;
                                    currentPage = 1;
                                    getMyItems(currentPage);
                                }
                            });
                }
            }
        });

        findViewById(R.id.more_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loading) {
                    if (currentPage < lastPage) {
                        currentPage ++;
                        getMyItems(currentPage);
                    }
                }
            }
        });

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void moreBtnShow() {
        moreLayout.setVisibility(View.VISIBLE);
        moreLayout.animate()
                .translationY(0)
                .setDuration(100)
                .setListener(null);
    }

    private void moreBtnHide() {
        moreLayout.animate()
                .translationY(moreLayout.getHeight())
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        moreLayout.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.currentActivity = this;
    }

    private void getMyItems(final int page) {
        emptyTitle.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.MY_ITEMS_URL + "?page=" + String.valueOf(page);

        RequestBody requestBody = new FormBody.Builder()
                .add("userID", Common.me.getId())
                .add("lang", Common.lang)
                .build();

        if (selectedCatId > 0)
            requestBody = new FormBody.Builder()
                    .add("categoryID", selectedCatId == 0 ? "all" : String.valueOf(selectedCatId-1))
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
                String mMessage = e.getMessage().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        loading = false;
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                        if (items.size() == 0)
                            emptyTitle.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        loading = false;
                        try {
                            JSONObject result = new JSONObject(mMessage);
                            try {
                                cm.showAlertDlg("", result.getString("message"), null, null);
                                if (items.size() == 0)
                                    emptyTitle.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                JSONArray itemObjs = result.getJSONArray("data");
                                if (itemObjs != null) {
                                    ArrayList newList = new ArrayList();
                                    for (int i=0; i<itemObjs.length(); i++) {
                                        try {
                                            JSONObject object = (JSONObject) itemObjs.get(i);
                                            newList.add(new ItemModel(object));
                                        } catch (JSONException e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                    if (page == 1) {
                                        items.clear();
                                        items.addAll(newList);
                                        adapter.updateItemList(items);
                                    }else {
                                        items.addAll(newList);
                                        adapter.addItemList(newList);
                                    }
                                    if (items.size() == 0)
                                        emptyTitle.setVisibility(View.VISIBLE);
                                    JSONObject metaObj = result.getJSONObject("meta");
                                    if (metaObj != null) lastPage = metaObj.getInt("last_page");
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

    @Override
    public void onItemClick(View view, int position) {
        ItemModel item = items.get(position);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("item", item);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}