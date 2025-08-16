// File: MusicManager.java
package com.example.gamebyna;

import android.content.Context;
import android.media.MediaPlayer;

public class MusicManager {
    private static MusicManager instance;
    private MediaPlayer mediaPlayer;
    private int currentResId = -1;

    private MusicManager() {}

    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    /**
     * Inisialisasi musik dengan resource baru. Jika sebelumnya ada musik lain, akan dihentikan.
     * Tidak langsung memulai playback—hanya menyiapkan MediaPlayer.
     */
    public void init(Context ctx, int resId, boolean loop) {
        if (mediaPlayer != null) {
            if (currentResId == resId) return; // Sudah inisialisasi sama
            stop(); // Hentikan dan release sebelumnya
        }

        mediaPlayer = MediaPlayer.create(ctx.getApplicationContext(), resId);
        mediaPlayer.setLooping(loop);
        currentResId = resId;

        mediaPlayer.setOnCompletionListener(mp -> {
            if (!loop) {
                stop();
            }
        });
    }

    /**
     * Mainkan musik jika belum diputar. Jika sudah pause, akan resume.
     * @param volume 0.0f – 1.0f
     */
    public void play(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    /** Pause tanpa release resource. */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /** Hentikan dan release resource, reset ID. */
    public void stop() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (IllegalStateException ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
            currentResId = -1;
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}
