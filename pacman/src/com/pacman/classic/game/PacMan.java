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
        if (mouthAngle <= 0f) {
            mouthAngle = 0f;
            mouthDelta = 2f;
        } else if (mouthAngle >= MAX_MOUTH_ANGLE) {
            mouthAngle = MAX_MOUTH_ANGLE;
            mouthDelta = -2f;
        }

        // Try to change direction if requested
        if (requestedDirection != direction && requestedDirection != Direction.NONE) {
            int nextCol = tileCol + requestedDirection.dx;
            int nextRow = tileRow + requestedDirection.dy;
            if (isAligned() && maze.isWalkableForPacman(nextRow, nextCol)) {
                direction = requestedDirection;
            }
        }

        if (direction == Direction.NONE) return;

        float moveAmount = SPEED * dt;
        float newX = x + direction.dx * moveAmount;
        float newY = y + direction.dy * moveAmount;

        // Check tunnel wrap
        int wrapCol = maze.tunnelWrap(tileRow, Math.round(newX));
        if (wrapCol >= 0) {
            x = wrapCol;
            tileCol = wrapCol;
            y = newY;
            tileRow = Math.round(newY);
            return;
        }

        int nextTileCol = tileCol + direction.dx;
        int nextTileRow = tileRow + direction.dy;

        if (maze.isWalkableForPacman(nextTileRow, nextTileCol)) {
            x = newX;
            y = newY;
            // Snap to tile center when aligned
            if (isAligned()) {
                tileCol = Math.round(x);
                tileRow = Math.round(y);
                x = tileCol;
                y = tileRow;
            }
        } else {
            // Can't move: snap to center
            x = tileCol;
            y = tileRow;
        }
    }

    /** True when position is close enough to tile center to turn. */
    private boolean isAligned() {
        float fx = Math.abs(x - Math.round(x));
        float fy = Math.abs(y - Math.round(y));
        return fx < 0.15f && fy < 0.15f;
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
