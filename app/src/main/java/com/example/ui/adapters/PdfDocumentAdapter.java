package com.example.ui.adapters;

import android.graphics.Bitmap;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.model.PdfDocumentEntity;
import com.example.engine.PdfEngine;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

public class PdfDocumentAdapter extends RecyclerView.Adapter<PdfDocumentAdapter.PdfViewHolder> {

    public interface OnPdfItemClickListener {
        void onPdfClick(PdfDocumentEntity entity);
        void onFavoriteClick(PdfDocumentEntity entity);
        void onDeleteClick(PdfDocumentEntity entity);
        void onShareClick(PdfDocumentEntity entity);
    }

    private List<PdfDocumentEntity> pdfList;
    private final OnPdfItemClickListener listener;

    public PdfDocumentAdapter(List<PdfDocumentEntity> pdfList, OnPdfItemClickListener listener) {
        this.pdfList = pdfList;
        this.listener = listener;
    }

    public void setPdfList(List<PdfDocumentEntity> pdfList) {
        this.pdfList = pdfList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_document, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        PdfDocumentEntity doc = pdfList.get(position);
        holder.fileName.setText(doc.getFileName());
        holder.fileSize.setText(Formatter.formatShortFileSize(holder.itemView.getContext(), doc.getFileSize()));
        holder.pageCount.setText(doc.getPageCount() + " Pages");

        holder.btnFavorite.setImageResource(doc.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

        // Async Thumbnail Load
        File file = new File(doc.getFilePath());
        if (file.exists()) {
            Executors.newSingleThreadExecutor().execute(() -> {
                Bitmap thumbnail = PdfEngine.renderPage(file, 0, 150);
                if (thumbnail != null) {
                    holder.itemView.post(() -> holder.thumbnail.setImageBitmap(thumbnail));
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPdfClick(doc);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(doc);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(doc);
        });

        holder.btnShare.setOnClickListener(v -> {
            if (listener != null) listener.onShareClick(doc);
        });
    }

    @Override
    public int getItemCount() {
        return pdfList != null ? pdfList.size() : 0;
    }

    static class PdfViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView fileName;
        TextView fileSize;
        TextView pageCount;
        ImageButton btnFavorite;
        ImageButton btnDelete;
        ImageButton btnShare;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.iv_pdf_thumbnail);
            fileName = itemView.findViewById(R.id.tv_pdf_name);
            fileSize = itemView.findViewById(R.id.tv_pdf_size);
            pageCount = itemView.findViewById(R.id.tv_pdf_pages);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnShare = itemView.findViewById(R.id.btn_share);
        }
    }
}
