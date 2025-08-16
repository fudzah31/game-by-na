package com.example.gamebyna;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME       = "app_settings";
    private static final String KEY_CURRENT_USER = "current_user";

    private EditText etUsername, etPassword;
    private Button btnLogin, btnSignup;
    private koneksi db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi database & SharedPreferences
        db    = new koneksi(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        etUsername = findViewById(R.id.usernamelogin);
        etPassword = findViewById(R.id.passwordlogin);
        btnLogin   = findViewById(R.id.btnlogin);
        btnSignup  = findViewById(R.id.btnsignup);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (db.checkUser(username, password)) {
                // Simpan username ke prefs agar bisa dipakai di MainActivity maupun Leaderboard
                prefs.edit()
                        .putString(KEY_CURRENT_USER, username)
                        .apply();

                if ("admin".equalsIgnoreCase(username)) {
                    // Jika admin, langsung ke LeaderboardActivity
                    startActivity(new Intent(LoginActivity.this, LeaderboardActivity.class));
                } else {
                    // Bukan admin, ke MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "Login gagal: periksa username/password", Toast.LENGTH_SHORT).show();
            }
        });

        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class))
        );
    }
}
