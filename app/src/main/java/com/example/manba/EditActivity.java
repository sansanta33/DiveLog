package com.example.manba;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditActivity extends AppCompatActivity {
    private DiveDbHelper dbHelper;
    private long userId;
    private EditText titleInput;
    private EditText locationInput;
    private EditText dateInput;
    private EditText depthInput;
    private EditText durationInput;
    private EditText visibilityInput;
    private EditText waterTempInput;
    private EditText buddyInput;
    private EditText fishInput;
    private EditText noteInput;
    private Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DiveDbHelper(this);
        userId = getIntent().getLongExtra(MainActivity.EXTRA_USER_ID, -1);
        titleInput = findViewById(R.id.etTitle);
        locationInput = findViewById(R.id.etLocation);
        dateInput = findViewById(R.id.etDate);
        depthInput = findViewById(R.id.etQuota);
        durationInput = findViewById(R.id.etOrganizer);
        visibilityInput = findViewById(R.id.etVisibility);
        waterTempInput = findViewById(R.id.etWaterTemp);
        buddyInput = findViewById(R.id.etBuddy);
        fishInput = findViewById(R.id.etFish);
        noteInput = findViewById(R.id.etDescription);
        categorySpinner = findViewById(R.id.spinnerCategory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_spinner,
                new String[]{"休闲潜水", "训练潜水", "自由潜", "夜潜"});
        adapter.setDropDownViewResource(R.layout.item_spinner);
        categorySpinner.setAdapter(adapter);

        TextView backButton = findViewById(R.id.btnEditBack);
        TextView saveButton = findViewById(R.id.btnSave);
        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveDive());
    }

    private void saveDive() {
        String title = titleInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String depthText = depthInput.getText().toString().trim();
        String durationText = durationInput.getText().toString().trim();
        String visibility = visibilityInput.getText().toString().trim();
        String waterTemp = waterTempInput.getText().toString().trim();
        String buddy = buddyInput.getText().toString().trim();
        String fish = fishInput.getText().toString().trim();
        String note = noteInput.getText().toString().trim();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location) || TextUtils.isEmpty(date)
                || TextUtils.isEmpty(depthText) || TextUtils.isEmpty(durationText) || TextUtils.isEmpty(note)) {
            Toast.makeText(this, "请至少填写标题、地点、日期、深度、时长和笔记", Toast.LENGTH_SHORT).show();
            return;
        }
        double depth;
        int duration;
        try {
            depth = Double.parseDouble(depthText);
            duration = Integer.parseInt(durationText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "深度和时长需要填写数字", Toast.LENGTH_SHORT).show();
            return;
        }
        if (depth <= 0 || duration <= 0) {
            Toast.makeText(this, "深度和时长必须大于 0", Toast.LENGTH_SHORT).show();
            return;
        }
        dbHelper.addActivity(userId, title, categorySpinner.getSelectedItem().toString(), location, date,
                depth, duration, valueOrDefault(visibility, "未记录"), valueOrDefault(waterTemp, "未记录"),
                valueOrDefault(buddy, "独自记录"), valueOrDefault(fish, "无"), note);
        Toast.makeText(this, "潜水记录已保存", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String valueOrDefault(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }
}
