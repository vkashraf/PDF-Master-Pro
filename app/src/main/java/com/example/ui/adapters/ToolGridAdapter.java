package com.example.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.model.PdfOperation;

import java.util.List;

public class ToolGridAdapter extends RecyclerView.Adapter<ToolGridAdapter.ToolViewHolder> {

    public interface OnToolClickListener {
        void onToolClick(PdfOperation operation);
    }

    private final List<PdfOperation> operations;
    private final OnToolClickListener listener;

    public ToolGridAdapter(List<PdfOperation> operations, OnToolClickListener listener) {
        this.operations = operations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tool_card, parent, false);
        return new ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        PdfOperation op = operations.get(position);
        holder.title.setText(op.getTitle());
        holder.description.setText(op.getDescription());
        holder.icon.setImageResource(op.getIconResId());
        holder.proBadge.setVisibility(op.isPro() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onToolClick(op);
        });
    }

    @Override
    public int getItemCount() {
        return operations != null ? operations.size() : 0;
    }

    static class ToolViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView description;
        ImageView proBadge;

        public ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_tool_icon);
            title = itemView.findViewById(R.id.tv_tool_title);
            description = itemView.findViewById(R.id.tv_tool_desc);
            proBadge = itemView.findViewById(R.id.iv_pro_badge);
        }
    }
}
