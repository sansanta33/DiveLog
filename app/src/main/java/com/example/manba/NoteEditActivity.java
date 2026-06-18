package com.example.manba;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NoteEditActivity extends AppCompatActivity {
    public static final String EXTRA_NOTE_ID = "note_id";
    public static final String EXTRA_DIVE_ID = "dive_id";
    public static final String EXTRA_DIVE_TITLE = "dive_title";

    private DiveDbHelper dbHelper;
    private long userId;
    private long noteId;
    private long diveId;
    private EditText titleInput;
    private EditText contentInput;
    private Spinner moodSpinner;
    private TextView deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.noteEditRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DiveDbHelper(this);
        userId = getIntent().getLongExtra(MainActivity.EXTRA_USER_ID, -1);
        noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
        diveId = getIntent().getLongExtra(EXTRA_DIVE_ID, 0);

        titleInput = findViewById(R.id.etNoteTitle);
        contentInput = findViewById(R.id.etNoteContent);
        moodSpinner = findViewById(R.id.spinnerMood);
        deleteButton = findViewById(R.id.btnDeleteNote);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_spinner,
                new String[]{"轻松", "兴奋", "紧张", "需要复盘", "已掌握"});
        adapter.setDropDownViewResource(R.layout.item_spinner);
        moodSpinner.setAdapter(adapter);

        TextView backButton = findViewById(R.id.btnNoteBack);
        TextView saveButton = findViewById(R.id.btnSaveNote);
        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveNote());
        deleteButton.setOnClickListener(v -> confirmDelete());

        loadNoteIfNeeded();
    }

    private void loadNoteIfNeeded() {
        if (noteId > 0) {
            NoteItem item = dbHelper.getNote(noteId);
            if (item == null) {
                finish();
                return;
            }
            titleInput.setText(item.title);
            contentInput.setText(item.content);
            setMood(item.mood);
            deleteButton.setVisibility(View.VISIBLE);
            return;
        }
        String diveTitle = getIntent().getStringExtra(EXTRA_DIVE_TITLE);
        if (!TextUtils.isEmpty(diveTitle)) {
            titleInput.setText(diveTitle + "复盘");
            contentInput.setText("本次潜水重点：\n\n需要改进：\n\n下次计划：");
        }
        deleteButton.setVisibility(View.GONE);
    }

    private void saveNote() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();
        String mood = moodSpinner.getSelectedItem().toString();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请填写标题和内容", Toast.LENGTH_SHORT).show();
            return;
        }
        if (noteId > 0) {
            dbHelper.updateNote(noteId, title, content, mood);
            Toast.makeText(this, "笔记已更新", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addNote(userId, diveId, title, content, mood);
            Toast.makeText(this, "笔记已保存", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("删除笔记")
                .setMessage("确定要删除这条笔记吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    dbHelper.deleteNote(noteId);
                    Toast.makeText(this, "笔记已删除", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private void setMood(String mood) {
        for (int i = 0; i < moodSpinner.getCount(); i++) {
            if (moodSpinner.getItemAtPosition(i).toString().equals(mood)) {
                moodSpinner.setSelection(i);
                return;
            }
        }
    }
}
