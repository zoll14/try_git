package com.pacman.classic.game;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "pacman_settings";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_MUSIC = "music_enabled";
    private static final String KEY_SWIPE = "swipe_controls";

    private final SharedPreferences prefs;

    public SettingsManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public boolean isMusicEnabled() {
        return prefs.getBoolean(KEY_MUSIC, true);
    }

    public void setMusicEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply();
    }

    /** Returns true if swipe controls are selected; false means D-Pad. */
    public boolean isSwipeEnabled() {
        return prefs.getBoolean(KEY_SWIPE, false);
    }

    public void setSwipeEnabled(boolean swipe) {
        prefs.edit().putBoolean(KEY_SWIPE, swipe).apply();
    }
}
