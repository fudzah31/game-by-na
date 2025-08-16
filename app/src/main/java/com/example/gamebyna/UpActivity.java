package com.example.gamebyna; // Ganti dengan package milikmu

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamebyna.MainActivity;

public class UpActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up); // Pastikan file layout bernama activity_up.xml

        imageView = findViewById(R.id.imageview);

        // Buat animasi fade-in
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(3000); // durasi animasi 2 detik
        fadeIn.setFillAfter(true); // biar gambar tetap terlihat setelah animasi

        // Event ketika animasi selesai
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Tidak perlu isi
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Setelah animasi selesai, pindah ke MainActivity
                Intent intent = new Intent(UpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Tutup UpActivity agar tidak bisa kembali
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Tidak perlu isi
            }
        });

        // Jalankan animasi
        imageView.startAnimation(fadeIn);
    }
}
