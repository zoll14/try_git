package com.pacman.classic.test;

import com.pacman.classic.game.GameEngine;
import com.pacman.classic.game.Maze;
import com.pacman.classic.game.PacMan;

public class PowerUpTest {
    private int passed = 0;
    private int failed = 0;

    private void assertEquals(String name, int expected, int actual) {
        if (expected == actual) { System.out.println("  PASS: " + name); passed++; }
        else { System.out.println("  FAIL: " + name + " (expected=" + expected + " actual=" + actual + ")"); failed++; }
    }

    private void assertEqualsF(String name, float expected, float actual, float delta) {
        if (Math.abs(expected - actual) <= delta) { System.out.println("  PASS: " + name); passed++; }
        else { System.out.println("  FAIL: " + name + " (expected=" + expected + " actual=" + actual + ")"); failed++; }
    }

    private void assertTrue(String name, boolean cond) {
        if (cond) { System.out.println("  PASS: " + name); passed++; }
        else { System.out.println("  FAIL: " + name); failed++; }
    }

    // ── PacMan power-up state ─────────────────────────────────────────────────

    public void testSpeedBoostApply() {
        PacMan p = new PacMan();
        assertTrue("no boost initially", p.getSpeedBoostTimer() == 0f);
        p.applySpeedBoost(5f);
        assertEqualsF("boost timer set to 5s", 5f, p.getSpeedBoostTimer(), 0.001f);
    }

    public void testSpeedBoostTicksDown() {
        PacMan p = new PacMan();
        p.applySpeedBoost(5f);
        Maze maze = new Maze();
        p.update(1.0f, maze); // advance 1 second
        assertTrue("boost timer decremented", p.getSpeedBoostTimer() < 5f);
        assertTrue("boost timer > 0 after 1s", p.getSpeedBoostTimer() > 0f);
    }

    public void testSpeedBoostExpiresAfterDuration() {
        PacMan p = new PacMan();
        p.applySpeedBoost(2f);
        Maze maze = new Maze();
        p.update(2.1f, maze); // advance past the duration
        assertEqualsF("boost timer at 0 after expiry", 0f, p.getSpeedBoostTimer(), 0.001f);
    }

    public void testShieldApply() {
        PacMan p = new PacMan();
        assertTrue("no shield initially", !p.isShielded());
        p.applyShield();
        assertTrue("shielded after applyShield()", p.isShielded());
    }

    public void testShieldConsume() {
        PacMan p = new PacMan();
        p.applyShield();
        boolean consumed = p.consumeShield();
        assertTrue("consumeShield returns true when shielded", consumed);
        assertTrue("shield gone after consume", !p.isShielded());
    }

    public void testShieldConsumeWhenNone() {
        PacMan p = new PacMan();
        boolean consumed = p.consumeShield();
        assertTrue("consumeShield returns false when not shielded", !consumed);
        assertTrue("still not shielded", !p.isShielded());
    }

    public void testResetClearsPowerUps() {
        PacMan p = new PacMan();
        p.applySpeedBoost(5f);
        p.applyShield();
        p.reset();
        assertTrue("reset clears shield", !p.isShielded());
        assertEqualsF("reset clears boost timer", 0f, p.getSpeedBoostTimer(), 0.001f);
    }

    // ── GameEngine integration ────────────────────────────────────────────────

    public void testSpeedBoostTileConstant() {
        assertEquals("SPEED_BOOST == 7", 7, Maze.SPEED_BOOST);
    }

    public void testShieldTileConstant() {
        assertEquals("SHIELD == 8", 8, Maze.SHIELD);
    }

    public void testGameEngineInitialNoPowerUps() {
        GameEngine engine = new GameEngine(null);
        engine.startNewGame();
        PacMan p = engine.getPacman();
        assertTrue("no shield at game start", !p.isShielded());
        assertEqualsF("no boost at game start", 0f, p.getSpeedBoostTimer(), 0.001f);
    }

    public void testShieldCollectedViaEngine() {
        // Place Pac-Man on a shield tile by teleporting pacman to (26,6) via maze collect
        Maze maze = new Maze();
        assertEquals("shield tile at (26,6)", Maze.SHIELD, maze.getTile(26, 6));
        int result = maze.collectTile(26, 6);
        assertEquals("collecting shield tile returns SHIELD", Maze.SHIELD, result);
    }

    public void testSpeedBoostCollectedViaMaze() {
        Maze maze = new Maze();
        assertEquals("speed boost tile at (8,6)", Maze.SPEED_BOOST, maze.getTile(8, 6));
        int result = maze.collectTile(8, 6);
        assertEquals("collecting speed boost tile returns SPEED_BOOST", Maze.SPEED_BOOST, result);
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        PowerUpTest t = new PowerUpTest();
        System.out.println("=== PowerUpTest ===");
        t.testSpeedBoostApply();
        t.testSpeedBoostTicksDown();
        t.testSpeedBoostExpiresAfterDuration();
        t.testShieldApply();
        t.testShieldConsume();
        t.testShieldConsumeWhenNone();
        t.testResetClearsPowerUps();
        t.testSpeedBoostTileConstant();
        t.testShieldTileConstant();
        t.testGameEngineInitialNoPowerUps();
        t.testShieldCollectedViaEngine();
        t.testSpeedBoostCollectedViaMaze();
        System.out.println("=== " + t.passed + " passed, " + t.failed + " failed ===");
        System.exit(t.failed > 0 ? 1 : 0);
    }
}
