package com.example.gamebyna;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity {
    private static final String PREFS_NAME        = "app_settings";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    private static final String KEY_MUSIC_VOLUME  = "music_volume";

    private ImageView ivSusunKata, ivTebakGambar, ivMatematika, ivTebakNegara;
    private Button btnKeluar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Edge‑to‑edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Bind ImageViews sesuai XML
        ivSusunKata    = findViewById(R.id.susunkata);
        ivTebakGambar  = findViewById(R.id.tebakgambar);
        ivMatematika   = findViewById(R.id.matematika);
        ivTebakNegara  = findViewById(R.id.tebaknegara);
        btnKeluar      = findViewById(R.id.btnKeluar);

        // Set click listener untuk tiap game mode
        ivSusunKata.setOnClickListener(v -> startActivity(new Intent(this, susunkataActivity.class)));
        ivTebakGambar.setOnClickListener(v -> startActivity(new Intent(this, tebakgambarActivity.class)));
        ivMatematika.setOnClickListener(v -> startActivity(new Intent(this, matematikaActivity.class)));
        ivTebakNegara.setOnClickListener(v -> startActivity(new Intent(this, tebaknegaraActivity.class)));

        // Tombol Keluar → kembali ke MainActivity
        btnKeluar.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
        float volume    = prefs.getInt(KEY_MUSIC_VOLUME, 50) / 100f;

        if (enabled) MusicManager.getInstance().play(volume);
        else         MusicManager.getInstance().pause();
    }
}
