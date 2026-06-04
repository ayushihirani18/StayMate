package com.example.staymateapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.staymateapp.R;

import java.util.List;

public class ImageSliderAdapter
        extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private final List<String> imageUrls;
    private boolean isFullScreen;

    public ImageSliderAdapter(List<String> imageUrls, boolean isFullScreen) {
        this.imageUrls = imageUrls;
        this.isFullScreen = isFullScreen;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        int layout = isFullScreen
                ? R.layout.item_fullscreen_image
                : R.layout.item_slider_image;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ImageViewHolder holder, int position) {

        Glide.with(holder.itemView.getContext())
                .load(imageUrls.get(position))
                .placeholder(R.drawable.ic_home)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {

            android.content.Context context = holder.itemView.getContext();

            android.content.Intent intent =
                    new android.content.Intent(context, com.example.staymateapp.FullScreenImageActivity.class);

            intent.putStringArrayListExtra(
                    "imageUrls",
                    new java.util.ArrayList<>(imageUrls)
            );

            // 🔥 PASS CLICKED POSITION
            intent.putExtra("position", position);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls == null ? 0 : imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sliderImage);
        }
    }
}