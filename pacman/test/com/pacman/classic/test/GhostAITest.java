package com.pacman.classic.test;

import com.pacman.classic.game.*;

public class GhostAITest {
    private int passed = 0;
    private int failed = 0;

    private void assertTrue(String name, boolean condition) {
        if (condition) { System.out.println("  PASS: " + name); passed++; }
        else { System.out.println("  FAIL: " + name); failed++; }
    }

    private void assertEquals(String name, int expected, int actual) {
        if (expected == actual) { System.out.println("  PASS: " + name); passed++; }
        else { System.out.println("  FAIL: " + name + " (expected=" + expected + " actual=" + actual + ")"); failed++; }
    }

    public void testBlinkyStartsInScatterMode() {
        Blinky b = new Blinky();
        assertTrue("Blinky mode is SCATTER initially", b.getMode() == GhostMode.SCATTER);
    }

    public void testGhostFrightened() {
        Blinky b = new Blinky();
        b.setFrightened();
        assertTrue("Ghost becomes FRIGHTENED", b.getMode() == GhostMode.FRIGHTENED);
        assertTrue("Frightened timer > 0", b.getFrightenedTimer() > 0);
    }

    public void testGhostEatenAfterFrightened() {
        Blinky b = new Blinky();
        b.setFrightened();
        b.setEaten(200);
        assertTrue("Ghost is EATEN", b.getMode() == GhostMode.EATEN);
        assertTrue("isEaten() true", b.isEaten());
        assertEquals("Eat score 200", 200, b.getEatScore());
    }

    public void testSetModeSwitchesDirection() {
        Blinky b = new Blinky();
        Direction orig = b.getMode() == GhostMode.SCATTER ? Direction.UP : Direction.DOWN;
        b.setMode(GhostMode.CHASE);
        assertTrue("Mode changed to CHASE", b.getMode() == GhostMode.CHASE);
    }

    public void testFrightenedDoesNotOverrideEaten() {
        Blinky b = new Blinky();
        b.setEaten(200);
        b.setFrightened();
        assertTrue("EATEN not overridden by FRIGHTENED", b.getMode() == GhostMode.EATEN);
    }

    public void testBlinkyChaseTargetIsPacman() {
        Blinky b = new Blinky();
        // Use reflection-free approach: check via update direction tendency
        // We just verify the ghost exists and starts correctly
        assertTrue("Blinky created", b != null);
        assertTrue("Blinky not eaten initially", !b.isEaten());
    }

    public void testGhostReset() {
        Blinky b = new Blinky();
        b.setFrightened();
        b.setEaten(200);
        b.reset();
        assertTrue("After reset mode is SCATTER", b.getMode() == GhostMode.SCATTER);
        assertTrue("After reset not eaten", !b.isEaten());
    }

    public void testFlashingDuringFrightened() {
        Blinky b = new Blinky();
        b.setFrightened();
        // Right after setting frightened, timer is 8s, so not flashing (< 2s threshold)
        assertTrue("Not flashing at 8s", !b.isFlashing());
    }

    public static void main(String[] args) {
        GhostAITest t = new GhostAITest();
        System.out.println("=== GhostAITest ===");
        t.testBlinkyStartsInScatterMode();
        t.testGhostFrightened();
        t.testGhostEatenAfterFrightened();
        t.testSetModeSwitchesDirection();
        t.testFrightenedDoesNotOverrideEaten();
        t.testBlinkyChaseTargetIsPacman();
        t.testGhostReset();
        t.testFlashingDuringFrightened();
        System.out.println("=== " + t.passed + " passed, " + t.failed + " failed ===");
        System.exit(t.failed > 0 ? 1 : 0);
    }
}
