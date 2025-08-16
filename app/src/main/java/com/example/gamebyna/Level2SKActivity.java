package com.example.gamebyna;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Level2SKActivity extends AppCompatActivity {

    private static final String TARGET_WORD     = "PESAWAT";
    private static final long   TOTAL_TIME_MS   = 30_000;

    private static final String USER_PREFS      = "app_settings";
    private static final String KEY_USER_ID     = "current_user_id";
    private static final String SK_PREFS        = "level_status_sk";
    private static final String KEY_LEVEL3      = "level3";

    private Button btnPause, btnShuffle, btnCheck;
    private TextView textTimer, textScore;
    private EditText editAnswer;
    private GridLayout gridLetters;
    private ImageView imageWord;

    private CountDownTimer timer;
    private int score = 0;
    private long timeRemaining = TOTAL_TIME_MS;

    private MusicManager musicManager;
    private koneksi db;
    private int currentUserId;
    private SharedPreferences skPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level2_skactivity);

        // Ambil ID user dari SharedPreferences
        SharedPreferences userPrefs = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        currentUserId = userPrefs.getInt(KEY_USER_ID, -1);

        // SharedPreferences untuk Susun Kata
        skPrefs = getSharedPreferences(SK_PREFS, Context.MODE_PRIVATE);

        // Init views & helpers
        btnPause    = findViewById(R.id.btn_pause);
        textTimer   = findViewById(R.id.text_timer);
        textScore   = findViewById(R.id.text_score);
        editAnswer  = findViewById(R.id.edit_answer);
        gridLetters = findViewById(R.id.grid_letters);
        btnShuffle  = findViewById(R.id.btn_shuffle);
        btnCheck    = findViewById(R.id.btn_check);
        imageWord   = findViewById(R.id.image_word);

        musicManager = MusicManager.getInstance();
        db = new koneksi(this);

        // Set image
        imageWord.setImageResource(R.drawable.pesawat);


        // Score awal
        score = db.getTotalPointsForUser(userPrefs.getString("current_user", ""));
        textScore.setText("Score: " + score);

        startTimer(TOTAL_TIME_MS);
        shuffleAndBuildLetters();

        btnShuffle.setOnClickListener(v -> {
            editAnswer.setText("");
            shuffleAndBuildLetters();
        });

        btnCheck.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            evaluateAnswer();
        });

        btnPause.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            showPauseDialog();
        });
    }

    private void startTimer(long millis) {
        textTimer.setText(formatTime(millis));
        timer = new CountDownTimer(millis, 1000) {
            @Override public void onTick(long msUntilFinished) {
                timeRemaining = msUntilFinished;
                textTimer.setText(formatTime(msUntilFinished));
            }
            @Override public void onFinish() {
                textTimer.setText("00:00");
                evaluateAnswer();
            }
        }.start();
    }

    private String formatTime(long ms) {
        int totalSec = (int)(ms / 1000);
        int m = totalSec / 60, s = totalSec % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void shuffleAndBuildLetters() {
        gridLetters.removeAllViews();
        List<Character> letters = new ArrayList<>();
        for (char c : TARGET_WORD.toCharArray()) letters.add(c);
        Collections.shuffle(letters);
        gridLetters.setColumnCount(letters.size());

        for (char c : letters) {
            Button btn = new Button(this);
            btn.setText(String.valueOf(c));
            btn.setTextSize(20f);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0;
            p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            btn.setLayoutParams(p);
            btn.setOnClickListener(v -> {
                editAnswer.append(String.valueOf(c));
                v.setEnabled(false);
            });
            gridLetters.addView(btn);
        }
    }

    private void evaluateAnswer() {
        String ans = editAnswer.getText().toString().trim();
        int stars;
        if (ans.equalsIgnoreCase(TARGET_WORD)) {
            long secLeft = timeRemaining / 1000;
            if (secLeft >= 20) stars = 3;
            else if (secLeft >= 10) stars = 2;
            else stars = 1;
            musicManager.stop();
            musicManager.init(this, R.raw.tepuk, false);
        } else {
            stars = 0;
            musicManager.stop();
            musicManager.init(this, R.raw.sedih, false);
        }
        musicManager.play(1f);

        score += (stars == 3 ? 10 : stars * 3);
        textScore.setText("Score: " + score);

        int timeUsed = (int)((TOTAL_TIME_MS - timeRemaining) / 1000);
        db.updateProgress(2, stars, timeUsed, currentUserId);

        // Unlock Level 3 SK
        if (stars >= 1) {
            skPrefs.edit()
                    .putBoolean(KEY_LEVEL3, true)
                    .apply();
        }

        showResultDialog(stars);
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
        TextView tvS   = dlg.findViewById(R.id.tv_score);
        TextView tvT   = dlg.findViewById(R.id.tv_time);

        s1.setImageResource(stars >= 1 ? R.drawable.star : R.drawable.star_empty);
        s2.setImageResource(stars >= 2 ? R.drawable.star : R.drawable.star_empty);
        s3.setImageResource(stars >= 3 ? R.drawable.star : R.drawable.star_empty);

        tvS.setText("Score: " + score);
        tvT.setText("Time: " + formatTime(TOTAL_TIME_MS - timeRemaining));
        tvStatus.setText(stars == 0 ? "You Failed" : "Level Completed");

        dlg.findViewById(R.id.btn_retry).setOnClickListener(v -> { dlg.dismiss(); recreate(); });
        dlg.findViewById(R.id.btn_levels).setOnClickListener(v -> {
            dlg.dismiss();
            Intent i = new Intent(Level2SKActivity.this, susunkataActivity.class);
            i.putExtra("USER_ID", currentUserId);
            startActivity(i);
            finish();
        });
        dlg.findViewById(R.id.btn_next).setOnClickListener(v -> {
            dlg.dismiss();
            Intent i = new Intent(Level2SKActivity.this, Level3SKActivity.class);
            i.putExtra("USER_ID", currentUserId);
            startActivity(i);
            finish();
        });

        dlg.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        musicManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        musicManager.stop();
    }
}
