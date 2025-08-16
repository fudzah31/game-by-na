// MainActivity.java
package com.example.gamebyna;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME        = "app_settings";
    private static final String KEY_MUSIC_VOLUME  = "music_volume";
    private static final String KEY_SOUND_VOLUME  = "sound_volume";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_CURRENT_USER  = "current_user";

    private koneksi db;
    private TextView nameBoard, scoreBoard;
    private SharedPreferences prefs;

    private SoundPool soundPool;
    private int soundWin, soundLose;
    private int currentUserId; // ← tambahan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Setup insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Inisialisasi db & prefs
        db    = new koneksi(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        nameBoard  = findViewById(R.id.name);
        scoreBoard = findViewById(R.id.poin);

        loadUserAndScore();
        storeCurrentUserId(); // ← tambahan

        // Inisiasi musik (tanpa auto-play)
        MusicManager mgr = MusicManager.getInstance();
        mgr.init(this, R.raw.music, true);
        applyMusicSettings();

        initSoundEffects();

        findViewById(R.id.play).setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, DashboardActivity.class);
            // ↓ kirim user ID ke dashboard
            i.putExtra("USER_ID", currentUserId);
            startActivity(i);
        });
        findViewById(R.id.leaderboard).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LeaderboardActivity.class))
        );
        findViewById(R.id.setting).setOnClickListener(v -> showSettingDialog());
        findViewById(R.id.exit).setOnClickListener(v -> showExitDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserAndScore();
        storeCurrentUserId(); // ← tambahan
        applyMusicSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    /** Load username & poin dari prefs/db */
    private void loadUserAndScore() {
        String user = prefs.getString(KEY_CURRENT_USER, "");
        nameBoard.setText(user.isEmpty() ? "Guest" : user);
        int pts = db.getTotalPointsForUser(user);
        scoreBoard.setText(String.valueOf(pts));
    }

    /** Simpan user ID ke SharedPreferences */
    private void storeCurrentUserId() {
        String user = prefs.getString(KEY_CURRENT_USER, "");
        if (!user.isEmpty()) {
            currentUserId = db.getUserId(user);
            prefs.edit().putInt("current_user_id", currentUserId).apply();
        }
    }

    /** Play atau pause musik sesuai setting */
    private void applyMusicSettings() {
        MusicManager mgr = MusicManager.getInstance();
        boolean musicOn = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
        float vol = prefs.getInt(KEY_MUSIC_VOLUME, 50) / 100f;
        if (musicOn) mgr.play(vol);
        else mgr.pause();
    }

    /** Siapkan efek suara menang/kalah */
    private void initSoundEffects() {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attrs)
                .build();
        soundWin  = soundPool.load(this, R.raw.tepuk, 1);
        soundLose = soundPool.load(this, R.raw.sedih, 1);
    }

    public void playWin() {
        if (prefs.getBoolean(KEY_SOUND_ENABLED, true) && soundPool != null) {
            float vol = prefs.getInt(KEY_SOUND_VOLUME, 50) / 100f;
            soundPool.play(soundWin, vol, vol, 1, 0, 1f);
        }
    }

    public void playLose() {
        if (prefs.getBoolean(KEY_SOUND_ENABLED, true) && soundPool != null) {
            float vol = prefs.getInt(KEY_SOUND_VOLUME, 50) / 100f;
            soundPool.play(soundLose, vol, vol, 1, 0, 1f);
        }
    }

    @SuppressLint("InflateParams")
    private void showSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dlg = LayoutInflater.from(this).inflate(R.layout.activity_setting, null);

        ImageButton btnMusic = dlg.findViewById(R.id.btn_music);
        SeekBar sbMusic      = dlg.findViewById(R.id.seekbar_music);
        ImageButton btnSound = dlg.findViewById(R.id.btn_sound);
        SeekBar sbSound      = dlg.findViewById(R.id.seekbar_sound);
        Button btnOk         = dlg.findViewById(R.id.btn_ok);

        AtomicBoolean musicEnabled = new AtomicBoolean(
                prefs.getBoolean(KEY_MUSIC_ENABLED, true));
        AtomicBoolean soundEnabled = new AtomicBoolean(
                prefs.getBoolean(KEY_SOUND_ENABLED, true));
        int musVol = prefs.getInt(KEY_MUSIC_VOLUME, 50);
        int sndVol = prefs.getInt(KEY_SOUND_VOLUME, 50);

        sbMusic.setProgress(musVol);
        sbSound.setProgress(sndVol);
        btnMusic.setAlpha(musicEnabled.get() ? 1f : 0.4f);
        btnSound.setAlpha(soundEnabled.get() ? 1f : 0.4f);

        btnMusic.setOnClickListener(v -> {
            boolean enabled = musicEnabled.getAndSet(!musicEnabled.get());
            prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply();
            btnMusic.setAlpha(enabled ? 1f : 0.4f);
            applyMusicSettings();
        });
        sbMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                prefs.edit().putInt(KEY_MUSIC_VOLUME, p).apply();
                applyMusicSettings();
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        btnSound.setOnClickListener(v -> {
            boolean enabled = soundEnabled.getAndSet(!soundEnabled.get());
            prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
            btnSound.setAlpha(enabled ? 1f : 0.4f);
        });
        sbSound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                prefs.edit().putInt(KEY_SOUND_VOLUME, p).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        builder.setView(dlg);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        btnOk.setOnClickListener(v -> dialog.dismiss());
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dlg = LayoutInflater.from(this).inflate(R.layout.activity_exit, null);
        Button iya   = dlg.findViewById(R.id.btn_iya);
        Button tidak = dlg.findViewById(R.id.btn_tidak);

        builder.setView(dlg);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        iya.setOnClickListener(v -> {
            dialog.dismiss();
            // Kembali ke LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            // Optional: hapus back stack supaya user tidak bisa kembali ke MainActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        tidak.setOnClickListener(v -> dialog.dismiss());
    }

}
