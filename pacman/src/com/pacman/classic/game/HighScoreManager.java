package com.pacman.classic.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HighScoreManager {
    private static final String PREFS_NAME = "pacman_scores";
    private static final String KEY_SCORE_COUNT = "score_count";
    private static final String KEY_SCORE_PREFIX = "score_";
    private static final String KEY_NAME_PREFIX = "name_";
    private static final int MAX_SCORES = 10;

    public static class ScoreEntry {
        public final String name;
        public final int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    private final SharedPreferences prefs;

    public HighScoreManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<ScoreEntry> getScores() {
        int count = prefs.getInt(KEY_SCORE_COUNT, 0);
        List<ScoreEntry> list = new ArrayList<ScoreEntry>();
        for (int i = 0; i < count; i++) {
            int s = prefs.getInt(KEY_SCORE_PREFIX + i, 0);
            String n = prefs.getString(KEY_NAME_PREFIX + i, "AAA");
            list.add(new ScoreEntry(n, s));
        }
        Collections.sort(list, new Comparator<ScoreEntry>() {
            public int compare(ScoreEntry a, ScoreEntry b) {
                return b.score - a.score;
            }
        });
        return list;
    }

    public boolean isHighScore(int score) {
        List<ScoreEntry> scores = getScores();
        if (scores.size() < MAX_SCORES) return score > 0;
        return score > scores.get(scores.size() - 1).score;
    }

    public void addScore(String name, int score) {
        List<ScoreEntry> scores = getScores();
        scores.add(new ScoreEntry(name, score));
        Collections.sort(scores, new Comparator<ScoreEntry>() {
            public int compare(ScoreEntry a, ScoreEntry b) {
                return b.score - a.score;
            }
        });
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt(KEY_SCORE_COUNT, scores.size());
        for (int i = 0; i < scores.size(); i++) {
            ed.putInt(KEY_SCORE_PREFIX + i, scores.get(i).score);
            ed.putString(KEY_NAME_PREFIX + i, scores.get(i).name);
        }
        ed.apply();
    }

    public void clearScores() {
        prefs.edit().clear().apply();
    }
}
