package com.pacman.classic.game;

/** Clyde (Orange): Chases Pacman when far (> 8 tiles), scatters to corner when close. */
public class Clyde extends Ghost {
    private static final float SCATTER_DISTANCE = 8f * 8f; // squared

    public Clyde() {
        super(15, 13, 0, 29, 12f);
    }

    @Override
    protected int[] calculateChaseTarget(PacMan pacman, Ghost blinky) {
        float d = dist(tileCol, tileRow, pacman.getTileCol(), pacman.getTileRow());
        if (d > SCATTER_DISTANCE) {
            return new int[]{pacman.getTileCol(), pacman.getTileRow()};
        } else {
            return new int[]{scatterCol, scatterRow};
        }
    }
}
