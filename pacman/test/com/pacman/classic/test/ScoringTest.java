package com.pacman.classic.test;

import com.pacman.classic.game.GameEngine;

public class ScoringTest {
    private int passed = 0;
    private int failed = 0;

    private void assertEquals(String name, int expected, int actual) {
        if (expected == actual) { System.out.println("  PASS: " + name); passed++; }
        else { System.out.println("  FAIL: " + name + " (expected=" + expected + " actual=" + actual + ")"); failed++; }
    }

    private void assertTrue(String name, boolean cond) {
        if (cond) { System.out.println("  PASS: " + name); passed++; }
        else { System.out.println("  FAIL: " + name); failed++; }
    }

    public void testDotScore() {
        assertEquals("DOT score is 10", 10, GameEngine.DOT_SCORE);
    }

    public void testPowerPelletScore() {
        assertEquals("POWER PELLET score is 50", 50, GameEngine.POWER_PELLET_SCORE);
    }

    public void testGhostScoreDoubling() {
        // 200, 400, 800, 1600 for consecutive ghosts
        int base = GameEngine.BASE_GHOST_SCORE;
        assertEquals("1st ghost 200", 200, base * (1 << 0));
        assertEquals("2nd ghost 400", 400, base * (1 << 1));
        assertEquals("3rd ghost 800", 800, base * (1 << 2));
        assertEquals("4th ghost 1600", 1600, base * (1 << 3));
    }

    public void testInitialGameState() {
        GameEngine engine = new GameEngine(null);
        engine.startNewGame();
        assertEquals("initial score 0", 0, engine.getScore());
        assertEquals("initial lives 3", 3, engine.getLives());
        assertEquals("initial level 1", 1, engine.getLevel());
        assertTrue("initial state READY", engine.getState() == GameEngine.State.READY);
    }

    public void testHighScoreUpdate() {
        GameEngine engine = new GameEngine(null);
        engine.startNewGame();
        assertEquals("highscore starts 0", 0, engine.getHighScore());
        engine.updateHighScore(5000);
        assertEquals("highscore updated to 5000", 5000, engine.getHighScore());
        engine.updateHighScore(3000);
        assertEquals("highscore stays at 5000 (max)", 5000, engine.getHighScore());
    }

    public void testPauseResume() {
        GameEngine engine = new GameEngine(null);
        engine.startNewGame();
        // Advance past READY state
        engine.update(4f);
        assertTrue("PLAYING after READY", engine.getState() == GameEngine.State.PLAYING);
        engine.pause();
        assertTrue("PAUSED after pause()", engine.getState() == GameEngine.State.PAUSED);
        engine.resume();
        assertTrue("PLAYING after resume()", engine.getState() == GameEngine.State.PLAYING);
    }

    public static void main(String[] args) {
        ScoringTest t = new ScoringTest();
        System.out.println("=== ScoringTest ===");
        t.testDotScore();
        t.testPowerPelletScore();
        t.testGhostScoreDoubling();
        t.testInitialGameState();
        t.testHighScoreUpdate();
        t.testPauseResume();
        System.out.println("=== " + t.passed + " passed, " + t.failed + " failed ===");
        System.exit(t.failed > 0 ? 1 : 0);
    }
}
