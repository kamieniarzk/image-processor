package com.example.rt_image_processing;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    ArrayList<Video> videosList;
    Context context;

    public VideoAdapter(Context context, ArrayList<Video> videosList){
        this.context = context;
        this.videosList = videosList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_row, parent, false);
        return new VideoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        final Video item = videosList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.durationTextView.setText(item.getDuration());
        Glide.with(context).load(item.getData()).into(holder.imgView_thumbnail);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), PlayerActivity.class);
            intent.putExtra("videoId", item.getId());
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView_thumbnail;
        TextView titleTextView;
        TextView durationTextView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            imgView_thumbnail = itemView.findViewById(R.id.thumbnailImageView);
        }
    }
}
