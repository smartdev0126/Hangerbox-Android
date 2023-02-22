package com.hanger_box.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.models.ItemModel;
import com.hanger_box.models.LibraryModel;
import com.hanger_box.models.ModelComment;
import com.hanger_box.utils.GlideImageLoader;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class LibrariesRecyclerViewAdapter extends RecyclerView.Adapter<LibrariesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<LibraryModel> list;
    private LayoutInflater mInflater;
    private LibrariesRecyclerViewAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public LibrariesRecyclerViewAdapter(Context context, ArrayList<LibraryModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.list = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public LibrariesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cell, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull LibrariesRecyclerViewAdapter.ViewHolder holder, int position) {
        LibraryModel selectedItem = list.get(position);
        holder.itemImage.setImageBitmap(null);
        if (selectedItem.getCodiImage1() != null) {
            RequestOptions options = new RequestOptions()
                    .priority(Priority.HIGH);

            holder.loadingProgress.setVisibility(View.VISIBLE);
            new GlideImageLoader(holder.itemImage,
                    holder.loadingProgress).load(selectedItem.getCodiImage1(), options);
        }else {
            holder.loadingProgress.setVisibility(View.GONE);
        }

        holder.rel.setBackground(mInflater.getContext().getResources().getDrawable(R.drawable.border_text));

        if(Common.me.getId().equals(list.get(position).getUserId())) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Libraries").child(list.get(position).getId()).child("Comments");
            Query query = reference.orderByChild("ptime");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        ModelComment modelComment = dataSnapshot1.getValue(ModelComment.class);
                        if(modelComment.getreadstatus().equals("0")) {
                            holder.rel.setBackground(mInflater.getContext().getResources().getDrawable(R.drawable.unreadstatus));
                        } else {
                            holder.rel.setBackground(mInflater.getContext().getResources().getDrawable(R.drawable.border_text));
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

    }

    // total number of cells
    @Override
    public int getItemCount() {
        if (list != null)
            return list.size();
        else
            return 0;
    }

    public void updateItemList(ArrayList<LibraryModel> newList) {
        this.list = (ArrayList<LibraryModel>) newList.clone();
        notifyDataSetChanged();
    }

    public void addItemList(ArrayList<ItemModel> newList) {
        this.list.addAll((ArrayList<LibraryModel>) newList.clone());
 //       notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView itemImage;
        ProgressBar loadingProgress;
        RelativeLayout rel;

        ViewHolder(View itemView) {
            super(itemView);
            rel = itemView.findViewById(R.id.library_item_layout);
            itemImage = itemView.findViewById(R.id.item_image);
            loadingProgress = itemView.findViewById(R.id.loading_progress);
            loadingProgress.setVisibility(View.GONE);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public LibraryModel getItem(int id) {
        return list.get(id);
    }

    // allows clicks events to be caught
    public void setItemClickListener(LibrariesRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}