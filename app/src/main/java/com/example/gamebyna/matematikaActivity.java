package com.example.gamebyna;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class matematikaActivity extends AppCompatActivity {

    private static final String PREF_NAME         = "level_status_mt"; // khusus Matematika
    private static final String SETTING_PREF      = "setting_pref";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    private static final String KEY_MUSIC_VOLUME  = "music_volume";
    private static final String KEY_USER_ID       = "current_user_id"; // DITAMBAHKAN

    private static final String[] LEVEL_KEYS = {
            "level1", "level2", "level3", "level4", "level5"
    };

    private static final int[] UNLOCKED_DRAWABLES = {
            R.drawable.lev1sk,
            R.drawable.lev2sk,
            R.drawable.lev3sk,
            R.drawable.lev4sk,
            R.drawable.lev5sk
    };

    private ImageView[] levelButtons = new ImageView[5];
    private SharedPreferences levelPrefs;
    private SharedPreferences settingPrefs;
    private SharedPreferences userPrefs; // DITAMBAHKAN
    private int currentUserId;           // DITAMBAHKAN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matematika);

        // Ambil user ID dari SharedPreferences global
        userPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE); // DITAMBAHKAN
        currentUserId = userPrefs.getInt(KEY_USER_ID, -1); // DITAMBAHKAN

        // SharedPreferences khusus untuk user saat ini
        String levelPrefName = PREF_NAME + "_user_" + currentUserId; // DITAMBAHKAN
        levelPrefs = getSharedPreferences(levelPrefName, Context.MODE_PRIVATE); // DITAMBAHKAN

        settingPrefs = getSharedPreferences(SETTING_PREF, Context.MODE_PRIVATE);

        // Musik
        if (settingPrefs.getBoolean(KEY_MUSIC_ENABLED, true)) {
            float vf = settingPrefs.getInt(KEY_MUSIC_VOLUME, 50) / 100f;
            MusicManager.getInstance().init(this, R.raw.music, true);
            MusicManager.getInstance().play(vf);
        }

        // Tombol kembali
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Tombol level
        levelButtons[0] = findViewById(R.id.btnLevel1);
        levelButtons[1] = findViewById(R.id.btnLevel2);
        levelButtons[2] = findViewById(R.id.btnLevel3);
        levelButtons[3] = findViewById(R.id.btnLevel4);
        levelButtons[4] = findViewById(R.id.btnLevel5);

        setupLevelButtons();
    }

    private void setupLevelButtons() {
        for (int i = 0; i < levelButtons.length; i++) {
            final int idx = i;
            boolean unlocked = (i == 0) || levelPrefs.getBoolean(LEVEL_KEYS[i], false);

            if (unlocked) {
                levelButtons[i].setImageResource(UNLOCKED_DRAWABLES[i]);
                levelButtons[i].setOnClickListener(v -> openLevel(idx));
            } else {
                levelButtons[i].setImageResource(R.drawable.ic_lock);
                levelButtons[i].setOnClickListener(null);
            }
        }
    }

    private void openLevel(int index) {
        Intent intent;
        switch (index) {
            case 0: intent = new Intent(this, Level1MTActivity.class); break;
            case 1: intent = new Intent(this, Level2MTActivity.class); break;
            case 2: intent = new Intent(this, Level3MTActivity.class); break;
            case 3: intent = new Intent(this, Level4MTActivity.class); break;
            case 4: intent = new Intent(this, Level5MTActivity.class); break;
            default: return;
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupLevelButtons();
        boolean musicOn = settingPrefs.getBoolean(KEY_MUSIC_ENABLED, true);
        float vf = settingPrefs.getInt(KEY_MUSIC_VOLUME, 50) / 100f;
        if (musicOn) MusicManager.getInstance().play(vf);
        else MusicManager.getInstance().pause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.getInstance().pause();
    }
}
