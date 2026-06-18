package com.example.manba;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    private DiveDbHelper dbHelper;
    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DiveDbHelper(this);
        usernameInput = findViewById(R.id.etUsername);
        passwordInput = findViewById(R.id.etPassword);
        TextView loginButton = findViewById(R.id.btnLogin);
        TextView registerButton = findViewById(R.id.btnRegister);
        TextView demoButton = findViewById(R.id.btnDemo);

        loginButton.setOnClickListener(v -> login());
        registerButton.setOnClickListener(v -> register());
        demoButton.setOnClickListener(v -> {
            usernameInput.setText("demo");
            passwordInput.setText("123456");
            login();
        });
    }

    private void login() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        long userId = dbHelper.login(username, password);
        if (userId <= 0) {
            Toast.makeText(this, "账号或密码不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        enterApp(userId);
    }

    private void register() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        if (username.length() < 2 || password.length() < 6) {
            Toast.makeText(this, "账号至少 2 位，密码至少 6 位", Toast.LENGTH_SHORT).show();
            return;
        }
        long userId = dbHelper.register(username, password);
        if (userId <= 0) {
            Toast.makeText(this, "账号已存在，请换一个", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
        enterApp(userId);
    }

    private void enterApp(long userId) {
        SharedPreferences.Editor editor = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit();
        editor.putLong(MainActivity.PREF_USER_ID, userId);
        editor.apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
