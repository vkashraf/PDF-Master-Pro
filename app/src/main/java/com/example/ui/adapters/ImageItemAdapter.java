package com.example.ui.adapters;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;

import java.util.List;

public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.ImageViewHolder> {

    public interface OnImageRemoveListener {
        void onRemove(int position);
    }

    private final List<String> imagePaths;
    private final OnImageRemoveListener removeListener;

    public ImageItemAdapter(List<String> imagePaths, OnImageRemoveListener removeListener) {
        this.imagePaths = imagePaths;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_picker, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String path = imagePaths.get(position);
        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(path));
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) removeListener.onRemove(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths != null ? imagePaths.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_picked_image);
            btnRemove = itemView.findViewById(R.id.btn_remove_image);
        }
    }
}
