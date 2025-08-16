package com.example.gamebyna;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Level1MTActivity extends AppCompatActivity {
    private static final long TOTAL_TIME_MS = 30_000;
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_USER_ID = "current_user_id";
    private static final String KEY_LEVEL_2_UNLOCKED = "level_2_unlocked";

    private Button btnOption1, btnOption2, btnOption3;
    private Button btnPause, btnCheck;
    private TextView textTimer, textScore, textQuestion;
    private View mainLayout;

    private CountDownTimer timer;
    private long timeRemaining = TOTAL_TIME_MS;
    private int score;
    private int selectedValue = -1;

    private MusicManager musicManager;
    private koneksi db;
    private int currentUserId;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level1_mtactivity);
        mainLayout = findViewById(R.id.main);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);

        btnPause     = findViewById(R.id.btn_pause);
        btnCheck     = findViewById(R.id.btn_check);
        textTimer    = findViewById(R.id.text_timer);
        textScore    = findViewById(R.id.text_score);
        textQuestion = findViewById(R.id.text_question);
        btnOption1   = findViewById(R.id.btn_option_1);
        btnOption2   = findViewById(R.id.btn_option_2);
        btnOption3   = findViewById(R.id.btn_option_3);

        db = new koneksi(this);
        score = db.getTotalPointsForUser(prefs.getString("current_user", ""));
        textScore.setText("Score: " + score);

        musicManager = MusicManager.getInstance();

        startTimer(TOTAL_TIME_MS);

        btnPause.setOnClickListener(v -> showPauseDialog());

        View.OnClickListener optClick = v -> {
            btnOption1.setAlpha(1f);
            btnOption2.setAlpha(1f);
            btnOption3.setAlpha(1f);
            v.setAlpha(0.6f);
            if (v.getId() == R.id.btn_option_1) selectedValue = 11;
            else if (v.getId() == R.id.btn_option_2) selectedValue = 7;
            else if (v.getId() == R.id.btn_option_3) selectedValue = 12;
        };
        btnOption1.setOnClickListener(optClick);
        btnOption2.setOnClickListener(optClick);
        btnOption3.setOnClickListener(optClick);

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
        // correct answer: 2×3+5 = 11
        if (selectedValue == 11) {
            long secLeft = timeRemaining / 1000;
            if (secLeft >= 20) stars = 3;
            else if (secLeft >= 10) stars = 2;
            else stars = 1;
            playSound(R.raw.tepuk);
        } else {
            stars = 0;
            playSound(R.raw.sedih);
        }

        int gained = (stars == 3 ? 10 : stars * 3);
        score += gained;
        textScore.setText("Score: " + score);

        int timeUsed = (int)((TOTAL_TIME_MS - timeRemaining) / 1000);
        db.updateProgress(1, stars, timeUsed, currentUserId);

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
        ImageView s1 = dlg.findViewById(R.id.star1);
        ImageView s2 = dlg.findViewById(R.id.star2);
        ImageView s3 = dlg.findViewById(R.id.star3);
        TextView tvSc = dlg.findViewById(R.id.tv_score);
        TextView tvTm = dlg.findViewById(R.id.tv_time);

        s1.setImageResource(stars >= 1 ? R.drawable.star : R.drawable.star_empty);
        s2.setImageResource(stars >= 2 ? R.drawable.star : R.drawable.star_empty);
        s3.setImageResource(stars >= 3 ? R.drawable.star : R.drawable.star_empty);

        tvSc.setText("Score: " + score);
        tvTm.setText("Time: " + formatTime(TOTAL_TIME_MS - timeRemaining));

        if (stars == 0) {
            tvStatus.setText("You Failed");
        } else {
            tvStatus.setText("Level Completed");

            // ✅ UNLOCK LEVEL 2
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_LEVEL_2_UNLOCKED, true);
            editor.apply();
        }

        Button retry = dlg.findViewById(R.id.btn_retry);
        Button levels = dlg.findViewById(R.id.btn_levels);
        Button next = dlg.findViewById(R.id.btn_next);

        retry.setOnClickListener(v -> { dlg.dismiss(); recreate(); });
        levels.setOnClickListener(v -> {
            dlg.dismiss();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
        next.setOnClickListener(v -> {
            dlg.dismiss();
            startActivity(new Intent(this, Level2MTActivity.class));
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
