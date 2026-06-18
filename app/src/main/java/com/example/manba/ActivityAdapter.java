package com.example.manba;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    public interface OnActivityClickListener {
        void onActivityClick(ActivityItem item);
    }

    private final List<ActivityItem> data = new ArrayList<>();
    private final OnActivityClickListener listener;

    public ActivityAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ActivityItem> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem item = data.get(position);
        holder.title.setText(item.title);
        holder.category.setText(item.category);
        holder.info.setText(item.date + " | " + item.location);
        holder.meta.setText("深度 " + formatDepth(item.depth) + "m  |  时长 " + item.duration + "min  |  能见度 " + item.visibility);
        holder.fish.setText("观察：" + (item.fishSeen == null || item.fishSeen.isEmpty() ? "暂无鱼类记录" : item.fishSeen));
        holder.itemView.setOnClickListener(v -> listener.onActivityClick(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView category;
        TextView info;
        TextView meta;
        TextView fish;

        ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            category = itemView.findViewById(R.id.tvCategory);
            info = itemView.findViewById(R.id.tvInfo);
            meta = itemView.findViewById(R.id.tvOrganizer);
            fish = itemView.findViewById(R.id.tvCapacity);
        }
    }

    private String formatDepth(double depth) {
        if (depth == (long) depth) {
            return String.valueOf((long) depth);
        }
        return String.valueOf(depth);
    }
}
