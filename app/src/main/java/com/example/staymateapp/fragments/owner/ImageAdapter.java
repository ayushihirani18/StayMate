package com.example.staymateapp.fragments.owner;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.staymateapp.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final Context context;
    private final List<Uri> imageList;
    private final Runnable onAddClick;
    private final OnImageRemoveListener removeListener;

    public interface OnImageRemoveListener {
        void onRemove(Uri uri);
    }

    public ImageAdapter(Context context,
                        List<Uri> imageList,
                        Runnable onAddClick,
                        OnImageRemoveListener removeListener) {

        this.context = context;
        this.imageList = imageList;
        this.onAddClick = onAddClick;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_image, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Last item = "+" button
        if (position == imageList.size()) {

            holder.imgPreview.setImageResource(R.drawable.ic_add);
            holder.imgRemove.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                if (onAddClick != null) {
                    onAddClick.run();
                }
            });

        } else {

            Uri uri = imageList.get(position);

            // Use Glide so preview works for local + cloud images
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.ic_home)
                    .into(holder.imgPreview);

            holder.imgRemove.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(null);

            holder.imgRemove.setOnClickListener(v -> {

                int pos = holder.getBindingAdapterPosition();

                if (pos != RecyclerView.NO_POSITION) {

                    Uri removedUri = imageList.get(pos);

                    imageList.remove(pos);
                    notifyItemRemoved(pos);

                    if (removeListener != null) {
                        removeListener.onRemove(removedUri);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size() + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPreview, imgRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPreview = itemView.findViewById(R.id.imgPreview);
            imgRemove = itemView.findViewById(R.id.imgRemove);
        }
    }
}