package com.example.manba;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ActivityPagerAdapter extends FragmentStateAdapter {
    public static final String[] TABS = {"全部", "休闲潜水", "训练潜水", "自由潜", "深潜"};

    private final long userId;

    public ActivityPagerAdapter(@NonNull FragmentActivity fragmentActivity, long userId) {
        super(fragmentActivity);
        this.userId = userId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ActivityListFragment.newInstance(TABS[position], userId);
    }

    @Override
    public int getItemCount() {
        return TABS.length;
    }
}
