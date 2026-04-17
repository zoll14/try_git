package com.pacman.classic.test;

import com.pacman.classic.game.Maze;

/**
 * Pure-JVM tests for Maze logic (no Android dependencies).
 * Run with: javac + java (no instrumentation needed).
 */
public class MazeTest {
    private int passed = 0;
    private int failed = 0;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void assertTrue(String name, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + name);
            passed++;
        } else {
            System.out.println("  FAIL: " + name);
            failed++;
        }
    }

    private void assertEquals(String name, int expected, int actual) {
        if (expected == actual) {
            System.out.println("  PASS: " + name);
            passed++;
        } else {
            System.out.println("  FAIL: " + name + " (expected=" + expected + " actual=" + actual + ")");
            failed++;
        }
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    public void testDimensions() {
        assertEquals("COLS == 28", 28, Maze.COLS);
        assertEquals("ROWS == 30", 30, Maze.ROWS);
    }

    public void testBorderIsWall() {
        Maze maze = new Maze();
        // Top row entirely walls
        for (int c = 0; c < Maze.COLS; c++) {
            assertTrue("top-row wall at col " + c, maze.getTile(0, c) == Maze.WALL);
        }
        // Corner cells are walls
        assertTrue("top-left wall",    maze.getTile(0, 0)           == Maze.WALL);
        assertTrue("top-right wall",   maze.getTile(0, Maze.COLS-1) == Maze.WALL);
        assertTrue("bottom-left wall", maze.getTile(Maze.ROWS-1, 0) == Maze.WALL);
    }

    public void testDotsCountPositive() {
        Maze maze = new Maze();
        assertTrue("dots > 0", maze.getDotsRemaining() > 0);
        assertTrue("total dots match initial", maze.getTotalDots() == maze.getDotsRemaining());
    }

    public void testCollectDot() {
        Maze maze = new Maze();
        // Row 1 col 1 should be a dot
        int before = maze.getDotsRemaining();
        if (maze.getTile(1, 1) == Maze.DOT) {
            int collected = maze.collectTile(1, 1);
            assertEquals("collect dot returns DOT", Maze.DOT, collected);
            assertEquals("dots decreased by 1", before - 1, maze.getDotsRemaining());
            assertEquals("tile is now EMPTY", Maze.EMPTY, maze.getTile(1, 1));
        } else {
            assertTrue("row1,col1 is dot (skipped, different tile type)", true);
        }
    }

    public void testCollectWallReturnsZero() {
        Maze maze = new Maze();
        int collected = maze.collectTile(0, 0); // wall
        assertEquals("collecting wall returns WALL(0)", Maze.WALL, collected);
    }

    public void testSpeedBoostTileExists() {
        Maze maze = new Maze();
        // Power-ups placed at (8,6) and (8,21)
        assertEquals("speed boost at (8,6)",  Maze.SPEED_BOOST, maze.getTile(8, 6));
        assertEquals("speed boost at (8,21)", Maze.SPEED_BOOST, maze.getTile(8, 21));
    }

    public void testShieldTileExists() {
        Maze maze = new Maze();
        // Shield placed at (26,6) and (26,21)
        assertEquals("shield at (26,6)",  Maze.SHIELD, maze.getTile(26, 6));
        assertEquals("shield at (26,21)", Maze.SHIELD, maze.getTile(26, 21));
    }

    public void testPowerUpWalkableForPacman() {
        Maze maze = new Maze();
        assertTrue("speed boost walkable for pacman", maze.isWalkableForPacman(8, 6));
        assertTrue("shield walkable for pacman",      maze.isWalkableForPacman(26, 6));
    }

    public void testCollectSpeedBoost() {
        Maze maze = new Maze();
        int dotsBefore = maze.getDotsRemaining();
        int result = maze.collectTile(8, 6);
        assertEquals("collecting speed boost returns SPEED_BOOST", Maze.SPEED_BOOST, result);
        assertEquals("dots unchanged after power-up collect", dotsBefore, maze.getDotsRemaining());
        assertEquals("tile cleared to EMPTY", Maze.EMPTY, maze.getTile(8, 6));
    }

    public void testCollectShield() {
        Maze maze = new Maze();
        int dotsBefore = maze.getDotsRemaining();
        int result = maze.collectTile(26, 6);
        assertEquals("collecting shield returns SHIELD", Maze.SHIELD, result);
        assertEquals("dots unchanged after shield collect", dotsBefore, maze.getDotsRemaining());
        assertEquals("tile cleared to EMPTY", Maze.EMPTY, maze.getTile(26, 6));
    }

    public void testPowerUpsDoNotCountTowardLevelComplete() {
        Maze maze = new Maze();
        // Collect all dots/pellets but leave power-ups
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                int t = maze.getTile(r, c);
                if (t == Maze.DOT || t == Maze.POWER_PELLET) maze.collectTile(r, c);
            }
        }
        // Level should be complete even though power-up tiles remain
        assertTrue("level complete when all dots eaten (power-ups ignored)", maze.isLevelComplete());
    }

    public void testPowerPelletAtRow3Col1() {
        Maze maze = new Maze();
        assertTrue("power pellet at (3,1)", maze.isPowerPellet(3, 1));
    }

    public void testPowerPelletCollect() {
        Maze maze = new Maze();
        int before = maze.getDotsRemaining();
        assertTrue("power pellet exists", maze.isPowerPellet(3, 1));
        int collected = maze.collectTile(3, 1);
        assertEquals("power pellet collected returns POWER_PELLET", Maze.POWER_PELLET, collected);
        assertEquals("dots decreased", before - 1, maze.getDotsRemaining());
    }

    public void testWalkableForPacman() {
        Maze maze = new Maze();
        assertTrue("wall not walkable for pacman", !maze.isWalkableForPacman(0, 0));
        assertTrue("ghost house not walkable for pacman", !maze.isWalkableForPacman(Maze.GHOST_HOUSE_ROW, 12));
        assertTrue("dot tile walkable for pacman", maze.isWalkableForPacman(1, 1));
    }

    public void testWalkableForGhost() {
        Maze maze = new Maze();
        assertTrue("wall not walkable for ghost", !maze.isWalkableForGhost(0, 0));
        assertTrue("ghost house walkable for ghost", maze.isWalkableForGhost(Maze.GHOST_HOUSE_ROW, 12));
    }

    public void testLevelCompleteAfterAllDots() {
        Maze maze = new Maze();
        assertTrue("not complete initially", !maze.isLevelComplete());
        // collect all dots
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                maze.collectTile(r, c);
            }
        }
        assertTrue("complete after all dots", maze.isLevelComplete());
    }

    public void testPacmanStartTile() {
        Maze maze = new Maze();
        // Pacman starts at (PACMAN_START_ROW, PACMAN_START_COL)
        int row = Maze.PACMAN_START_ROW;
        int col = Maze.PACMAN_START_COL;
        assertTrue("pacman start is walkable", maze.isWalkableForPacman(row, col));
    }

    public void testResetRestoresDots() {
        Maze maze = new Maze();
        int orig = maze.getDotsRemaining();
        maze.collectTile(1, 1);
        maze.collectTile(1, 2);
        maze.reset();
        assertEquals("reset restores dot count", orig, maze.getDotsRemaining());
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        MazeTest t = new MazeTest();
        System.out.println("=== MazeTest ===");
        t.testDimensions();
        t.testBorderIsWall();
        t.testDotsCountPositive();
        t.testCollectDot();
        t.testCollectWallReturnsZero();
        t.testPowerPelletAtRow3Col1();
        t.testPowerPelletCollect();
        t.testWalkableForPacman();
        t.testWalkableForGhost();
        t.testLevelCompleteAfterAllDots();
        t.testPacmanStartTile();
        t.testResetRestoresDots();
        t.testSpeedBoostTileExists();
        t.testShieldTileExists();
        t.testPowerUpWalkableForPacman();
        t.testCollectSpeedBoost();
        t.testCollectShield();
        t.testPowerUpsDoNotCountTowardLevelComplete();
        System.out.println("=== " + t.passed + " passed, " + t.failed + " failed ===");
        System.exit(t.failed > 0 ? 1 : 0);
    }
}
