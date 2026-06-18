package com.example.manba;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivityListFragment extends Fragment {
    private static final String ARG_FILTER = "filter";
    private static final String ARG_USER_ID = "user_id";

    private String filter;
    private long userId;
    private CampusDbHelper dbHelper;
    private ActivityAdapter adapter;
    private TextView emptyView;

    public static ActivityListFragment newInstance(String filter, long userId) {
        ActivityListFragment fragment = new ActivityListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, filter);
        args.putLong(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        filter = requireArguments().getString(ARG_FILTER, "全部");
        userId = requireArguments().getLong(ARG_USER_ID, -1);
        dbHelper = new CampusDbHelper(requireContext());
        emptyView = view.findViewById(R.id.tvEmpty);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ActivityAdapter(item -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_ACTIVITY_ID, item.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (dbHelper == null || adapter == null) {
            return;
        }
        List<ActivityItem> items = dbHelper.getActivities(userId, filter);
        adapter.submitList(items);
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
