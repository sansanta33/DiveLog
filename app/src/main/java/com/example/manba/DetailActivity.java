package com.example.manba;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_ACTIVITY_ID = "activity_id";

    private DiveDbHelper dbHelper;
    private long activityId;
    private ActivityItem activityItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DiveDbHelper(this);
        activityId = getIntent().getLongExtra(EXTRA_ACTIVITY_ID, -1);
        loadActivity();

        TextView backButton = findViewById(R.id.btnBack);
        TextView noteButton = findViewById(R.id.btnJoin);
        backButton.setOnClickListener(v -> finish());
        noteButton.setOnClickListener(v -> {
            if (activityItem == null) {
                return;
            }
            long userId = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                    .getLong(MainActivity.PREF_USER_ID, -1);
            Intent intent = new Intent(this, NoteEditActivity.class);
            intent.putExtra(MainActivity.EXTRA_USER_ID, userId);
            intent.putExtra(NoteEditActivity.EXTRA_DIVE_ID, activityItem.id);
            intent.putExtra(NoteEditActivity.EXTRA_DIVE_TITLE, activityItem.title);
            startActivity(intent);
        });
    }

    private void loadActivity() {
        activityItem = dbHelper.getActivity(activityId);
        if (activityItem == null) {
            finish();
            return;
        }
        TextView title = findViewById(R.id.tvDetailTitle);
        TextView category = findViewById(R.id.tvDetailCategory);
        TextView time = findViewById(R.id.tvDetailTime);
        TextView location = findViewById(R.id.tvDetailLocation);
        TextView buddy = findViewById(R.id.tvDetailOrganizer);
        TextView note = findViewById(R.id.tvDetailDescription);
        TextView metrics = findViewById(R.id.tvDetailCapacity);
        TextView aiTip = findViewById(R.id.tvAiTip);

        title.setText(activityItem.title);
        category.setText(activityItem.category);
        time.setText("日期：" + activityItem.date);
        location.setText("地点：" + activityItem.location);
        buddy.setText("潜伴：" + activityItem.buddy + "  |  水温：" + activityItem.waterTemp);
        metrics.setText("深度 " + activityItem.depth + "m  |  时长 " + activityItem.duration +
                "min  |  能见度 " + activityItem.visibility + "\n观察：" + activityItem.fishSeen);
        note.setText(activityItem.description);
        aiTip.setText(activityItem.aiTip);
    }
}
