package com.pacman.classic.game;

public class Maze {
    public static final int COLS = 28;
    public static final int ROWS = 31;

    // Tile constants
    public static final int WALL = 0;
    public static final int DOT = 1;
    public static final int POWER_PELLET = 2;
    public static final int EMPTY = 3;
    public static final int GHOST_HOUSE = 4;
    public static final int GHOST_DOOR = 5;
    public static final int TUNNEL = 6;

    // Ghost starting/home positions
    public static final int GHOST_HOUSE_ROW = 13;
    public static final int GHOST_HOUSE_LEFT_COL = 11;
    public static final int GHOST_HOUSE_RIGHT_COL = 16;
    public static final int GHOST_DOOR_ROW = 11;
    public static final int GHOST_DOOR_COL = 13;

    // Pacman start position
    public static final int PACMAN_START_COL = 13;
    public static final int PACMAN_START_ROW = 23;

    // Left/right tunnel row
    public static final int TUNNEL_ROW = 14;

    private static final int[][] BASE_LAYOUT = {
        // Row 0
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        // Row 1
        {0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0},
        // Row 2
        {0,1,0,0,0,0,1,0,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,1,0},
        // Row 3
        {0,2,0,0,0,0,1,0,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,2,0},
        // Row 4
        {0,1,0,0,0,0,1,0,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,1,0},
        // Row 5
        {0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
        // Row 6
        {0,1,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,1,0},
        // Row 7
        {0,1,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,1,0},
        // Row 8
        {0,1,1,1,1,1,1,0,0,1,1,1,1,0,0,1,1,1,1,0,0,1,1,1,1,1,1,0},
        // Row 9
        {0,0,0,0,0,0,1,0,0,0,0,0,3,0,0,3,0,0,0,0,0,1,0,0,0,0,0,0},
        // Row 10
        {0,0,0,0,0,0,1,0,0,3,3,3,3,3,3,3,3,3,3,0,0,1,0,0,0,0,0,0},
        // Row 11
        {0,0,0,0,0,0,1,0,0,3,0,0,0,5,5,0,0,0,3,0,0,1,0,0,0,0,0,0},
        // Row 12 (ghost door row)
        {6,6,6,6,6,6,1,3,3,3,0,4,4,4,4,4,4,0,3,3,3,1,6,6,6,6,6,6},
        // Row 13 (ghost house center)
        {3,3,3,3,3,3,1,0,0,3,0,4,4,4,4,4,4,0,3,0,0,1,3,3,3,3,3,3},
        // Row 14 (tunnel row)
        {6,6,6,6,6,6,1,0,0,3,3,3,3,3,3,3,3,3,3,0,0,1,6,6,6,6,6,6},
        // Row 15
        {0,0,0,0,0,0,1,0,0,3,0,0,0,0,0,0,0,0,3,0,0,1,0,0,0,0,0,0},
        // Row 16
        {0,0,0,0,0,0,1,0,0,3,0,0,0,0,0,0,0,0,3,0,0,1,0,0,0,0,0,0},
        // Row 17
        {0,0,0,0,0,0,1,0,0,3,1,1,1,1,1,1,1,1,3,0,0,1,0,0,0,0,0,0},
        // Row 18
        {0,0,0,0,0,0,1,0,0,3,1,0,0,0,0,0,0,1,3,0,0,1,0,0,0,0,0,0},
        // Row 19
        {0,0,0,0,0,0,1,0,0,3,3,3,3,3,3,3,3,3,3,0,0,1,0,0,0,0,0,0},
        // Row 20
        {0,0,0,0,0,0,1,0,0,3,0,0,0,0,0,0,0,0,3,0,0,1,0,0,0,0,0,0},
        // Row 21
        {0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0},
        // Row 22
        {0,1,0,0,0,0,1,0,0,0,0,0,1,3,3,1,0,0,0,0,0,1,0,0,0,0,1,0},
        // Row 23 (pacman start row)
        {0,1,0,0,0,0,1,0,0,0,0,0,1,3,3,1,0,0,0,0,0,1,0,0,0,0,1,0},
        // Row 24
        {0,2,1,1,0,0,1,1,1,1,1,1,1,3,3,1,1,1,1,1,1,1,0,0,1,1,2,0},
        // Row 25
        {0,0,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,1,0,0,0},
        // Row 26
        {0,0,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,1,0,0,0},
        // Row 27
        {0,1,1,1,1,1,1,0,0,1,1,1,1,0,0,1,1,1,1,0,0,1,1,1,1,1,1,0},
        // Row 28
        {0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0},
        // Row 29
        {0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0},
        // Row 30
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
    };

    private int[][] tiles;
    private int dotsRemaining;
    private int totalDots;

    public Maze() {
        reset();
    }

    public void reset() {
        tiles = new int[ROWS][COLS];
        dotsRemaining = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                tiles[r][c] = BASE_LAYOUT[r][c];
                if (tiles[r][c] == DOT || tiles[r][c] == POWER_PELLET) {
                    dotsRemaining++;
                }
            }
        }
        totalDots = dotsRemaining;
    }

    public int getTile(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return WALL;
        }
        return tiles[row][col];
    }

    public boolean isWalkable(int row, int col) {
        int t = getTile(row, col);
        return t != WALL;
    }

    public boolean isWalkableForPacman(int row, int col) {
        int t = getTile(row, col);
        return t != WALL && t != GHOST_HOUSE && t != GHOST_DOOR;
    }

    public boolean isWalkableForGhost(int row, int col) {
        int t = getTile(row, col);
        return t != WALL;
    }

    /** Returns true if tile had a collectible (dot or power pellet). */
    public boolean collectTile(int row, int col) {
        int t = getTile(row, col);
        if (t == DOT || t == POWER_PELLET) {
            tiles[row][col] = EMPTY;
            dotsRemaining--;
            return true;
        }
        return false;
    }

    public boolean isPowerPellet(int row, int col) {
        return getTile(row, col) == POWER_PELLET;
    }

    public int getDotsRemaining() {
        return dotsRemaining;
    }

    public int getTotalDots() {
        return totalDots;
    }

    public boolean isLevelComplete() {
        return dotsRemaining <= 0;
    }

    /** Wraps column for tunnel tiles. Returns -1 if no wrap needed. */
    public int tunnelWrap(int row, int col) {
        if (getTile(row, col) == TUNNEL || getTile(row, Math.max(0, col)) == TUNNEL) {
            if (col < 0) return COLS - 1;
            if (col >= COLS) return 0;
        }
        return -1;
    }
}
