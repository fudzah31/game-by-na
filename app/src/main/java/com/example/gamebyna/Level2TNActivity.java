package com.example.gamebyna;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Level2TNActivity extends AppCompatActivity {
    private static final long   TOTAL_TIME_MS = 30_000;
    private static final String PREFS_NAME    = "app_settings";
    private static final String KEY_USER_ID   = "current_user_id";

    // SharedPreferences untuk menu Tebak Negara
    private static final String TN_PREFS   = "level_status_tn";
    private static final String KEY_LEVEL3 = "level3";

    private ImageButton btnImage1, btnImage2, btnImage3;
    private Button      btnPause, btnCheck;
    private TextView    textTimer, textScore, textWord;

    private CountDownTimer timer;
    private long           timeRemaining = TOTAL_TIME_MS;
    private int            score         = 0;
    private int            selectedRes   = -1;

    private MusicManager      musicManager;
    private koneksi           db;
    private int               currentUserId;
    private SharedPreferences userPrefs, tnPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level2_tnactivity);  // RelativeLayout XML yang Anda berikan

        // SharedPreferences
        userPrefs     = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        tnPrefs       = getSharedPreferences(TN_PREFS, Context.MODE_PRIVATE);
        currentUserId = userPrefs.getInt(KEY_USER_ID, -1);

        // Bind views
        btnPause  = findViewById(R.id.btn_pause);
        btnCheck  = findViewById(R.id.btn_check);
        textTimer = findViewById(R.id.text_timer);
        textScore = findViewById(R.id.text_score);
        textWord  = findViewById(R.id.text_word);
        btnImage1 = findViewById(R.id.btn_image_1);
        btnImage2 = findViewById(R.id.btn_image_2);
        btnImage3 = findViewById(R.id.btn_image_3);

        // Init DB & skor awal
        db    = new koneksi(this);
        score = db.getTotalPointsForUser(userPrefs.getString("current_user", ""));
        textScore.setText("Score: " + score);

        // Init Musik
        musicManager = MusicManager.getInstance();
        // Set kata soal
        textWord.setText("MALAYSIA");

        // Mulai timer & interaksi
        startTimer(TOTAL_TIME_MS);

        btnPause.setOnClickListener(v -> showPauseDialog());

        View.OnClickListener imgClick = v -> {
            btnImage1.setAlpha(0.6f);
            btnImage2.setAlpha(0.6f);
            btnImage3.setAlpha(0.6f);
            v.setAlpha(1f);
            if (v.getId() == R.id.btn_image_1)      selectedRes = R.drawable.flag_malaysia;
            else if (v.getId() == R.id.btn_image_2) selectedRes = R.drawable.flag_singapore;
            else if (v.getId() == R.id.btn_image_3) selectedRes = R.drawable.flag_thailand;
        };
        btnImage1.setOnClickListener(imgClick);
        btnImage2.setOnClickListener(imgClick);
        btnImage3.setOnClickListener(imgClick);

        btnCheck.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            evaluateAnswer();
        });
    }

    private void startTimer(long ms) {
        textTimer.setText(formatTime(ms));
        timer = new CountDownTimer(ms, 1000) {
            @Override public void onTick(long m) {
                timeRemaining = m;
                textTimer.setText(formatTime(m));
            }
            @Override public void onFinish() {
                textTimer.setText("00:00");
                evaluateAnswer();
            }
        }.start();
    }

    private String formatTime(long ms) {
        int sec = (int)(ms / 1000);
        return String.format("00:%02d", sec);
    }

    private void evaluateAnswer() {
        int stars;
        if (selectedRes == R.drawable.flag_malaysia) {
            long secLeft = timeRemaining / 1000;
            if (secLeft >= 20)       stars = 3;
            else if (secLeft >= 10)  stars = 2;
            else                      stars = 1;
            playSound(R.raw.tepuk);
        } else {
            stars = 0;
            playSound(R.raw.sedih);
        }

        // Update skor UI
        int gained = (stars == 3 ? 10 : stars * 3);
        score += gained;
        textScore.setText("Score: " + score);

        // Simpan ke DB
        int timeUsed = (int)((TOTAL_TIME_MS - timeRemaining) / 1000);
        db.updateProgress(2, stars, timeUsed, currentUserId);

        // Unlock Level 3 TN
        if (stars >= 1) {
            tnPrefs.edit()
                    .putBoolean(KEY_LEVEL3, true)
                    .apply();
        }

        showResultDialog(stars);
    }

    private void playSound(int resId) {
        musicManager.stop();
        musicManager.init(this, resId, false);
        musicManager.play(1f);
    }

    private void showPauseDialog() {
        Dialog dlg = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dlg.setContentView(R.layout.continw);
        dlg.setCancelable(false);
        dlg.findViewById(R.id.btn_lanjut).setOnClickListener(v -> {
            dlg.dismiss();
            startTimer(timeRemaining);
        });
        dlg.findViewById(R.id.btn_stop).setOnClickListener(v -> {
            dlg.dismiss();
            finish();
        });
        dlg.show();
    }

    private void showResultDialog(int stars) {
        Dialog dlg = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dlg.setContentView(R.layout.peraihan);
        dlg.setCancelable(false);
        dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvStatus = dlg.findViewById(R.id.tv_header);
        ImageView s1      = dlg.findViewById(R.id.star1);
        ImageView s2      = dlg.findViewById(R.id.star2);
        ImageView s3      = dlg.findViewById(R.id.star3);
        TextView tvSc     = dlg.findViewById(R.id.tv_score);
        TextView tvTm     = dlg.findViewById(R.id.tv_time);

        s1.setImageResource(stars >= 1 ? R.drawable.star : R.drawable.star_empty);
        s2.setImageResource(stars >= 2 ? R.drawable.star : R.drawable.star_empty);
        s3.setImageResource(stars >= 3 ? R.drawable.star : R.drawable.star_empty);

        tvSc.setText("Score: " + score);
        tvTm.setText("Time: " + formatTime(TOTAL_TIME_MS - timeRemaining));
        tvStatus.setText(stars == 0 ? "You Failed" : "Level Completed");

        Button retry  = dlg.findViewById(R.id.btn_retry);
        Button levels = dlg.findViewById(R.id.btn_levels);
        Button next   = dlg.findViewById(R.id.btn_next);

        retry.setOnClickListener(v -> { dlg.dismiss(); recreate(); });
        levels.setOnClickListener(v -> {
            dlg.dismiss();
            startActivity(new Intent(this, tebaknegaraActivity.class));
            finish();
        });
        next.setOnClickListener(v -> {
            dlg.dismiss();
            startActivity(new Intent(this, Level3TNActivity.class));
            finish();
        });

        dlg.show();
    }

    @Override protected void onPause() {
        super.onPause();
        musicManager.pause();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        musicManager.stop();
    }
}
