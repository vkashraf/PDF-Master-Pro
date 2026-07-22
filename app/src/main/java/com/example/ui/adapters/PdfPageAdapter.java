package com.example.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.model.PdfPageModel;

import java.util.List;

public class PdfPageAdapter extends RecyclerView.Adapter<PdfPageAdapter.PageViewHolder> {

    public interface OnPageClickListener {
        void onPageClick(PdfPageModel page, int position);
    }

    private final List<PdfPageModel> pages;
    private final OnPageClickListener listener;

    public PdfPageAdapter(List<PdfPageModel> pages, OnPageClickListener listener) {
        this.pages = pages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        PdfPageModel page = pages.get(position);
        holder.pageNumber.setText("Page " + (page.getPageIndex() + 1));
        if (page.getPageBitmap() != null) {
            holder.pageImage.setImageBitmap(page.getPageBitmap());
        }

        holder.overlaySelection.setVisibility(page.isSelected() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPageClick(page, position);
        });
    }

    @Override
    public int getItemCount() {
        return pages != null ? pages.size() : 0;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView pageImage;
        TextView pageNumber;
        View overlaySelection;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            pageImage = itemView.findViewById(R.id.iv_page_image);
            pageNumber = itemView.findViewById(R.id.tv_page_number);
            overlaySelection = itemView.findViewById(R.id.view_selection_overlay);
        }
    }
}
