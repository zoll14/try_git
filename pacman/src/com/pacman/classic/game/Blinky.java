package com.pacman.classic.game;

/** Blinky (Red): Directly chases Pacman's current tile. */
public class Blinky extends Ghost {
    public Blinky() {
        super(13, 11, 25, 0, 0f); // exits immediately
    }

    @Override
    protected int[] calculateChaseTarget(PacMan pacman, Ghost blinky) {
        return new int[]{pacman.getTileCol(), pacman.getTileRow()};
    }
}
