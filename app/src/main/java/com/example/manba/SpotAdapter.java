package com.example.manba;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.SpotViewHolder> {
    private final List<SpotItem> data = new ArrayList<>();

    public void submitList(List<SpotItem> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spot, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        SpotItem item = data.get(position);
        holder.title.setText(item.title);
        holder.meta.setText(item.location + " | " + item.diveType + " | 推荐度 " + item.score + "%");
        holder.detail.setText("建议深度 " + formatDepth(item.depth) + "m  |  能见度 " + item.visibility + "\n观察：" + item.fishSeen);
        holder.reason.setText(item.reason);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class SpotViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;
        TextView detail;
        TextView reason;

        SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvSpotTitle);
            meta = itemView.findViewById(R.id.tvSpotMeta);
            detail = itemView.findViewById(R.id.tvSpotDetail);
            reason = itemView.findViewById(R.id.tvSpotReason);
        }
    }

    private String formatDepth(double depth) {
        if (depth == (long) depth) {
            return String.valueOf((long) depth);
        }
        return String.valueOf(depth);
    }
}
