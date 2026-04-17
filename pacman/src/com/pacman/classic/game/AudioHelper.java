package com.pacman.classic.game;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Handles all audio: looping background siren + programmatic sound effects.
 * No raw asset files needed — everything is synthesised on-device.
 */
public class AudioHelper {

    private static final int SAMPLE_RATE = 22050;

    private boolean soundEnabled;
    private boolean musicEnabled;

    // Dot "waka" alternates between two pitches each call
    private volatile boolean wakaPhase = false;

    // Music loop state (volatile so MusicLoop thread reads fresh values)
    private volatile boolean musicRunning = false;
    private volatile boolean musicPaused  = false;
    private volatile boolean frightened   = false;
    private Thread musicThread;

    // ── Siren note tables {freq_hz, duration_ms} ────────────────────────────

    /** Normal-mode: ascending + descending sweep (~840 ms/cycle). */
    private static final float[][] SIREN = {
        {220, 60},{233, 60},{247, 60},{262, 60},{277, 60},{294, 60},
        {311, 60},{330, 60},{311, 60},{294, 60},{277, 60},{262, 60},
        {247, 60},{233, 60}
    };

    /** Frightened-mode: rapid two-tone warble. */
    private static final float[][] FRIGHT = {
        {175, 50},{196, 50},{175, 50},{196, 50},
        {175, 50},{196, 50},{0,   30}
    };

    // ────────────────────────────────────────────────────────────────────────

    public AudioHelper(Context ctx, boolean soundEnabled, boolean musicEnabled) {
        this.soundEnabled = soundEnabled;
        this.musicEnabled = musicEnabled;
    }

    // ── Music lifecycle ──────────────────────────────────────────────────────

    /** Start the background siren if not already running. */
    public void startMusic() {
        if (!musicEnabled || musicRunning) return;
        musicRunning = true;
        musicPaused  = false;
        musicThread  = new Thread(new MusicLoop(), "pac-music");
        musicThread.setDaemon(true);
        musicThread.start();
    }

    /** Permanently stop the music thread. */
    public void stopMusic() {
        musicRunning = false;
        if (musicThread != null) {
            musicThread.interrupt();
            musicThread = null;
        }
    }

    /** Silence the siren without killing the thread (app going to background). */
    public void pauseMusic() { musicPaused = true; }

    /** Resume after a pause. */
    public void resumeMusic() {
        if (!musicEnabled) return;
        if (!musicRunning) { startMusic(); return; }
        musicPaused = false;
    }

    /** Switch between normal siren and frightened warble. */
    public void setFrightened(boolean f) { frightened = f; }

    // ── Sound effects ────────────────────────────────────────────────────────

    /** Alternating "waka waka" pitches — call once per dot eaten. */
    public void playDot() {
        if (!soundEnabled) return;
        float freq = wakaPhase ? 330f : 415f;
        wakaPhase = !wakaPhase;
        playAsync(new float[][]{{freq, 40}}, 0.40f);
    }

    /** Three-tone descending sweep. */
    public void playPowerPellet() {
        if (!soundEnabled) return;
        playAsync(new float[][]{{784, 80},{659, 80},{523, 120}}, 0.55f);
    }

    /** Ascending chirp when a ghost is eaten. */
    public void playGhostEat() {
        if (!soundEnabled) return;
        playAsync(new float[][]{{440, 55},{587, 55},{784, 100}}, 0.50f);
    }

    /** Descending chromatic death jingle (B4 down to C4). */
    public void playDeath() {
        if (!soundEnabled) return;
        playAsync(new float[][]{
            {494, 80},{466, 80},{440, 80},{415, 80},{392, 80},{370, 80},
            {349, 80},{330, 80},{311, 80},{294, 80},{262, 160}
        }, 0.55f);
    }

    /** Rapid ascending sweep — speed boost collected. */
    public void playSpeedBoost() {
        if (!soundEnabled) return;
        playAsync(new float[][]{{523,40},{659,40},{784,40},{1047,80}}, 0.45f);
    }

    /** Calm ascending chime — shield collected. */
    public void playShield() {
        if (!soundEnabled) return;
        playAsync(new float[][]{{392,60},{494,60},{587,90}}, 0.45f);
    }

    /** Short metallic ping — shield absorbs a hit. */
    public void playShieldUsed() {
        if (!soundEnabled) return;
        playAsync(new float[][]{{880,50},{660,50},{440,80}}, 0.50f);
    }

    /** Short ascending fanfare on level clear. */
    public void playLevelComplete() {
        if (!soundEnabled) return;
        playAsync(new float[][]{
            {349, 90},{392, 90},{440, 90},{494, 90},{523, 200}
        }, 0.55f);
    }

    // ── Settings ─────────────────────────────────────────────────────────────

    public void setSoundEnabled(boolean e) { soundEnabled = e; }

    public void setMusicEnabled(boolean e) {
        musicEnabled = e;
        if (e) startMusic(); else stopMusic();
    }

    public void release() { stopMusic(); }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private void playAsync(final float[][] notes, final float vol) {
        Thread t = new Thread(new Runnable() {
            public void run() { renderAndPlay(notes, vol); }
        });
        t.setDaemon(true);
        t.start();
    }

    /** Synthesise a note sequence to a static AudioTrack and play it. */
    private void renderAndPlay(float[][] notes, float vol) {
        try {
            int total = 0;
            for (float[] n : notes) total += sampleCount(n[1]);

            short[] buf = new short[total];
            int pos = 0;
            for (float[] note : notes) {
                int ns     = sampleCount(note[1]);
                float freq = note[0];
                int attack = Math.max(1, ns / 8);
                int decay  = Math.max(1, ns / 4);
                for (int i = 0; i < ns; i++, pos++) {
                    if (freq > 0) {
                        float env = 1f;
                        if (i < attack) env = (float) i / attack;
                        else if (i > ns - decay) env = (float)(ns - i) / decay;
                        double a = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
                        buf[pos] = (short)(Math.sin(a) * vol * env * Short.MAX_VALUE);
                    }
                }
            }

            byte[] bytes = shortToBytes(buf);
            AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bytes.length, AudioTrack.MODE_STATIC);
            track.write(bytes, 0, bytes.length);
            track.play();

            int waitMs = 0;
            for (float[] n : notes) waitMs += (int) n[1];
            Thread.sleep(waitMs + 80);
            track.stop();
            track.release();
        } catch (Exception ignore) {}
    }

    /** Streaming music loop: continuously generates note sequences. */
    private class MusicLoop implements Runnable {
        @Override
        public void run() {
            // 50 ms chunk
            final int bufSamples = SAMPLE_RATE / 20;
            final int minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
            final int trackBuf = Math.max(minBuf * 2, bufSamples * 2 * 2);

            byte[] bytes = new byte[bufSamples * 2];

            AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                trackBuf, AudioTrack.MODE_STREAM);
            track.play();

            int   noteIdx   = 0;
            int   samplePos = 0;
            float[][] cur   = SIREN;

            try {
                while (musicRunning) {
                    if (musicPaused) {
                        // Feed silence so the track doesn't underrun noisily
                        java.util.Arrays.fill(bytes, (byte) 0);
                        track.write(bytes, 0, bytes.length);
                        Thread.sleep(30);
                        continue;
                    }

                    // Switch melody table if frightened state changed
                    float[][] melody = frightened ? FRIGHT : SIREN;
                    if (melody != cur) {
                        cur       = melody;
                        noteIdx   = 0;
                        samplePos = 0;
                    }

                    // Fill one 50 ms buffer
                    for (int i = 0; i < bufSamples; ) {
                        if (noteIdx >= cur.length) noteIdx = 0;
                        float freq = cur[noteIdx][0];
                        int   ns   = sampleCount(cur[noteIdx][1]);

                        int toWrite = Math.min(ns - samplePos, bufSamples - i);
                        for (int j = 0; j < toWrite; j++, i++, samplePos++) {
                            short v = 0;
                            if (freq > 0) {
                                double a = 2.0 * Math.PI * freq * samplePos / SAMPLE_RATE;
                                v = (short)(Math.sin(a) * 0.18 * Short.MAX_VALUE);
                            }
                            bytes[i * 2]     = (byte)(v & 0xff);
                            bytes[i * 2 + 1] = (byte)((v >> 8) & 0xff);
                        }

                        if (samplePos >= ns) { samplePos = 0; noteIdx++; }
                    }

                    track.write(bytes, 0, bytes.length);
                }
            } catch (InterruptedException ignore) {
            } finally {
                track.stop();
                track.release();
            }
        }
    }

    private static int sampleCount(float ms) {
        return Math.max(1, (int)(SAMPLE_RATE * ms / 1000f));
    }

    private static byte[] shortToBytes(short[] buf) {
        byte[] b = new byte[buf.length * 2];
        for (int i = 0; i < buf.length; i++) {
            b[i * 2]     = (byte)(buf[i] & 0xff);
            b[i * 2 + 1] = (byte)((buf[i] >> 8) & 0xff);
        }
        return b;
    }
}
