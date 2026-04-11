package com.pacman.classic.game;

public class PacMan {
    // Position in tile units (float for smooth movement)
    private float x;
    private float y;
    // Which tile we're in
    private int tileCol;
    private int tileRow;

    private Direction direction;
    private Direction requestedDirection;

    // Animation
    private float mouthAngle;
    private float mouthDelta;
    private boolean dying;
    private float deathAnimProgress; // 0..1

    // Speed in tiles per second
    private static final float SPEED = 7.5f;
    private static final float MAX_MOUTH_ANGLE = 45f;

    public PacMan() {
        reset();
    }

    public void reset() {
        tileCol = Maze.PACMAN_START_COL;
        tileRow = Maze.PACMAN_START_ROW;
        x = tileCol;
        y = tileRow;
        direction = Direction.NONE;
        requestedDirection = Direction.LEFT;
        mouthAngle = MAX_MOUTH_ANGLE;
        mouthDelta = -2f;
        dying = false;
        deathAnimProgress = 0f;
    }

    public void update(float dt, Maze maze) {
        if (dying) {
            deathAnimProgress += dt * 1.2f;
            return;
        }

        // Animate mouth
        mouthAngle += mouthDelta * 180f * dt;
        if (mouthAngle <= 0f)             { mouthAngle = 0f;             mouthDelta =  2f; }
        else if (mouthAngle >= MAX_MOUTH_ANGLE) { mouthAngle = MAX_MOUTH_ANGLE; mouthDelta = -2f; }

        // At tile center: honour queued direction change
        if (atCenter() && requestedDirection != Direction.NONE) {
            int nc = tileCol + requestedDirection.dx;
            int nr = tileRow + requestedDirection.dy;
            if (maze.isWalkableForPacman(nr, nc)) {
                direction = requestedDirection;
            }
        }

        if (direction == Direction.NONE) return;

        int nextCol = tileCol + direction.dx;
        int nextRow = tileRow + direction.dy;

        // Blocked at tile center — stop
        if (atCenter() && !maze.isWalkableForPacman(nextRow, nextCol)) {
            return;
        }

        float moveAmount = SPEED * dt;

        // Tunnel wrap
        int wrapCol = maze.tunnelWrap(tileRow, (int)(x + direction.dx * moveAmount));
        if (wrapCol >= 0) {
            x = wrapCol; tileCol = wrapCol;
            y += direction.dy * moveAmount;
            tileRow = Math.round(y);
            return;
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

    /** True when position is exactly on the tile-center (set by snapping). */
    private boolean atCenter() {
        return Math.abs(x - tileCol) < 0.01f && Math.abs(y - tileRow) < 0.01f;
    }

    public void setRequestedDirection(Direction dir) {
        this.requestedDirection = dir;
    }

    public void startDying() {
        dying = true;
        deathAnimProgress = 0f;
        direction = Direction.NONE;
    }

    public boolean isDeathAnimComplete() {
        return dying && deathAnimProgress >= 1f;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getTileCol() { return tileCol; }
    public int getTileRow() { return tileRow; }
    public Direction getDirection() { return direction; }
    public float getMouthAngle() { return mouthAngle; }
    public boolean isDying() { return dying; }
    public float getDeathAnimProgress() { return deathAnimProgress; }
}
