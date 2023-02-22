package com.hanger_box.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.models.UserModel;
import com.hanger_box.models.UserModel;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.hanger_box.common.Common.currentActivity;

public class FollowRecyclerViewAdapter extends RecyclerView.Adapter<FollowRecyclerViewAdapter.ViewHolder> {

    private ArrayList<UserModel> list;
    private LayoutInflater mInflater;
    private FollowRecyclerViewAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public FollowRecyclerViewAdapter(Context context, ArrayList<UserModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.list = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public FollowRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.follow_cell, parent, false);
        return new FollowRecyclerViewAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull FollowRecyclerViewAdapter.ViewHolder holder, int position) {
        UserModel selectedItem = list.get(position);
        holder.avataImage.setImageBitmap(null);
        Glide.with(currentActivity).load(selectedItem.getAvatar())
                .into(holder.avataImage);
        holder.userName.setText(selectedItem.getName());
        holder.userCountry.setText(selectedItem.getCountryName());
        holder.userProfile.setText(selectedItem.getProfile());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItemList(ArrayList<UserModel> newList) {
        this.list = (ArrayList<UserModel>) newList.clone();
        notifyDataSetChanged();
    }

    public void addItemList(ArrayList<UserModel> newList) {
        this.list.addAll((ArrayList<UserModel>) newList.clone());
//        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView avataImage;
        TextView userName, userCountry, userProfile;

        ViewHolder(View itemView) {
            super(itemView);
            avataImage = itemView.findViewById(R.id.user_avatar);
            userName = itemView.findViewById(R.id.user_nickname);
            userCountry = itemView.findViewById(R.id.user_country);
            userProfile = itemView.findViewById(R.id.user_profile);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    UserModel getItem(int id) {
        return list.get(id);
    }

    // allows clicks events to be caught
    public void setItemClickListener(FollowRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}