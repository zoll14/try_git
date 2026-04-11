package com.pacman.classic.game;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

public class AudioHelper {
    private static final String TAG = "AudioHelper";
    private static final int MAX_STREAMS = 5;

    private SoundPool soundPool;
    private MediaPlayer bgMusic;

    // Sound IDs
    private int soundDot;
    private int soundPowerPellet;
    private int soundGhostEat;
    private int soundDeath;
    private int soundLevelComplete;
    private int soundExtraLife;

    private boolean soundEnabled;
    private boolean musicEnabled;
    private boolean loaded;

    private Context context;

    public AudioHelper(Context ctx, boolean soundEnabled, boolean musicEnabled) {
        this.context = ctx;
        this.soundEnabled = soundEnabled;
        this.musicEnabled = musicEnabled;
        init();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        try {
            soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool pool, int id, int status) {
                    // sounds loaded
                }
            });
            // We generate tones programmatically since we have no raw assets
            loaded = true;
        } catch (Exception e) {
            Log.w(TAG, "SoundPool init failed: " + e.getMessage());
        }
    }

    public void playDot() {
        if (!soundEnabled || !loaded) return;
        playBeep(880, 30);
    }

    public void playPowerPellet() {
        if (!soundEnabled || !loaded) return;
        playBeep(440, 200);
    }

    public void playGhostEat() {
        if (!soundEnabled || !loaded) return;
        playBeep(660, 150);
    }

    public void playDeath() {
        if (!soundEnabled || !loaded) return;
        playBeep(220, 500);
    }

    public void playLevelComplete() {
        if (!soundEnabled || !loaded) return;
        playBeep(1100, 400);
    }

    /** Generate a simple beep tone on a background thread (no raw assets needed). */
    private void playBeep(final int freq, final int durationMs) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    int sampleRate = 44100;
                    int numSamples = sampleRate * durationMs / 1000;
                    byte[] buf = new byte[numSamples * 2];
                    for (int i = 0; i < numSamples; i++) {
                        double angle = 2.0 * Math.PI * i / (sampleRate / freq);
                        short val = (short)(Math.sin(angle) * 0.3 * Short.MAX_VALUE);
                        buf[i * 2] = (byte)(val & 0xff);
                        buf[i * 2 + 1] = (byte)((val >> 8) & 0xff);
                    }
                    android.media.AudioTrack track = new android.media.AudioTrack(
                        AudioManager.STREAM_MUSIC, sampleRate,
                        android.media.AudioFormat.CHANNEL_OUT_MONO,
                        android.media.AudioFormat.ENCODING_PCM_16BIT,
                        buf.length, android.media.AudioTrack.MODE_STATIC);
                    track.write(buf, 0, buf.length);
                    track.play();
                    Thread.sleep(durationMs + 50);
                    track.stop();
                    track.release();
                } catch (Exception e) {
                    // ignore audio errors
                }
            }
        }).start();
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (bgMusic != null) {
            if (enabled) bgMusic.start();
            else bgMusic.pause();
        }
    }

    public void release() {
        if (soundPool != null) soundPool.release();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.release();
        }
    }
}
