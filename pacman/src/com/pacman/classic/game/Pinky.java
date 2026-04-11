package com.pacman.classic.game;

/** Pinky (Pink): Targets 4 tiles ahead of Pacman's direction. */
public class Pinky extends Ghost {
    public Pinky() {
        super(13, 13, 2, 0, 4f);
    }

    @Override
    protected int[] calculateChaseTarget(PacMan pacman, Ghost blinky) {
        int col = pacman.getTileCol() + pacman.getDirection().dx * 4;
        int row = pacman.getTileRow() + pacman.getDirection().dy * 4;
        return new int[]{col, row};
    }
}
