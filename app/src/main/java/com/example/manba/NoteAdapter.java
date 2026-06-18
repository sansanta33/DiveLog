package com.example.manba;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    public interface OnNoteClickListener {
        void onNoteClick(NoteItem item);
    }

    private final List<NoteItem> data = new ArrayList<>();
    private final OnNoteClickListener listener;

    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NoteItem> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteItem item = data.get(position);
        holder.title.setText(item.title);
        holder.meta.setText(item.mood + " | 更新于 " + item.updatedAt);
        holder.content.setText(item.content);
        holder.itemView.setOnClickListener(v -> listener.onNoteClick(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;
        TextView content;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvNoteTitle);
            meta = itemView.findViewById(R.id.tvNoteMeta);
            content = itemView.findViewById(R.id.tvNoteContent);
        }
    }
}
