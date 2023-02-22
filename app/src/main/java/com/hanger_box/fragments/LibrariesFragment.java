package com.hanger_box.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hanger_box.R;
import com.hanger_box.activities.AddItemActivity;
import com.hanger_box.activities.AddLibraryActivity;
import com.hanger_box.activities.LoginActivity;
import com.hanger_box.adapters.AdapterComment;
import com.hanger_box.adapters.LibrariesRecyclerViewAdapter;
import com.hanger_box.adapters.MyRecyclerViewAdapter;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.models.LibraryModel;
import com.hanger_box.models.ModelComment;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LibrariesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LibrariesFragment extends Fragment implements LibrariesRecyclerViewAdapter.ItemClickListener {

    private static final int LIBRARY_EDIT_ACTIVITY_ID = 1003;
    private static final int LIBRARY_ADD_ACTIVITY_ID = 3003;
    LibrariesRecyclerViewAdapter adapter;
    GridLayoutManager mLayoutManager;
    private View root_view, parent_view;
    private LinearLayout loadingLayout, emptyLibrariesLayout, moreLayout;
    private TextView tabAllBtn, tabMyLibrariesBtn, unreadbadge;
    private Handler handler;
    private Runnable runnable;
    private ArrayList<LibraryModel> libraries;
    private int tabIndex = 1;// 1: All, 2: My
    private int currentPage = 1;
    private int lastPage = 1;
    private static int cnt = 0;
    private Boolean loading = false;
//    private Parcelable recyclerViewState;
    public static RecyclerView recyclerView;
    private String searchTag = "";
    private Boolean shouldRefreshOnResume = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_libraries, null);
        parent_view = getActivity().findViewById(R.id.main_view);

        libraries = new ArrayList<>();
        loadingLayout = root_view.findViewById(R.id.loading_layout);
        moreLayout = root_view.findViewById(R.id.more_layout);
        moreBtnHide();
        emptyLibrariesLayout = root_view.findViewById(R.id.empty_libraries_layout);
        emptyLibrariesLayout.setVisibility(View.GONE);

        tabAllBtn = root_view.findViewById(R.id.tab_all_btn);
        tabMyLibrariesBtn = root_view.findViewById(R.id.tab_my_library_btn);
        unreadbadge = root_view.findViewById(R.id.unread_badge);

        unreadbadge.setVisibility(View.GONE);

        // set up the RecyclerView
        recyclerView = root_view.findViewById(R.id.grid_view);
        int numberOfColumns = 2;
        mLayoutManager = new GridLayoutManager(Common.currentActivity, numberOfColumns);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        adapter = new LibrariesRecyclerViewAdapter(Common.currentActivity, libraries);
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

        tabAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabAllBtn.setBackgroundColor(getResources().getColor(R.color.gray));
                tabAllBtn.setTextColor(getResources().getColor(R.color.white));
                tabMyLibrariesBtn.setBackgroundColor(getResources().getColor(R.color.white));
                tabMyLibrariesBtn.setTextColor(getResources().getColor(R.color.border_color));
//                libraries.clear();
                currentPage = 1;
                tabIndex = 1;
                getLibraries();
            }
        });

        tabMyLibrariesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.me == null) {
                    Intent intent = new Intent(currentActivity, LoginActivity.class);
                    startActivity(intent);
                    return;
                }

                tabAllBtn.setBackgroundColor(getResources().getColor(R.color.white));
                tabAllBtn.setTextColor(getResources().getColor(R.color.border_color));
                tabMyLibrariesBtn.setBackgroundColor(getResources().getColor(R.color.gray));
                tabMyLibrariesBtn.setTextColor(getResources().getColor(R.color.white));
//                libraries.clear();

                currentPage = 1;
                tabIndex = 2;
                getLibraries();

            }
        });

        root_view.findViewById(R.id.more_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loading) {
                    if (currentPage < lastPage) {
                        currentPage ++;
                    }
                    getLibraries();
                }
            }
        });

//        refresh();

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
            if (Common.initLibrary) {
                currentPage = 1;
                Common.initLibrary = false;
            }
            getLibraries();
        }
    }

    public void changedSearchTag(String tag) {
        searchTag = tag;
        currentPage = 1;
        getLibraries();
    }

    private void getLibraries() {
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        emptyLibrariesLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String apiUrl = tabIndex == 1 ? Config.LIBRARIES_URL : Config.MY_LIBRARIES_URL;

        String path = Config.SERVER_URL + apiUrl + "?page=" + String.valueOf(currentPage);

        RequestBody requestBody = new FormBody.Builder()
                .add("lang", Common.lang)
                .build();
        if (Common.me != null) {
            requestBody = new FormBody.Builder()
                    .add("userID", Common.me.getId())
                    .add("lang", Common.lang)
                    .build();
        }

        if (!searchTag.equals("")) {
            path = Config.SERVER_URL + apiUrl + "?q=" + String.valueOf(searchTag);
        }

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        if (Common.me != null) {
            request = new okhttp3.Request.Builder()
                    .url(path)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                    .post(requestBody)
                    .build();
        }
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
                                try {
                                    cm.showAlertDlg("", result.getString("message"), null, null);
                                    if (libraries.size() == 0)
                                        emptyLibrariesLayout.setVisibility(View.VISIBLE);
                                } catch (JSONException e) {
                                    e.printStackTrace();
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
                                        if (currentPage == 1) {
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
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        showUnreadPoints();
    }

    public LibrariesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LibrariesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibrariesFragment newInstance(String param1, String param2) {
        LibrariesFragment fragment = new LibrariesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onItemClick(View view, int position) {
        LibraryModel library = libraries.get(position);
        Intent intent = new Intent(currentActivity, AddLibraryActivity.class);
        intent.putExtra("from_activity", "library");
        intent.putExtra("from", "detail_library");
        intent.putExtra("library", library);
        getActivity().startActivityForResult(intent, LIBRARY_EDIT_ACTIVITY_ID);
//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        emptyLibrariesLayout.setVisibility(View.GONE);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(this).attach(this).commit();

        switch (requestCode) {
            case LIBRARY_ADD_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    LibraryModel newItem = (LibraryModel) data.getSerializableExtra("library");
                    libraries.add(0, newItem);
                    adapter.updateItemList(libraries);
                    adapter.notifyItemInserted(0);

                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                if (libraries.size() == 0)
                    emptyLibrariesLayout.setVisibility(View.VISIBLE);

                break;
            case LIBRARY_EDIT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    String keyWord = data.getStringExtra("key_word");
                    if (keyWord != null) {
                        mClickListener.onTagClick(keyWord);
                    }else {
                        LibraryModel updatedItem = (LibraryModel) data.getSerializableExtra("library");
                        Boolean deleted = data.getBooleanExtra("delete_flag", false);
                        for (int i=0; i<libraries.size(); i++) {
                            if (libraries.get(i).getId().equals(updatedItem.getId())) {
                                if (!deleted) {
                                    libraries.set(i, updatedItem);
                                    adapter.updateItemList(libraries);
                                }else {
                                    libraries.remove(i);
                                    adapter.updateItemList(libraries);
                                }
                                break;
                            }
                        }
                        if (libraries.size() == 0)
                            emptyLibrariesLayout.setVisibility(View.VISIBLE);
//                        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                    }
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }

                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mClickListener = (TagClickListener) context;
    }

    // allows clicks events to be caught
    public void setTagClickListener(TagClickListener tagClickListener) {
        this.mClickListener = tagClickListener;
    }

    TagClickListener mClickListener;

    // parent activity will implement this method to respond to click events
    public interface TagClickListener {
        void onTagClick(String key);
    }

    public int checkUnreadCount() {
        for(int i = 0; i < libraries.size(); i ++) {
            if(Common.me.getId().equals(libraries.get(i).getUserId())) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Libraries").child(libraries.get(i).getId()).child("Comments");

                Query query = reference.orderByChild("ptime");

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            ModelComment modelComment = dataSnapshot1.getValue(ModelComment.class);

                            if(modelComment.getreadstatus().equals("0")) {
                                cnt++;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        return cnt;
    }


    public void showUnreadPoints() {
        cnt = checkUnreadCount();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                unreadbadge.setText(String.valueOf(cnt));
                if(cnt == 0) {
                    unreadbadge.setVisibility(View.GONE);
                } else {
                    unreadbadge.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
        cnt = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        shouldRefreshOnResume = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //shoudRefreshOnResume is a global var
        if (shouldRefreshOnResume) {
            showUnreadPoints();
        }
    }
}