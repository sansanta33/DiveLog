package com.example.manba;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "user_id";
    public static final String PREFS = "dive_prefs";
    public static final String PREF_USER_ID = "current_user_id";

    private DiveDbHelper dbHelper;
    private long userId;
    private TextView subtitleView;
    private TextView recommendationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DiveDbHelper(this);
        userId = getSharedPreferences(PREFS, MODE_PRIVATE).getLong(PREF_USER_ID, -1);
        if (userId <= 0) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        TextView titleView = findViewById(R.id.tvHomeTitle);
        subtitleView = findViewById(R.id.tvHomeSubtitle);
        recommendationView = findViewById(R.id.tvRecommendation);
        titleView.setText("你好，" + dbHelper.getUsername(userId));

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager.setAdapter(new ActivityPagerAdapter(this, userId));
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(ActivityPagerAdapter.TABS[position])).attach();

        TextView publishButton = findViewById(R.id.btnPublish);
        TextView notesButton = findViewById(R.id.btnNotes);
        TextView spotsButton = findViewById(R.id.btnSpots);
        TextView fishButton = findViewById(R.id.btnFishIdentify);
        TextView logoutButton = findViewById(R.id.btnLogout);
        publishButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(EXTRA_USER_ID, userId);
            startActivity(intent);
        });
        notesButton.setOnClickListener(v -> startActivity(new Intent(this, NoteActivity.class)));
        spotsButton.setOnClickListener(v -> startActivity(new Intent(this, SpotActivity.class)));
        fishButton.setOnClickListener(v -> startActivity(new Intent(this, FishIdentifyActivity.class)));
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
            editor.remove(PREF_USER_ID);
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null && userId > 0) {
            int diveCount = dbHelper.countDives(userId);
            int noteCount = dbHelper.countNotes(userId);
            subtitleView.setText("已记录 " + diveCount + " 次潜水，" + noteCount + " 条复盘笔记");
            recommendationView.setText(dbHelper.buildHomeRecommendation(userId));
        }
    }
}
