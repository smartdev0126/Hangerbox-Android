package com.hanger_box.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.models.ItemModel;
import com.hanger_box.utils.GlideImageLoader;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ItemModel> list;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, ArrayList<ItemModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.list = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_cell, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel selectedItem = list.get(position);
        holder.itemImage.setImageBitmap(null);
        RequestOptions options = new RequestOptions()
                .priority(Priority.HIGH);

        holder.loadingProgress.setVisibility(View.VISIBLE);
        new GlideImageLoader(holder.itemImage,
                holder.loadingProgress).load(selectedItem.getImage(), options);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItemList(ArrayList<ItemModel> newList) {
        this.list = (ArrayList<ItemModel>) newList.clone();
        notifyDataSetChanged();
    }

    public void addItemList(ArrayList<ItemModel> newList) {
        this.list.addAll((ArrayList<ItemModel>) newList.clone());
//        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView itemImage;
        ProgressBar loadingProgress;

        ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemView.setOnClickListener(this);
            loadingProgress = itemView.findViewById(R.id.loading_progress);
            loadingProgress.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    ItemModel getItem(int id) {
        return list.get(id);
    }

    // allows clicks events to be caught
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}