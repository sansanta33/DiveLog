package com.example.manba;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SpotActivity extends AppCompatActivity {
    private CampusDbHelper dbHelper;
    private SpotAdapter adapter;
    private long userId;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spots);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.spotsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new CampusDbHelper(this);
        userId = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getLong(MainActivity.PREF_USER_ID, -1);
        searchInput = findViewById(R.id.etSpotSearch);
        TextView backButton = findViewById(R.id.btnSpotBack);
        TextView searchButton = findViewById(R.id.btnSpotSearch);
        RecyclerView recyclerView = findViewById(R.id.recyclerSpots);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SpotAdapter();
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());
        searchButton.setOnClickListener(v -> loadSpots());
        loadSpots();
    }

    private void loadSpots() {
        adapter.submitList(dbHelper.searchRecommendedSpots(userId, searchInput.getText().toString()));
    }
}
