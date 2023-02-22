package com.hanger_box.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hanger_box.R;
import com.hanger_box.adapters.LibrariesRecyclerViewAdapter;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.models.LibraryModel;
import com.hanger_box.models.UserModel;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener, LibrariesRecyclerViewAdapter.ItemClickListener {

    private static final int MYLIBRARY_EDIT_ACTIVITY_ID = 5004;

    LibrariesRecyclerViewAdapter adapter;
    GridLayoutManager mLayoutManager;
    private LinearLayout loadingLayout, emptyLibrariesLayout, moreLayout;
    private TextView nicknameTxt, countryTxt, profileTxt;
    private RoundedImageView avatarImg;
    private TextView followNum, followerNum, favoriteTxt, itemsTxt, librariesTxt, followBtn;

    private ArrayList<LibraryModel> libraries;
    private int follows;
    private int followers;
    private int currentPage = 1;
    private int lastPage = 1;
    private int selectedCatId = 0;
    private Boolean loading = false;
//    private Parcelable recyclerViewState;
    private RecyclerView recyclerView;

    private int myFavorites = 0;
    private int myItems = 0;
    private int myLibraries = 0;
    private Boolean followFlag = false;

    private UserModel me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Common.currentActivity = this;

        libraries = new ArrayList<>();
        follows = 0;
        followers = 0;
        loadingLayout = findViewById(R.id.loading_layout);
        moreLayout = findViewById(R.id.more_layout);
        moreBtnHide();
        emptyLibrariesLayout = findViewById(R.id.empty_libraries_layout);
        emptyLibrariesLayout.setVisibility(View.GONE);

        avatarImg = findViewById(R.id.user_avatar);

        nicknameTxt = findViewById(R.id.user_nickname);
        countryTxt = findViewById(R.id.user_country);
        profileTxt = findViewById(R.id.user_profile);

        // set up the RecyclerView
        recyclerView = findViewById(R.id.grid_view);
        int numberOfColumns = 2;
        mLayoutManager = new GridLayoutManager(currentActivity, numberOfColumns);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        adapter = new LibrariesRecyclerViewAdapter(currentActivity, libraries);
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

        followNum = findViewById(R.id.follow_num);
        followerNum = findViewById(R.id.follower_num);

        followBtn = findViewById(R.id.follow_btn);
        followBtn.setOnClickListener(this);

        favoriteTxt = findViewById(R.id.favorites_txt);
        itemsTxt = findViewById(R.id.items_txt);
        librariesTxt = findViewById(R.id.libraries_txt);

        findViewById(R.id.back_btn).setOnClickListener(this);

        me = (UserModel) getIntent().getExtras().getSerializable("user_info");

        findViewById(R.id.more_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loading) {
                    if (currentPage < lastPage) {
                        currentPage ++;
                    }
                    getMyLibraries();
                }
            }
        });

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.currentActivity = this;
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

    public void refresh() {
        if (!loading) {
            currentPage = 1;
            showMyInfo();
            getMyProfile();
            getMyLibraries();
            getMyFollowers();
            getMyFollows();
        }
    }

    private void showMyInfo() {
        if(me == null) {
            cm.showAlertDlg("",getString(R.string.error_show_account), null, null);
        }else {
            if (me.getAvatar() != null && !me.getAvatar().equals("")) {
                Glide.with(currentActivity)
                        .load(me.getAvatar())
                        .placeholder(getResources().getDrawable(R.drawable.ic_profile_gray))
                        .into(avatarImg);
            } else {
                avatarImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_profile_gray));
            }
            nicknameTxt.setText(me.getName());
            countryTxt.setText(me.getCountryName());
            profileTxt.setText(me.getProfile());
            favoriteTxt.setText(String.valueOf(myFavorites));
            itemsTxt.setText(String.valueOf(myItems));
            librariesTxt.setText(String.valueOf(myLibraries));
            followNum.setText(String.valueOf(follows));
            followerNum.setText(String.valueOf(followers));
        }
    }


    private void getMyProfile() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        if(me == null) {
            cm.showAlertDlg("",getString(R.string.error_show_account), null, null);
        } else {
            final String path = Config.SERVER_URL + Config.USERINFO_URL + "/" + me.getId();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(path)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    String mMessage = e.getMessage().toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                            try {
                                JSONObject result = new JSONObject(mMessage);
                                try {
                                    cm.showAlertDlg("", result.getString("message"), null, null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    me = new UserModel(result);

                                    try {
                                        myFavorites = Integer.valueOf(result.getString("favoriteCount"));
                                    } catch (JSONException e2) {
                                        e2.printStackTrace();
                                        myFavorites = 0;
                                    }
                                    try {
                                        myItems = Integer.valueOf(result.getString("itemsCount"));
                                    } catch (JSONException e2) {
                                        e2.printStackTrace();
                                        myItems = 0;
                                    }
                                    try {
                                        myLibraries = Integer.valueOf(result.getString("libraryCount"));
                                    } catch (JSONException e2) {
                                        e2.printStackTrace();
                                        myLibraries = 0;
                                    }
                                    showMyInfo();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }


    }

    private void getMyLibraries() {
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        emptyLibrariesLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String apiUrl = Config.USER_SHOWN_LIBRARIES_URL;
        final int page = currentPage;

        String path = Config.SERVER_URL + apiUrl + "?page=" + String.valueOf(page);
        if(me == null) {
            cm.showAlertDlg("",getString(R.string.error_show_account), null, null);
        } else {
            RequestBody requestBody = new FormBody.Builder()
                    .add("id", me.getId())
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
                            if (libraries.size() == 0)
                                emptyLibrariesLayout.setVisibility(View.VISIBLE);
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
                                    if (libraries.size() == 0)
                                        emptyLibrariesLayout.setVisibility(View.VISIBLE);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    JSONArray itemObjs = result.getJSONArray("data");
                                    if (itemObjs != null) {
                                        ArrayList newList = new ArrayList();
                                        for (int i = 0; i < itemObjs.length(); i++) {
                                            try {
                                                JSONObject object = (JSONObject) itemObjs.get(i);
                                                newList.add(new LibraryModel(object));
                                            } catch (JSONException e2) {
                                                e2.printStackTrace();
                                            }
                                        }
                                        if (page == 1) {
                                            libraries.clear();
                                            libraries.addAll(newList);
                                            adapter.updateItemList(libraries);
                                        } else {
                                            libraries.addAll(newList);
                                            adapter.addItemList(newList);
                                        }
                                        if (libraries.size() == 0)
                                            emptyLibrariesLayout.setVisibility(View.VISIBLE);

                                        moreBtnHide();
                                        JSONObject metaObj = result.getJSONObject("meta");
                                        if (metaObj != null) {
                                            lastPage = metaObj.getInt("last_page");
                                        }
//                                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
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
    }

    private void getMyFollows() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.GET_FOLLOWS_URL;
        if(me == null) {
            cm.showAlertDlg("",getString(R.string.error_show_account), null, null);
        } else {
            RequestBody requestBody = new FormBody.Builder()
                    .add("user_id", me.getId())
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
                            try {
                                JSONObject result = new JSONObject(mMessage);
                                try {
                                    cm.showAlertDlg("", result.getString("message"), null, null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    JSONArray itemObjs = result.getJSONArray("data");
                                    if (itemObjs != null) {
                                        follows = itemObjs.length();
                                        followNum.setText(String.valueOf(follows));
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
    }

    private void getMyFollowers() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.GET_FOLLOWERS_URL;
        if(me == null) {
            cm.showAlertDlg("",getString(R.string.error_show_account), null, null);
        } else {
            RequestBody requestBody = new FormBody.Builder()
                    .add("user_id", me.getId())
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
                            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                        }
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    final String mMessage = response.body().string();
                    runOnUiThread(new Runnable() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void run() {
                            try {
                                JSONObject result = new JSONObject(mMessage);
                                try {
                                    cm.showAlertDlg("", result.getString("message"), null, null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    JSONArray itemObjs = result.getJSONArray("data");
                                    if (itemObjs != null) {
                                        ArrayList newList = new ArrayList();
                                        for (int i = 0; i < itemObjs.length(); i++) {
                                            try {
                                                JSONObject object = itemObjs.getJSONObject(i);
                                                UserModel user = new UserModel(object);
                                                newList.add(user);
                                                if (user.getId().equals(Common.me.getId())) {
                                                    followFlag = true;
                                                }
                                            } catch (JSONException e2) {
                                                e2.printStackTrace();
                                            }
                                        }
                                        if (followFlag) {
                                            followBtn.setText(R.string.btn_following);
                                            followBtn.setBackground(getResources().getDrawable(R.drawable.round_btn));
                                            followBtn.setTextColor(getResources().getColor(R.color.white));
                                        } else {
                                            followBtn.setText(R.string.btn_follow);
                                            followBtn.setBackground(getResources().getDrawable(R.drawable.round_btn_white));
                                            followBtn.setTextColor(getResources().getColor(R.color.black));
                                        }
                                        followers = itemObjs.length();
                                        followerNum.setText(String.valueOf(followers));
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
    }

    private void setFollow() {
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.SET_FOLLOW_URL;

        RequestBody requestBody = new FormBody.Builder()
                .add("user_id1", Common.me.getId())
                .add("user_id2", me.getId())
                .add("action", followFlag ? "delete" : "create")
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
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                runOnUiThread(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        loading = false;
                        try {
                            JSONObject result = new JSONObject(mMessage);
                            try {
                                cm.showAlertDlg("", result.getString("message"), null, null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                if (followFlag) {
                                    followFlag = false;
                                    followBtn.setText(R.string.btn_follow);
                                    followBtn.setBackground(getResources().getDrawable(R.drawable.round_btn_white));
                                    followBtn.setTextColor(getResources().getColor(R.color.black));
                                    followers --;
                                }else {
                                    followFlag = true;
                                    followBtn.setText(R.string.btn_following);
                                    followBtn.setBackground(getResources().getDrawable(R.drawable.round_btn));
                                    followBtn.setTextColor(getResources().getColor(R.color.white));
                                    followers ++;
                                }
                                followerNum.setText(String.valueOf(followers));
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
            case R.id.follow_btn:
                setFollow();
                break;
            case R.id.back_btn:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("user", me);
                if (followFlag) {
                    returnIntent.putExtra("delete_flag", false);
                }else {
                    returnIntent.putExtra("delete_flag", true);
                }
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        LibraryModel library = libraries.get(position);
        Intent intent = new Intent(currentActivity, AddLibraryActivity.class);
        intent.putExtra("from_activity", "profile");
        intent.putExtra("from", "detail_library");
        intent.putExtra("library", library);
        startActivityForResult(intent, MYLIBRARY_EDIT_ACTIVITY_ID);
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }
}