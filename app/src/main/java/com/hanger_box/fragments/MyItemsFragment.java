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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanger_box.R;
import com.hanger_box.activities.AddItemActivity;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyItemsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyItemsFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener {

    private static final int ITEM_EDIT_ACTIVITY_ID = 1002;
    private static final int ITEM_ADD_ACTIVITY_ID = 2002;

    MyRecyclerViewAdapter adapter;
    GridLayoutManager mLayoutManager;
    private View root_view, parent_view;
    private LinearLayout loadingLayout, emptyItemsLayout, searchLayout, moreLayout;
    private TextView categoryTxt, emptyTitle;

    private ArrayList<ItemModel> items;
    private int currentPage = 1;
    private int lastPage = 1;
    private int selectedCatId = 0;
    private Boolean loading = false;
    private CharSequence [] searchCategories;
//    private Parcelable recyclerViewState;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_my_items, null);
        parent_view = getActivity().findViewById(R.id.main_view);

        items = new ArrayList<>();

        loadingLayout = root_view.findViewById(R.id.loading_layout);
        searchLayout = root_view.findViewById(R.id.search_layout);
        moreLayout = root_view.findViewById(R.id.more_layout);
        moreBtnHide();
        emptyItemsLayout = root_view.findViewById(R.id.empty_items_layout);
        emptyItemsLayout.setVisibility(View.GONE);

        categoryTxt = root_view.findViewById(R.id.category_txt);
        emptyTitle = root_view.findViewById(R.id.empty_items_title);
        emptyTitle.setVisibility(View.GONE);
        // set up the RecyclerView
        recyclerView = root_view.findViewById(R.id.grid_view);
        int numberOfColumns = 2;
        mLayoutManager = new GridLayoutManager(Common.currentActivity, numberOfColumns);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

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

//        getMyItems(currentPage);

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

        root_view.findViewById(R.id.from_camera_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddItemActivity.class);
                intent.putExtra("from_favorite", false);
                intent.putExtra("from", "from_camera");
                getActivity().startActivityForResult(intent, ITEM_ADD_ACTIVITY_ID);
            }
        });

        root_view.findViewById(R.id.from_gallery_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddItemActivity.class);
                intent.putExtra("from", "from_gallery");
                intent.putExtra("from_favorite", false);
                getActivity().startActivityForResult(intent, ITEM_ADD_ACTIVITY_ID);
            }
        });

        root_view.findViewById(R.id.from_link_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddItemActivity.class);
                intent.putExtra("from", "from_link");
                intent.putExtra("from_favorite", false);
                getActivity().startActivityForResult(intent, ITEM_ADD_ACTIVITY_ID);
            }
        });

        root_view.findViewById(R.id.more_btn).setOnClickListener(new View.OnClickListener() {
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

//        refresh();
        return root_view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

//        refresh();
    }

    public MyItemsFragment() {
        // Required empty public constructor
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
            if (Common.initMyItem) {
                currentPage = 1;
                Common.initMyItem = false;
            }
            getMyItems(currentPage);
        }
    }

    public void addItemBtnClicked() {
        searchLayout.setVisibility(View.GONE);
        emptyTitle.setVisibility(View.GONE);
        emptyItemsLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
    }

    public void backBtnClicked() {
        searchLayout.setVisibility(View.VISIBLE);
        emptyTitle.setVisibility(View.GONE);
        emptyItemsLayout.setVisibility(View.GONE);
//        loadingLayout.setVisibility(View.GONE);
    }

    private void getMyItems(final int page) {
        if (Common.me == null) return;
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        emptyItemsLayout.setVisibility(View.GONE);
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
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
            }
        });
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyItemsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyItemsFragment newInstance(String param1, String param2) {
        MyItemsFragment fragment = new MyItemsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onItemClick(View view, int position) {
        ItemModel item = items.get(position);
        Intent intent = new Intent(currentActivity, AddItemActivity.class);
        intent.putExtra("from", "detail_item");
        intent.putExtra("item", item);
        intent.putExtra("from_favorite", false);
        getActivity().startActivityForResult(intent, ITEM_EDIT_ACTIVITY_ID);
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        searchLayout.setVisibility(View.VISIBLE);
        emptyTitle.setVisibility(View.GONE);
        emptyItemsLayout.setVisibility(View.GONE);
        switch (requestCode) {
            case ITEM_ADD_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    ItemModel newItem = (ItemModel) data.getSerializableExtra("item");
                    items.add(0, newItem);
                    adapter.updateItemList(items);
                    adapter.notifyItemInserted(0);
                    searchLayout.setVisibility(View.VISIBLE);
                    emptyTitle.setVisibility(View.GONE);
                    loadingLayout.setVisibility(View.GONE);
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                if (items.size() == 0)
                    emptyTitle.setVisibility(View.VISIBLE);
                break;
            case ITEM_EDIT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    ItemModel updatedItem = (ItemModel) data.getSerializableExtra("item");
                    Boolean deleted = data.getBooleanExtra("delete_flag", false);
                    for (int i=0; i<items.size(); i++) {
                        if (items.get(i).getId().equals(updatedItem.getId())) {
                            if (!deleted) {
                                items.set(i, updatedItem);
                                adapter.updateItemList(items);
//                                adapter.notifyItemChanged(i);
                            }else {
                                items.remove(i);
                                adapter.updateItemList(items);
//                                adapter.notifyItemRemoved(i);
                            }
                            break;
                        }
                    }
                    if (items.size() == 0)
                        emptyTitle.setVisibility(View.VISIBLE);
//                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
        }
    }
}