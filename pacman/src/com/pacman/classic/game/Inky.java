package com.pacman.classic.game;

/** Inky (Cyan): Complex targeting using Blinky's position and Pacman's facing direction. */
public class Inky extends Ghost {
    public Inky() {
        super(11, 13, 27, 30, 8f);
    }

    @Override
    protected int[] calculateChaseTarget(PacMan pacman, Ghost blinky) {
        // 2 tiles ahead of Pacman
        int pivotCol = pacman.getTileCol() + pacman.getDirection().dx * 2;
        int pivotRow = pacman.getTileRow() + pacman.getDirection().dy * 2;
        // Vector from Blinky to pivot, doubled
        int targetCol = pivotCol + (pivotCol - blinky.getTileCol());
        int targetRow = pivotRow + (pivotRow - blinky.getTileRow());
        return new int[]{targetCol, targetRow};
    }
}
