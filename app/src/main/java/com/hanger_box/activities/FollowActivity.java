package com.hanger_box.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hanger_box.R;
import com.hanger_box.adapters.FollowRecyclerViewAdapter;
import com.hanger_box.common.Common;
import com.hanger_box.models.UserModel;

import java.util.ArrayList;

import static com.hanger_box.common.Common.currentActivity;

public class FollowActivity extends AppCompatActivity implements FollowRecyclerViewAdapter.ItemClickListener, View.OnClickListener {

    private static final int FOLLOW_EDIT_ACTIVITY_ID = 5005;

    FollowRecyclerViewAdapter adapter;
    private LinearLayout loadingLayout;
    private TextView titleView;

    private ArrayList<UserModel> items;
//    private Parcelable recyclerViewState;
    private RecyclerView recyclerView;
    private String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        currentActivity = this;

        items = new ArrayList<>();

        loadingLayout = findViewById(R.id.loading_layout);
        titleView = findViewById(R.id.titleTxt);

        // set up the RecyclerView
        recyclerView = findViewById(R.id.grid_view);
        int numberOfColumns = 1;
        recyclerView.setLayoutManager(new GridLayoutManager(Common.currentActivity, numberOfColumns));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        adapter = new FollowRecyclerViewAdapter(Common.currentActivity, items);
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
//                if (!recyclerView.canScrollVertically(1) && dy > 0)
//                {
//                    if (currentPage < lastPage)
//                        moreBtnShow();
//                    else
//                        moreBtnHide();
//                }
//                if (dy < 0)
//                {
//                    moreBtnHide();
//                }
            }
        });
        findViewById(R.id.back_btn).setOnClickListener(this);

        items = (ArrayList<UserModel>) getIntent().getExtras().getSerializable("follow_list");
        adapter.updateItemList(items);
        type = getIntent().getExtras().getString("type");
        if (type.equals("follow")) {
            titleView.setText(getResources().getString(R.string.show_followings));
        }else {
            titleView.setText(getResources().getString(R.string.show_followers));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        currentActivity = this;
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        intent.putExtra("user_info", items.get(position));
        startActivityForResult(intent, FOLLOW_EDIT_ACTIVITY_ID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.back_btn:
                finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FOLLOW_EDIT_ACTIVITY_ID:
                if (resultCode == Activity.RESULT_OK) {
                    UserModel updatedItem = (UserModel) data.getSerializableExtra("user");
                    Boolean deleted = data.getBooleanExtra("delete_flag", false);
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getId().toString().equals(updatedItem.getId().toString())) {
                            if (!deleted) {
                                items.set(i, updatedItem);
                                adapter.updateItemList(items);
                                adapter.notifyItemChanged(i);
                            } else {
                                items.remove(i);
                                adapter.updateItemList(items);
                                adapter.notifyItemRemoved(i);
                            }
                            break;
                        }
                    }
//                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
        }
    }
}