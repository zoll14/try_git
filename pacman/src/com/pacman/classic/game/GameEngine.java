package com.pacman.classic.game;

public class GameEngine {
    public enum State {
        READY, PLAYING, PACMAN_DYING, LEVEL_COMPLETE, GAME_OVER, PAUSED
    }

    // Scoring
    public static final int DOT_SCORE          = 10;
    public static final int POWER_PELLET_SCORE = 50;
    public static final int BASE_GHOST_SCORE   = 200;

    // Scatter/chase timing (seconds)
    private static final float[] SCATTER_TIMES = {7f, 7f, 5f, 5f};
    private static final float[] CHASE_TIMES   = {20f, 20f, 20f, Float.MAX_VALUE};

    private Maze maze;
    private PacMan pacman;
    private Ghost[] ghosts;
    private Blinky blinky;

    private State state;
    private int score;
    private int lives;
    private int level;
    private int highScore;

    private float stateTimer;       // used for READY / LEVEL_COMPLETE / DYING delays
    private float modeTimer;        // scatter/chase cycle
    private int modeIndex;
    private boolean inScatter;

    private int ghostsEatenInRow;   // for score doubling: 200, 400, 800, 1600
    private int lastEatScoreShow;

    private GameEventListener listener;

    public interface GameEventListener {
        void onDotEaten();
        void onPowerPelletEaten();
        void onGhostEaten(int score);
        void onPacmanDied();
        void onLevelComplete();
        void onGameOver();
        void onSpeedBoostCollected();
        void onShieldCollected();
        void onShieldUsed();
    }

    public GameEngine(GameEventListener listener) {
        this.listener = listener;
        initLevel();
    }

    private void initLevel() {
        maze = new Maze();
        pacman = new PacMan();
        blinky = new Blinky();
        Ghost pinky = new Pinky();
        Ghost inky  = new Inky();
        Ghost clyde = new Clyde();
        ghosts = new Ghost[]{blinky, pinky, inky, clyde};
        ghostsEatenInRow = 0;
        modeIndex = 0;
        inScatter = true;
        modeTimer = SCATTER_TIMES[0];
        state = State.READY;
        stateTimer = 3f;  // "READY!" for 3 seconds
    }

    public void startNewGame() {
        score = 0;
        lives = 3;
        level = 1;
        initLevel();
    }

    public void update(float dt) {
        switch (state) {
            case READY:
                stateTimer -= dt;
                if (stateTimer <= 0f) {
                    state = State.PLAYING;
                }
                break;

            case PLAYING:
                updatePlaying(dt);
                break;

            case PACMAN_DYING:
                stateTimer -= dt;
                pacman.startDying();  // keeps anim running
                if (stateTimer <= 0f || pacman.isDeathAnimComplete()) {
                    if (lives > 0) {
                        resetAfterDeath();
                    } else {
                        state = State.GAME_OVER;
                        if (listener != null) listener.onGameOver();
                    }
                }
                break;

            case LEVEL_COMPLETE:
                stateTimer -= dt;
                if (stateTimer <= 0f) {
                    level++;
                    initLevel();
                }
                break;

            case GAME_OVER:
            case PAUSED:
                break;
        }
    }

    private void updatePlaying(float dt) {
        // Scatter/chase mode cycling
        modeTimer -= dt;
        if (modeTimer <= 0f) {
            inScatter = !inScatter;
            modeIndex = Math.min(modeIndex + 1, SCATTER_TIMES.length - 1);
            float nextTime = inScatter ? SCATTER_TIMES[modeIndex] : CHASE_TIMES[modeIndex];
            modeTimer = nextTime;
            GhostMode nextMode = inScatter ? GhostMode.SCATTER : GhostMode.CHASE;
            for (Ghost g : ghosts) {
                g.setMode(nextMode);
            }
        }

        pacman.update(dt, maze);

        for (Ghost g : ghosts) {
            g.update(dt, maze, pacman, blinky);
        }

        // Collect tile
        int pc = pacman.getTileCol();
        int pr = pacman.getTileRow();
        int collected = maze.collectTile(pr, pc);
        if (collected == Maze.DOT) {
            score += DOT_SCORE;
            if (listener != null) listener.onDotEaten();
        } else if (collected == Maze.POWER_PELLET) {
            score += POWER_PELLET_SCORE;
            ghostsEatenInRow = 0;
            for (Ghost g : ghosts) g.setFrightened();
            if (listener != null) listener.onPowerPelletEaten();
        } else if (collected == Maze.SPEED_BOOST) {
            pacman.applySpeedBoost(5f);
            if (listener != null) listener.onSpeedBoostCollected();
        } else if (collected == Maze.SHIELD) {
            pacman.applyShield();
            if (listener != null) listener.onShieldCollected();
        }

        // Ghost collisions
        for (Ghost g : ghosts) {
            if (!overlaps(pacman, g)) continue;
            if (g.getMode() == GhostMode.FRIGHTENED) {
                ghostsEatenInRow++;
                int eatScore = BASE_GHOST_SCORE * (1 << (ghostsEatenInRow - 1));
                score += eatScore;
                g.setEaten(eatScore);
                if (listener != null) listener.onGhostEaten(eatScore);
            } else if (g.getMode() != GhostMode.EATEN) {
                if (pacman.consumeShield()) {
                    // Shield absorbs the hit — scare the offending ghost away
                    g.setFrightened();
                    if (listener != null) listener.onShieldUsed();
                } else {
                    lives--;
                    state = State.PACMAN_DYING;
                    stateTimer = 2.5f;
                    if (listener != null) listener.onPacmanDied();
                    return;
                }
            }
        }

        // Level complete?
        if (maze.isLevelComplete()) {
            state = State.LEVEL_COMPLETE;
            stateTimer = 3f;
            if (listener != null) listener.onLevelComplete();
        }
    }

    private boolean overlaps(PacMan p, Ghost g) {
        float dx = Math.abs(p.getX() - g.getX());
        float dy = Math.abs(p.getY() - g.getY());
        return dx < 0.7f && dy < 0.7f;
    }

    private void resetAfterDeath() {
        pacman.reset();
        for (Ghost g : ghosts) {
            g.reset();
        }
        ghostsEatenInRow = 0;
        modeIndex = 0;
        inScatter = true;
        modeTimer = SCATTER_TIMES[0];
        state = State.READY;
        stateTimer = 3f;
    }

    public void setDirection(Direction dir) {
        if (state == State.PLAYING) {
            pacman.setRequestedDirection(dir);
        }
    }

    public void pause() {
        if (state == State.PLAYING) state = State.PAUSED;
    }

    public void resume() {
        if (state == State.PAUSED) state = State.PLAYING;
    }

    public void updateHighScore(int hs) {
        this.highScore = Math.max(highScore, hs);
    }

    // Getters
    public Maze getMaze()        { return maze; }
    public PacMan getPacman()    { return pacman; }
    public Ghost[] getGhosts()   { return ghosts; }
    public State getState()      { return state; }
    public int getScore()        { return score; }
    public int getLives()        { return lives; }
    public int getLevel()        { return level; }
    public int getHighScore()    { return highScore; }
    public boolean isInScatter() { return inScatter; }
}
