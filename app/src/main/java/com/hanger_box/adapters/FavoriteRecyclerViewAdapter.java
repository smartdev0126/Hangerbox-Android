package com.hanger_box.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.models.ItemModel;
import com.hanger_box.utils.GlideImageLoader;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteRecyclerViewAdapter extends RecyclerView.Adapter<FavoriteRecyclerViewAdapter.ViewHolder> {

    private ArrayList<HashMap> list;
    private LayoutInflater mInflater;
    private FavoriteRecyclerViewAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public FavoriteRecyclerViewAdapter(Context context, ArrayList<HashMap> data) {
        this.mInflater = LayoutInflater.from(context);
        this.list = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public FavoriteRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.favorite_cell, parent, false);
        return new FavoriteRecyclerViewAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull FavoriteRecyclerViewAdapter.ViewHolder holder, int position) {
        HashMap selectedItem = list.get(position);
        ItemModel item1 = (ItemModel) selectedItem.get("item_1");
        holder.itemImage1.setImageBitmap(null);
        RequestOptions options = new RequestOptions()
                .priority(Priority.HIGH);

        if (item1 != null) {
            holder.loadingProgress1.setVisibility(View.VISIBLE);
            new GlideImageLoader(holder.itemImage1,
                    holder.loadingProgress1).load(item1.getImage(), options);
        }else {
            holder.loadingProgress1.setVisibility(View.GONE);
        }
        ItemModel item2 = (ItemModel) selectedItem.get("item_2");
        holder.itemImage2.setImageBitmap(null);
        if (item2 != null) {
            holder.loadingProgress2.setVisibility(View.VISIBLE);
            new GlideImageLoader(holder.itemImage2,
                    holder.loadingProgress2).load(item2.getImage(), options);
        }else {
            holder.loadingProgress2.setVisibility(View.GONE);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return list.size();
    }


    public void updateItemList(ArrayList<HashMap> newList) {
        this.list = (ArrayList<HashMap>) newList.clone();
        notifyDataSetChanged();
    }

    public void addItemList(ArrayList<HashMap> newList) {
        this.list.addAll((ArrayList<HashMap>) newList.clone());
//        this.list.addAll((ArrayList<ItemModel>) newList.clone());
        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView itemImage1;
        ImageView itemImage2;
        ProgressBar loadingProgress1;
        ProgressBar loadingProgress2;

        ViewHolder(View itemView) {
            super(itemView);
            itemImage1 = itemView.findViewById(R.id.item_image1);
            itemImage2 = itemView.findViewById(R.id.item_image2);
            loadingProgress1 = itemView.findViewById(R.id.loading_progress1);
            loadingProgress1.setVisibility(View.GONE);
            loadingProgress2 = itemView.findViewById(R.id.loading_progress2);
            loadingProgress2.setVisibility(View.GONE);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    HashMap getItem(int id) {
        return list.get(id);
    }

    // allows clicks events to be caught
    public void setItemClickListener(FavoriteRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}