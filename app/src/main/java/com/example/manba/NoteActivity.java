package com.example.manba;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private DiveDbHelper dbHelper;
    private long userId;
    private NoteAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notesRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DiveDbHelper(this);
        userId = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getLong(MainActivity.PREF_USER_ID, -1);
        emptyView = findViewById(R.id.tvNotesEmpty);
        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(item -> {
            Intent intent = new Intent(this, NoteEditActivity.class);
            intent.putExtra(NoteEditActivity.EXTRA_NOTE_ID, item.id);
            intent.putExtra(MainActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        TextView backButton = findViewById(R.id.btnNotesBack);
        TextView addButton = findViewById(R.id.btnAddNote);
        backButton.setOnClickListener(v -> finish());
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteEditActivity.class);
            intent.putExtra(MainActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<NoteItem> notes = dbHelper.getNotes(userId);
        adapter.submitList(notes);
        emptyView.setVisibility(notes.isEmpty() ? TextView.VISIBLE : TextView.GONE);
    }
}
