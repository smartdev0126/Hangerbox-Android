package com.hanger_box.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanger_box.R;
import com.hanger_box.activities.AddItemActivity;
import com.hanger_box.activities.FavoriteDetailActivity;
import com.hanger_box.adapters.FavoriteRecyclerViewAdapter;
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
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.content.ContentValues.TAG;
import static com.hanger_box.common.Common.categories;
import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteFragment extends Fragment implements FavoriteRecyclerViewAdapter.ItemClickListener {

    private static final int FAVORITE_EDIT_ACTIVITY_ID = 4001;

    FavoriteRecyclerViewAdapter adapter;
    private View root_view, parent_view;
    private LinearLayout loadingLayout, emptyFavoritesLayout, moreLayout;

    private ArrayList<HashMap> items;
    private int currentPage = 1;
    private int lastPage = 1;
    private int selectedCatId = 0;
    private Boolean loading = false;
    private Parcelable recyclerViewState;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_favorite, null);
        parent_view = getActivity().findViewById(R.id.main_view);

        items = new ArrayList<>();

        loadingLayout = root_view.findViewById(R.id.loading_layout);
        moreLayout = root_view.findViewById(R.id.more_layout);
        moreBtnHide();
        emptyFavoritesLayout = root_view.findViewById(R.id.empty_favorites_layout);
        emptyFavoritesLayout.setVisibility(View.GONE);

        // set up the RecyclerView
        recyclerView = root_view.findViewById(R.id.grid_view);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(Common.currentActivity, numberOfColumns));
        //recyclerView.setHasFixedSize(true);
        //recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        adapter = new FavoriteRecyclerViewAdapter(Common.currentActivity, items);
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

//        getFavorites(currentPage);

//        refresh();
        return root_view;
    }

    public void refresh() {
        if (Common.addFavorite) {
            currentPage = 1;
            Common.addFavorite = false;
        }
        getFavorites(currentPage);
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

    private void getFavorites(final int page) {
        if (Common.me == null) return;
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        emptyFavoritesLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.FAVORITES_URL + "?page=" + String.valueOf(page);

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
                            loadingLayout.setVisibility(View.GONE);
                            loading = false;
                            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                            if (items.size() == 0)
                                emptyFavoritesLayout.setVisibility(View.VISIBLE);
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
                                try {
                                    cm.showAlertDlg("", result.getString("message"), null, null);
                                    if (items.size() == 0)
                                        emptyFavoritesLayout.setVisibility(View.VISIBLE);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    JSONArray itemObjs = result.getJSONArray("data");
                                    if (itemObjs != null) {
                                        ArrayList newList = new ArrayList();
                                        for (int i=0; i<itemObjs.length(); i++) {
                                            JSONObject object = (JSONObject) itemObjs.get(i);
                                            HashMap map = new HashMap();
                                            try {
                                                map.put("id", object.getString("id"));
                                            } catch (JSONException e2) {
                                                e2.printStackTrace();
                                            }
                                            try {
                                                map.put("item_1", new ItemModel(object.getJSONObject("item_1")));
                                            } catch (JSONException e2) {
                                                e2.printStackTrace();
                                                map.put("item_1", null);
                                            }
                                            try {
                                                map.put("item_2", new ItemModel(object.getJSONObject("item_2")));
                                            } catch (JSONException e2) {
                                                e2.printStackTrace();
                                                map.put("item_2", null);
                                            }
                                            newList.add(map);
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
                                            emptyFavoritesLayout.setVisibility(View.VISIBLE);
//                                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
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
            }
        });
    }

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(currentActivity, FavoriteDetailActivity.class);
        intent.putExtra("favorite_item", items.get(position));
        getActivity().startActivityForResult(intent, FAVORITE_EDIT_ACTIVITY_ID);
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FAVORITE_EDIT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    HashMap updatedItem = (HashMap) data.getSerializableExtra("item");
                    Boolean deleted = data.getBooleanExtra("delete_flag", false);
                    for (int i=0; i<items.size(); i++) {
                        if (items.get(i).get("id").toString().equals(updatedItem.get("id").toString())) {
                            if (!deleted) {
                                items.set(i, updatedItem);
                                adapter.updateItemList(items);
                                adapter.notifyItemChanged(i);
                            }else {
                                items.remove(i);
                                adapter.updateItemList(items);
                                adapter.notifyItemRemoved(i);
                            }
                            break;
                        }
                    }
                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                }
                if (items.size() == 0)
                    emptyFavoritesLayout.setVisibility(View.VISIBLE);
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
        }
    }
}