package com.pacman.classic.game;

import java.util.Random;

public abstract class Ghost {
    protected float x;
    protected float y;
    protected int tileCol;
    protected int tileRow;

    protected Direction direction;
    protected GhostMode mode;
    protected GhostMode previousMode;

    // Speed: tiles per second
    protected static final float NORMAL_SPEED    = 6.5f;
    protected static final float FRIGHTENED_SPEED = 4.0f;
    protected static final float EATEN_SPEED      = 12.0f;
    protected static final float HOUSE_SPEED      = 3.0f;

    // Scatter corner targets
    protected final int scatterCol;
    protected final int scatterRow;

    // Starting position inside ghost house
    protected final int homeCol;
    protected final int homeRow;

    // How long ghost stays in house before exiting
    protected float houseTimer;
    protected boolean exitingHouse;

    // Frightened flash: last 2 seconds of frightened
    protected float frightenedTimer;
    public static final float FRIGHTENED_DURATION = 8f;

    protected boolean eaten;
    protected int eyeDirX;
    protected int eyeDirY;

    protected Random random = new Random();

    // Used for eaten ghost score multiplier feedback
    private int eatScore;

    protected Ghost(int homeCol, int homeRow, int scatterCol, int scatterRow, float houseDelay) {
        this.homeCol = homeCol;
        this.homeRow = homeRow;
        this.scatterCol = scatterCol;
        this.scatterRow = scatterRow;
        this.houseTimer = houseDelay;
        reset();
    }

    public void reset() {
        tileCol = homeCol;
        tileRow = homeRow;
        x = homeCol;
        y = homeRow;
        direction = Direction.UP;
        mode = GhostMode.SCATTER;
        previousMode = GhostMode.SCATTER;
        frightenedTimer = 0f;
        exitingHouse = false;
        eaten = false;
        eyeDirX = 0;
        eyeDirY = -1;
        eatScore = 0;
    }

    public void update(float dt, Maze maze, PacMan pacman, Ghost blinky) {
        if (mode == GhostMode.FRIGHTENED) {
            frightenedTimer -= dt;
            if (frightenedTimer <= 0f) {
                mode = previousMode;
                frightenedTimer = 0f;
            }
        }

        // Handle exiting house
        if (exitingHouse) {
            moveToExit(dt, maze);
            return;
        }
        if (isInHouse(maze)) {
            bounceInHouse(dt);
            houseTimer -= dt;
            if (houseTimer <= 0f) {
                exitingHouse = true;
            }
            return;
        }

        float speed = currentSpeed();
        move(dt, speed, maze, pacman, blinky);
    }

    private boolean isInHouse(Maze maze) {
        return maze.getTile(tileRow, tileCol) == Maze.GHOST_HOUSE;
    }

    private void bounceInHouse(float dt) {
        y += direction.dy * HOUSE_SPEED * dt;
        if (y <= homeRow - 0.8f) {
            direction = Direction.DOWN;
        } else if (y >= homeRow + 0.8f) {
            direction = Direction.UP;
        }
        tileRow = (int)y;
    }

    private void moveToExit(float dt, Maze maze) {
        // Move toward door column first, then up through door
        int doorCol = Maze.GHOST_DOOR_COL;
        int doorRow = Maze.GHOST_DOOR_ROW;

        if (Math.round(x) != doorCol) {
            float dx = (doorCol - x);
            float step = Math.min(Math.abs(dx), HOUSE_SPEED * dt);
            x += Math.signum(dx) * step;
            tileCol = Math.round(x);
        } else {
            y -= HOUSE_SPEED * dt;
            tileRow = Math.round(y);
            if (tileRow <= doorRow) {
                x = doorCol;
                tileCol = doorCol;
                y = doorRow - 1;
                tileRow = doorRow - 1;
                direction = Direction.LEFT;
                exitingHouse = false;
            }
        }
    }

    private void move(float dt, float speed, Maze maze, PacMan pacman, Ghost blinky) {
        // Calculate target tile based on mode
        int targetCol, targetRow;

        if (mode == GhostMode.EATEN) {
            targetCol = Maze.GHOST_DOOR_COL;
            targetRow = Maze.GHOST_DOOR_ROW;
            // If we reached ghost house, go back in
            if (tileCol == targetCol && tileRow <= targetRow + 1) {
                mode = previousMode;
                eaten = false;
                exitingHouse = true;
                houseTimer = 2f;
                return;
            }
        } else if (mode == GhostMode.FRIGHTENED) {
            targetCol = -1;
            targetRow = -1;
        } else if (mode == GhostMode.SCATTER) {
            targetCol = scatterCol;
            targetRow = scatterRow;
        } else {
            int[] target = calculateChaseTarget(pacman, blinky);
            targetCol = target[0];
            targetRow = target[1];
        }

        navigateTo(dt, speed, maze, targetCol, targetRow);
    }

    private void navigateTo(float dt, float speed, Maze maze, int targetCol, int targetRow) {
        float moveAmount = speed * dt;

        // Tunnel wrap
        if (tileRow == Maze.TUNNEL_ROW) {
            float nx = x + direction.dx * moveAmount;
            if (nx < 0)          { x = Maze.COLS - 1; tileCol = Maze.COLS - 1; return; }
            if (nx >= Maze.COLS) { x = 0;              tileCol = 0;             return; }
        }

        // At tile center: pick next direction
        if (atCenter()) {
            Direction best = chooseBestDirection(maze, targetCol, targetRow);
            direction = best;
        }

        int nextCol = tileCol + direction.dx;
        int nextRow = tileRow + direction.dy;

        if (!maze.isWalkableForGhost(nextRow, nextCol)) {
            return; // blocked — wait for next direction decision
        }

        // Move
        x += direction.dx * moveAmount;
        y += direction.dy * moveAmount;

        // Snap when crossing (or reaching) next tile center
        if      (direction.dx < 0 && x <= nextCol) { x = nextCol; tileCol = nextCol; }
        else if (direction.dx > 0 && x >= nextCol) { x = nextCol; tileCol = nextCol; }
        if      (direction.dy < 0 && y <= nextRow) { y = nextRow; tileRow = nextRow; }
        else if (direction.dy > 0 && y >= nextRow) { y = nextRow; tileRow = nextRow; }
    }

    private boolean atCenter() {
        return Math.abs(x - tileCol) < 0.01f && Math.abs(y - tileRow) < 0.01f;
    }

    private Direction chooseBestDirection(Maze maze, int targetCol, int targetRow) {
        Direction[] candidates = {Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT};
        Direction best = direction;
        float bestDist = Float.MAX_VALUE;
        boolean frightened = (mode == GhostMode.FRIGHTENED);

        for (Direction d : candidates) {
            if (d == direction.opposite()) continue; // No 180-turn
            int nextCol = tileCol + d.dx;
            int nextRow = tileRow + d.dy;
            if (!maze.isWalkableForGhost(nextRow, nextCol)) continue;
            // Ghosts can't go up at specific intersections (not enforced for simplicity)
            if (frightened) {
                // Random movement
                if (random.nextBoolean()) {
                    best = d;
                    break;
                }
                continue;
            }
            float dist = dist(nextCol, nextRow, targetCol, targetRow);
            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }

        // If frightened and no direction chosen, pick any valid
        if (frightened && best == direction) {
            for (Direction d : candidates) {
                if (d == direction.opposite()) continue;
                int nextCol = tileCol + d.dx;
                int nextRow = tileRow + d.dy;
                if (maze.isWalkableForGhost(nextRow, nextCol)) {
                    best = d;
                    break;
                }
            }
        }

        eyeDirX = best.dx;
        eyeDirY = best.dy;
        return best;
    }

    protected float dist(int c1, int r1, int c2, int r2) {
        float dc = c1 - c2;
        float dr = r1 - r2;
        return dc * dc + dr * dr;
    }

    private float currentSpeed() {
        switch (mode) {
            case FRIGHTENED: return FRIGHTENED_SPEED;
            case EATEN:      return EATEN_SPEED;
            default:         return NORMAL_SPEED;
        }
    }

    public void setFrightened() {
        if (mode != GhostMode.EATEN) {
            previousMode = (mode == GhostMode.FRIGHTENED) ? previousMode : mode;
            mode = GhostMode.FRIGHTENED;
            frightenedTimer = FRIGHTENED_DURATION;
            direction = direction.opposite();
        }
    }

    public void setEaten(int score) {
        mode = GhostMode.EATEN;
        eaten = true;
        eatScore = score;
    }

    public void setMode(GhostMode newMode) {
        if (mode != GhostMode.FRIGHTENED && mode != GhostMode.EATEN) {
            if (mode != newMode) {
                direction = direction.opposite();
            }
            mode = newMode;
            previousMode = newMode;
        } else {
            previousMode = newMode;
        }
    }

    /** Each ghost subclass defines its chase target. */
    protected abstract int[] calculateChaseTarget(PacMan pacman, Ghost blinky);

    public float getX() { return x; }
    public float getY() { return y; }
    public int getTileCol() { return tileCol; }
    public int getTileRow() { return tileRow; }
    public GhostMode getMode() { return mode; }
    public boolean isEaten() { return eaten; }
    public float getFrightenedTimer() { return frightenedTimer; }
    public boolean isFlashing() { return frightenedTimer > 0 && frightenedTimer < 2f; }
    public int getEyeDirX() { return eyeDirX; }
    public int getEyeDirY() { return eyeDirY; }
    public int getEatScore() { return eatScore; }
}
