package com.hanger_box.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hanger_box.R;
import com.hanger_box.activities.AddLibraryActivity;
import com.hanger_box.activities.FollowActivity;
import com.hanger_box.activities.LoginActivity;
import com.hanger_box.activities.UserProfileActivity;
import com.hanger_box.adapters.LibrariesRecyclerViewAdapter;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.LibraryModel;
import com.hanger_box.models.UserModel;
import com.hanger_box.utils.GlideApp;
import com.hanger_box.utils.GlideOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.countries;
import static com.hanger_box.common.Common.countries_en;
import static com.hanger_box.common.Common.currentActivity;

public class MyPageFragment extends Fragment implements View.OnClickListener, LibrariesRecyclerViewAdapter.ItemClickListener {

    private static final int LIBRARY_EDIT_ACTIVITY_ID = 1003;

    LibrariesRecyclerViewAdapter adapter;
    GridLayoutManager mLayoutManager;
    private View root_view, parent_view;
    private LinearLayout loadingLayout, emptyLibrariesLayout, moreLayout, followBtn, followerBtn;
    private TextView nicknameTxt, countryTxt, profileTxt;
    private RoundedImageView avatarImg;
    private TextView followNum, followerNum, favoriteTxt, itemsTxt, librariesTxt;

    private ArrayList<LibraryModel> libraries;
    private ArrayList<UserModel> follows;
    private ArrayList<UserModel> followers;
    private int currentPage = 1;
    private int lastPage = 1;
    private int selectedCatId = 0;
    private Boolean loading = false;
//    private Parcelable recyclerViewState;
    private RecyclerView recyclerView;

    private int myFavorites = 0;
    private int myItems = 0;
    private int myLibraries = 0;

    public MyPageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_my_page, null);
        parent_view = getActivity().findViewById(R.id.main_view);

        libraries = new ArrayList<>();
        follows = new ArrayList<>();
        followers = new ArrayList<>();
        loadingLayout = root_view.findViewById(R.id.loading_layout);
        moreLayout = root_view.findViewById(R.id.more_layout);
        moreBtnHide();
        emptyLibrariesLayout = root_view.findViewById(R.id.empty_libraries_layout);
        emptyLibrariesLayout.setVisibility(View.GONE);

        avatarImg = root_view.findViewById(R.id.user_avatar);

        nicknameTxt = root_view.findViewById(R.id.user_nickname);
        countryTxt = root_view.findViewById(R.id.user_country);

        profileTxt = root_view.findViewById(R.id.user_profile);

        // set up the RecyclerView
        recyclerView = root_view.findViewById(R.id.grid_view);
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

        followNum = root_view.findViewById(R.id.follow_num);
        followerNum = root_view.findViewById(R.id.follower_num);

        followBtn = root_view.findViewById(R.id.follow_btn);
        followBtn.setOnClickListener(this);
        followerBtn = root_view.findViewById(R.id.follower_btn);
        followerBtn.setOnClickListener(this);

        favoriteTxt = root_view.findViewById(R.id.favorites_txt);
        itemsTxt = root_view.findViewById(R.id.items_txt);
        librariesTxt = root_view.findViewById(R.id.libraries_txt);

        root_view.findViewById(R.id.more_btn).setOnClickListener(new View.OnClickListener() {
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

        return root_view;
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
            getMyProfile();
            getMyLibraries();
            getMyFollowers();
            getMyFollows();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showMyInfo() {
        if (Common.me.getAvatar() != null && Common.me.getAvatar() != "") {

            GlideApp.with(currentActivity).load(Common.me.getAvatar())
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .error(getResources().getDrawable(R.drawable.ic_profile_gray))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).into(avatarImg);
/*
            Glide.with(currentActivity)
                    .load(Common.me.getAvatar())
                    .placeholder(getResources().getDrawable(R.drawable.ic_profile_gray))
                    .into(avatarImg);
 */
        }else {
            avatarImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_profile_gray));
        }
        nicknameTxt.setText(Common.me.getName());

        int selectedId = 0;
        if (Common.lang.equals("ja")) {
            for (int i = 0; i < countries.length; i++) {
                if (Common.me.getCountryName().equals(countries[i]))
                    selectedId = i;
            }
        }else {
            for (int i = 0; i < countries_en.length; i++) {
                if (Common.me.getCountryName().equals(countries_en[i]))
                    selectedId = i;
            }
        }
        if (Common.lang.equals("ja")) {
            if (countries.length > 0)
                countryTxt.setText(String.valueOf(countries[selectedId]));
        }else {
            if (countries_en.length > 0)
                countryTxt.setText(String.valueOf(countries_en[selectedId]));
        }

        profileTxt.setText(Common.me.getProfile());
        favoriteTxt.setText(String.valueOf(myFavorites));
        itemsTxt.setText(String.valueOf(myItems));
        librariesTxt.setText(String.valueOf(myLibraries));
        followNum.setText(String.valueOf(follows.size()));
        followerNum.setText(String.valueOf(followers.size()));
    }

    private void getMyProfile() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.USERINFO_URL;

        RequestBody requestBody = new FormBody.Builder()
                .add("categoryID", String.valueOf(selectedCatId))
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                        }
                    });
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject result = new JSONObject(mMessage);
 //                               try {
//                                    cm.showAlertDlg("", result.getString("message"), null, null);
 //                               } catch (JSONException e) {
  //                                  e.printStackTrace();
                                    JSONObject itemObjs = result.getJSONObject("data");
                                    Common.me = new UserModel(itemObjs);
                                    Common.setLocale();
                                    showMyInfo();

                                    try {
                                        myFavorites = Integer.valueOf(itemObjs.getString("favoriteCount"));
                                    } catch (JSONException e2) {
                                        e2.printStackTrace();
                                        myFavorites = 0;
                                    }
                                    try {
                                        myItems = Integer.valueOf(itemObjs.getString("itemsCount"));
                                    } catch (JSONException e2) {
                                        e2.printStackTrace();
                                        myItems = 0;
                                    }
                                    try {
                                        myLibraries = Integer.valueOf(itemObjs.getString("libraryCount"));
                                    } catch (JSONException e2) {
                                        e2.printStackTrace();
                                        myLibraries = 0;
                                    }
     //                           }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

    }

    private void getMyLibraries() {
        if (Common.me == null) return;
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        emptyLibrariesLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String apiUrl = Config.MY_SHOWN_LIBRARIES_URL;
        final int page = currentPage;

        String path = Config.SERVER_URL + apiUrl + "?page=" + String.valueOf(page);

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
                String mMessage = e.getMessage().toString();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
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
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingLayout.setVisibility(View.GONE);
                            loading = false;
                            try {
                                JSONObject result = new JSONObject(mMessage);
//                                try {
//                                    cm.showAlertDlg("", result.getString("message"), null, null);
                                    if (libraries.size() == 0)
                                        emptyLibrariesLayout.setVisibility(View.VISIBLE);
 //                               } catch (JSONException e) {
 //                                   e.printStackTrace();
                                    JSONArray itemObjs = result.getJSONArray("data");
                                    if (itemObjs != null) {
                                        ArrayList newList = new ArrayList();
                                        for (int i=0; i<itemObjs.length(); i++) {
                                            try {
                                                JSONObject object = itemObjs.getJSONObject(i);
                                                newList.add(new LibraryModel(object));
                                            } catch (JSONException e2) {
                                                e2.printStackTrace();
                                            }
                                        }
                                        if (page == 1) {
                                            libraries.clear();
                                            libraries.addAll(newList);
                                            adapter.updateItemList(libraries);
                                        }else {
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
  //                              }
 //                           } catch (JSONException e) {
  //                              e.printStackTrace();
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void getMyFollows() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.GET_FOLLOWS_URL;

        RequestBody requestBody = new FormBody.Builder()
                .add("user_id", Common.me.getId())
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                        }
                    });
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject result = new JSONObject(mMessage);
   //                             try {
  //                                  cm.showAlertDlg("", result.getString("message"), null, null);
   //                             } catch (JSONException e) {
    //                                e.printStackTrace();
                                    JSONArray itemObjs = result.getJSONArray("data");
                                    if (itemObjs != null) {
                                        ArrayList newList = new ArrayList();
                                        for (int i=0; i<itemObjs.length(); i++) {
                                            try {
                                                JSONObject object = itemObjs.getJSONObject(i);
                                                newList.add(new UserModel(object));
                                            } catch (JSONException e2) {
                                                e2.printStackTrace();
                                            }
                                        }
                                        follows.clear();
                                        follows.addAll(newList);
                                        followNum.setText(String.valueOf(follows.size()));
   //                                 }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

    }

    private void getMyFollowers() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.GET_FOLLOWERS_URL;

        RequestBody requestBody = new FormBody.Builder()
                .add("user_id", Common.me.getId())
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                        }
                    });
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject result = new JSONObject(mMessage);
   //                             try {
   //                                 cm.showAlertDlg("", result.getString("message"), null, null);
   //                             } catch (JSONException e) {
    //                                e.printStackTrace();
                                    JSONArray itemObjs = result.getJSONArray("data");
                                    if (itemObjs != null) {
                                        ArrayList newList = new ArrayList();
                                        for (int i=0; i<itemObjs.length(); i++) {
                                                try {
                                                    JSONObject object = itemObjs.getJSONObject(i);
                                                    newList.add(new UserModel(object));
                                                } catch (JSONException e2) {
                                                    e2.printStackTrace();
                                                }
                                        }
                                        followers.clear();
                                        followers.addAll(newList);
                                        followerNum.setText(String.valueOf(followers.size()));
                                    }
      //                          }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Common.me != null) {
            showMyInfo();
            refresh();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.follow_btn:
                if (follows.size() > 0) {
                    intent = new Intent(currentActivity, FollowActivity.class);
                    intent.putExtra("follow_list", follows);
                    intent.putExtra("type", "follow");
                    startActivity(intent);
                }
                break;
            case R.id.follower_btn:
                if (followers.size() > 0) {
                    intent = new Intent(currentActivity, FollowActivity.class);
                    intent.putExtra("follow_list", followers);
                    intent.putExtra("type", "follower");
                    startActivity(intent);
                }
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
        getActivity().startActivityForResult(intent, LIBRARY_EDIT_ACTIVITY_ID);
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        emptyLibrariesLayout.setVisibility(View.GONE);
//        switch (requestCode) {
//            case MYLIBRARY_EDIT_ACTIVITY_ID:
//                if(resultCode == Activity.RESULT_OK){
//                    String keyWord = data.getStringExtra("key_word");
//                    if (keyWord != null) {
////                        mClickListener.onMyTagClick(keyWord);
//                    }else {
//                        LibraryModel updatedItem = (LibraryModel) data.getSerializableExtra("library");
//                        Boolean deleted = data.getBooleanExtra("delete_flag", false);
//                        for (int i=0; i<libraries.size(); i++) {
//                            if (libraries.get(i).getId().equals(updatedItem.getId())) {
//                                if (!deleted) {
//                                    libraries.set(i, updatedItem);
//                                    adapter.updateItemList(libraries);
////                                adapter.notifyItemChanged(i);
//                                }else {
//                                    libraries.remove(i);
//                                    adapter.updateItemList(libraries);
////                                adapter.notifyItemRemoved(i);
//                                }
//                                break;
//                            }
//                        }
//                        if (libraries.size() == 0)
//                            emptyLibrariesLayout.setVisibility(View.VISIBLE);
//                        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
//                    }
//                }
//                if (resultCode == Activity.RESULT_CANCELED) {
//                }
//                break;
//        }
//    }
}