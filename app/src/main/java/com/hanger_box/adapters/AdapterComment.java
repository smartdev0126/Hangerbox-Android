package com.hanger_box.adapters;

import static com.hanger_box.common.Common.currentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.models.ItemModel;
import com.hanger_box.models.LibraryModel;
import com.hanger_box.models.ModelComment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyHolder> {

    Context context;
    ArrayList<ModelComment> list;
    private AdapterComment.ClickListener mClickListener;

    public AdapterComment(Context context, ArrayList<ModelComment> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid = list.get(position).getuid();
        String uavatar = list.get(position).getuavatar();
        String comment = list.get(position).getComment();
        String timestamp = list.get(position).getPtime();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String timedate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        holder.time.setText(timedate);
        holder.comment.setText(comment);
        if(uavatar != null && uavatar != "" && uavatar != "#") {
            Glide.with(context)
                    .load(uavatar)
                    .placeholder(context.getResources().getDrawable(R.drawable.ic_profile_gray))
                    .into(holder.imagea);
        }else {
            holder.imagea.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_profile_gray));
        }
        String repid = list.get(position).getrepid();
        String repuavatar = list.get(position).getrepuavatar();
        String repcomment = list.get(position).getrepComment();
        String reptimestamp = list.get(position).getrepPtime();
        Calendar repcalendar = Calendar.getInstance(Locale.ENGLISH);
        repcalendar.setTimeInMillis(Long.parseLong(reptimestamp));
        String reptimedate = DateFormat.format("dd/MM/yyyy hh:mm aa", repcalendar).toString();
        holder.reptime.setText(reptimedate);
        holder.repcomment.setText(repcomment);
        if(repuavatar != null && repuavatar != "" && repuavatar != "#") {
            Glide.with(context)
                    .load(repuavatar)
                    .placeholder(context.getResources().getDrawable(R.drawable.ic_profile_gray))
                    .into(holder.imagea2);
        }else {
            holder.imagea2.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_profile_gray));
        }

        if(repid.toString().equals("")) {
            holder.rel.setVisibility(View.GONE);
        } else {
            holder.rel.setVisibility(View.VISIBLE);
        }
        String readstatus = list.get(position).getreadstatus();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{

        ImageView imagea, imagea2;
        TextView comment, time, repcomment, reptime;
        RelativeLayout rel;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imagea = itemView.findViewById(R.id.loadcomment);
            imagea2 = itemView.findViewById(R.id.repuavatar);
            comment = itemView.findViewById(R.id.commenttext);
            repcomment = itemView.findViewById(R.id.repcommenttext);
            time = itemView.findViewById(R.id.commenttime);
            reptime = itemView.findViewById(R.id.repcommenttime);
            rel = itemView.findViewById(R.id.replayout);

            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }
        @Override
        public boolean onLongClick(View v) {
            mClickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
        @Override
        public void onClick(View v) {
            mClickListener.onItemClick(getAdapterPosition(), v);
        }
    }
    public interface ClickListener {

        void onItemLongClick(int position, View v);
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }
}

