package com.pacman.classic.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView
        implements SurfaceHolder.Callback, GameEngine.GameEventListener {

    // ── Layout geometry ──────────────────────────────────────────────────────
    private float tileSize;
    private float mazeOffsetX;
    private float mazeOffsetY;
    private float hudHeight;
    private float dpadSize;
    private float dpadY;    // top of dpad area

    // ── Game ─────────────────────────────────────────────────────────────────
    private GameEngine engine;
    private AudioHelper audio;

    // ── Game loop ─────────────────────────────────────────────────────────────
    private GameThread gameThread;
    private volatile boolean running;

    // ── Paint ────────────────────────────────────────────────────────────────
    private Paint wallPaint;
    private Paint dotPaint;
    private Paint pelletPaint;
    private Paint pacmanPaint;
    private Paint ghostPaintR, ghostPaintP, ghostPaintC, ghostPaintO;
    private Paint frightenedPaint, flashPaint;
    private Paint eyeWhitePaint, eyePupilPaint;
    private Paint hudPaint;
    private Paint scorePaint;
    private Paint overlayPaint;
    private Paint dpadPaint, dpadPressedPaint;
    private Paint livePaint;

    // ── Input ────────────────────────────────────────────────────────────────
    private GestureDetector gestureDetector;
    private Direction pendingDirection = null;
    // D-pad touch state
    private boolean dpadUpPressed, dpadDownPressed, dpadLeftPressed, dpadRightPressed;
    private boolean dpadVisible = true;
    /** true = swipe-only; false = D-Pad (default) */
    private boolean swipeMode = false;

    // ── Score display overlay ─────────────────────────────────────────────────
    private int showScoreVal = 0;
    private float showScoreTimer = 0f;
    private float showScoreX, showScoreY;

    public GameView(Context ctx) {
        super(ctx);
        init(ctx);
    }

    public GameView(Context ctx, android.util.AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx);
    }

    public GameView(Context ctx, android.util.AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        init(ctx);
    }

    private void init(Context ctx) {
        getHolder().addCallback(this);
        setFocusable(true);
        initPaints();
        gestureDetector = new GestureDetector(ctx, new SwipeListener());
    }

    /** Call before startGame() to apply the user's control preference. */
    public void setSwipeMode(boolean swipe) {
        this.swipeMode = swipe;
        this.dpadVisible = !swipe;
    }

    // Called by GameActivity after view is created
    public void startGame(GameEngine eng, AudioHelper audio) {
        this.engine = eng;
        this.audio = audio;
        eng.startNewGame();
    }

    // ── SurfaceHolder.Callback ───────────────────────────────────────────────
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        computeLayout();
        startGameThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        computeLayout();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopGameThread();
    }

    // ── Geometry ─────────────────────────────────────────────────────────────
    private void computeLayout() {
        int w = getWidth();
        int h = getHeight();
        hudHeight = h * 0.06f;
        // D-pad occupies bottom 22% of screen
        float dpadAreaHeight = h * 0.22f;
        float mazeAreaHeight = h - hudHeight - dpadAreaHeight;

        // Tile size: fit 28 cols and 31 rows inside maze area
        float tileByCols = (float) w / Maze.COLS;
        float tileByRows = mazeAreaHeight / Maze.ROWS;
        tileSize = Math.min(tileByCols, tileByRows);

        mazeOffsetX = (w - tileSize * Maze.COLS) / 2f;
        mazeOffsetY = hudHeight;

        dpadY = hudHeight + tileSize * Maze.ROWS + (dpadAreaHeight - Math.min(dpadAreaHeight, w * 0.55f)) / 2f;
        dpadSize = Math.min(dpadAreaHeight * 0.9f, w * 0.55f);
    }

    // ── Game thread ────────────────────────────────────────────────────────────
    private void startGameThread() {
        running = true;
        gameThread = new GameThread();
        gameThread.start();
    }

    private void stopGameThread() {
        running = false;
        if (gameThread != null) {
            try { gameThread.join(500); } catch (InterruptedException ignore) {}
        }
    }

    private class GameThread extends Thread {
        private static final long TARGET_NANOS = 1_000_000_000L / 60;

        @Override
        public void run() {
            long prev = System.nanoTime();
            while (running) {
                long now = System.nanoTime();
                float dt = (now - prev) / 1_000_000_000f;
                prev = now;
                if (dt > 0.05f) dt = 0.05f; // cap to 50ms

                // Apply pending swipe/dpad direction
                applyInput();

                if (engine != null) engine.update(dt);

                // Tick score overlay
                if (showScoreTimer > 0) showScoreTimer -= dt;

                drawFrame();

                long elapsed = System.nanoTime() - now;
                long sleep = (TARGET_NANOS - elapsed) / 1_000_000L;
                if (sleep > 0) {
                    try { Thread.sleep(sleep); } catch (InterruptedException ignore) {}
                }
            }
        }
    }

    private synchronized void applyInput() {
        if (pendingDirection != null && engine != null) {
            engine.setDirection(pendingDirection);
            pendingDirection = null;
        }
        // D-pad
        if (engine != null) {
            if (dpadUpPressed) engine.setDirection(Direction.UP);
            else if (dpadDownPressed) engine.setDirection(Direction.DOWN);
            else if (dpadLeftPressed) engine.setDirection(Direction.LEFT);
            else if (dpadRightPressed) engine.setDirection(Direction.RIGHT);
        }
    }

    // ── Drawing ───────────────────────────────────────────────────────────────
    private void drawFrame() {
        SurfaceHolder holder = getHolder();
        if (!holder.getSurface().isValid()) return;
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas != null && engine != null) {
                canvas.drawColor(Color.BLACK);
                drawHud(canvas);
                drawMaze(canvas);
                drawGhosts(canvas);
                drawPacman(canvas);
                drawDpad(canvas);
                drawOverlay(canvas);
                drawScorePopup(canvas);
            }
        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawHud(Canvas c) {
        float y = hudHeight * 0.75f;
        scorePaint.setTextSize(hudHeight * 0.6f);
        scorePaint.setColor(Color.WHITE);
        c.drawText("SCORE: " + engine.getScore(), mazeOffsetX, y, scorePaint);
        String best = "BEST: " + engine.getHighScore();
        float bw = scorePaint.measureText(best);
        c.drawText(best, getWidth() / 2f - bw / 2f, y, scorePaint);
        String lvl = "LV" + engine.getLevel();
        c.drawText(lvl, getWidth() - mazeOffsetX - scorePaint.measureText(lvl), y, scorePaint);
    }

    private void drawMaze(Canvas c) {
        Maze maze = engine.getMaze();
        float r = tileSize * 0.12f;
        float pr = tileSize * 0.22f;
        for (int row = 0; row < Maze.ROWS; row++) {
            for (int col = 0; col < Maze.COLS; col++) {
                float px = mazeOffsetX + col * tileSize;
                float py = mazeOffsetY + row * tileSize;
                int tile = maze.getTile(row, col);
                if (tile == Maze.WALL) {
                    drawWall(c, row, col, px, py, maze);
                } else if (tile == Maze.DOT) {
                    c.drawCircle(px + tileSize / 2f, py + tileSize / 2f, r, dotPaint);
                } else if (tile == Maze.POWER_PELLET) {
                    float pulse = 0.18f + 0.06f * (float) Math.sin(System.nanoTime() / 200_000_000.0);
                    c.drawCircle(px + tileSize / 2f, py + tileSize / 2f, tileSize * pulse, pelletPaint);
                } else if (tile == Maze.GHOST_DOOR) {
                    Paint p = new Paint();
                    p.setColor(0xFFFFB8FF);
                    p.setStrokeWidth(tileSize * 0.15f);
                    c.drawLine(px, py + tileSize / 2f, px + tileSize * 2f, py + tileSize / 2f, p);
                }
            }
        }
    }

    private void drawWall(Canvas c, int row, int col, float px, float py, Maze maze) {
        // Simple filled rect for walls
        RectF rect = new RectF(px + 0.5f, py + 0.5f, px + tileSize - 0.5f, py + tileSize - 0.5f);
        c.drawRoundRect(rect, tileSize * 0.15f, tileSize * 0.15f, wallPaint);
    }

    private void drawPacman(Canvas c) {
        PacMan p = engine.getPacman();
        float cx = mazeOffsetX + p.getX() * tileSize + tileSize / 2f;
        float cy = mazeOffsetY + p.getY() * tileSize + tileSize / 2f;
        float radius = tileSize * 0.44f;

        if (p.isDying()) {
            // Death animation: shrink
            float prog = Math.min(p.getDeathAnimProgress(), 1f);
            float angle = 360f * prog;
            RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            pacmanPaint.setStyle(Paint.Style.FILL);
            c.drawArc(oval, 0, 360f - angle, true, pacmanPaint);
            return;
        }

        float mouth = p.getMouthAngle();
        float startAngle = directionAngle(p.getDirection()) + mouth;
        float sweepAngle = 360f - mouth * 2f;
        RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        pacmanPaint.setStyle(Paint.Style.FILL);
        c.drawArc(oval, startAngle, sweepAngle, true, pacmanPaint);
    }

    private float directionAngle(Direction dir) {
        switch (dir) {
            case RIGHT: return 0f;
            case DOWN:  return 90f;
            case LEFT:  return 180f;
            case UP:    return 270f;
            default:    return 0f;
        }
    }

    private void drawGhosts(Canvas c) {
        Ghost[] ghosts = engine.getGhosts();
        Paint[] colors = {ghostPaintR, ghostPaintP, ghostPaintC, ghostPaintO};
        for (int i = 0; i < ghosts.length; i++) {
            Ghost g = ghosts[i];
            float cx = mazeOffsetX + g.getX() * tileSize + tileSize / 2f;
            float cy = mazeOffsetY + g.getY() * tileSize + tileSize / 2f;
            drawGhostAt(c, g, cx, cy, colors[i]);
        }
    }

    private void drawGhostAt(Canvas c, Ghost g, float cx, float cy, Paint bodyPaint) {
        float r = tileSize * 0.42f;
        GhostMode mode = g.getMode();

        Paint body;
        if (mode == GhostMode.FRIGHTENED) {
            body = g.isFlashing() ? flashPaint : frightenedPaint;
        } else if (mode == GhostMode.EATEN) {
            drawGhostEyes(c, cx, cy, r, g);
            return;
        } else {
            body = bodyPaint;
        }

        // Body: rounded rectangle top + flat/wave bottom
        RectF top = new RectF(cx - r, cy - r, cx + r, cy + r * 0.6f);
        c.drawOval(top, body);
        RectF bottom = new RectF(cx - r, cy - r * 0.1f, cx + r, cy + r);
        c.drawRect(bottom, body);

        // Skirt: three bumps at the bottom
        float bumpR = r / 3f;
        for (int i = 0; i < 3; i++) {
            float bx = cx - r + bumpR + i * bumpR * 2f;
            c.drawCircle(bx, cy + r, bumpR, body);
        }

        // Eyes
        if (mode != GhostMode.FRIGHTENED) {
            drawGhostEyes(c, cx, cy, r, g);
        } else {
            // Frightened eyes: two white dots + mouth
            c.drawCircle(cx - r * 0.3f, cy - r * 0.1f, r * 0.12f, eyeWhitePaint);
            c.drawCircle(cx + r * 0.3f, cy - r * 0.1f, r * 0.12f, eyeWhitePaint);
        }
    }

    private void drawGhostEyes(Canvas c, float cx, float cy, float r, Ghost g) {
        float ex = g.getEyeDirX() * r * 0.15f;
        float ey = g.getEyeDirY() * r * 0.15f;
        float eyeR = r * 0.22f;
        float pupilR = r * 0.12f;
        // Left eye
        c.drawCircle(cx - r * 0.3f, cy - r * 0.15f, eyeR, eyeWhitePaint);
        c.drawCircle(cx - r * 0.3f + ex, cy - r * 0.15f + ey, pupilR, eyePupilPaint);
        // Right eye
        c.drawCircle(cx + r * 0.3f, cy - r * 0.15f, eyeR, eyeWhitePaint);
        c.drawCircle(cx + r * 0.3f + ex, cy - r * 0.15f + ey, pupilR, eyePupilPaint);
    }

    private void drawDpad(Canvas c) {
        if (!dpadVisible) return;
        float w = getWidth();
        float cx = w / 2f;
        float cy = dpadY + dpadSize / 2f;
        float btn = dpadSize / 3f;

        drawDpadButton(c, cx, cy - btn, btn, dpadUpPressed, "\u25B2");    // ▲
        drawDpadButton(c, cx, cy + btn, btn, dpadDownPressed, "\u25BC");  // ▼
        drawDpadButton(c, cx - btn, cy, btn, dpadLeftPressed, "\u25C4");  // ◄
        drawDpadButton(c, cx + btn, cy, btn, dpadRightPressed, "\u25BA"); // ►
        // center circle
        c.drawCircle(cx, cy, btn * 0.35f, dpadPaint);
    }

    private void drawDpadButton(Canvas c, float cx, float cy, float size, boolean pressed, String arrow) {
        Paint bg = pressed ? dpadPressedPaint : dpadPaint;
        c.drawCircle(cx, cy, size * 0.45f, bg);
        hudPaint.setTextSize(size * 0.5f);
        hudPaint.setColor(Color.WHITE);
        float tw = hudPaint.measureText(arrow);
        c.drawText(arrow, cx - tw / 2f, cy + size * 0.18f, hudPaint);
    }

    private void drawOverlay(Canvas c) {
        if (engine == null) return;
        GameEngine.State st = engine.getState();
        switch (st) {
            case READY:
                drawCenterText(c, "READY!", 0xFFFFFF00, tileSize * 1.2f);
                break;
            case GAME_OVER:
                drawCenterText(c, "GAME OVER", 0xFFFF0000, tileSize * 1.2f);
                break;
            case LEVEL_COMPLETE:
                drawCenterText(c, "LEVEL " + engine.getLevel() + " CLEAR!", 0xFF00FF88, tileSize * 1.0f);
                break;
            case PAUSED:
                drawCenterText(c, "PAUSED", 0xFFFFFFFF, tileSize * 1.2f);
                break;
            default:
                break;
        }
        // Lives
        drawLives(c);
    }

    private void drawLives(Canvas c) {
        float r = tileSize * 0.3f;
        float startX = mazeOffsetX + r;
        float ly = mazeOffsetY + Maze.ROWS * tileSize + tileSize * 0.3f;
        for (int i = 0; i < engine.getLives(); i++) {
            float lx = startX + i * (r * 2.5f);
            RectF oval = new RectF(lx - r, ly - r, lx + r, ly + r);
            pacmanPaint.setStyle(Paint.Style.FILL);
            c.drawArc(oval, 30f, 300f, true, pacmanPaint);
        }
    }

    private void drawCenterText(Canvas c, String text, int color, float textSize) {
        overlayPaint.setColor(color);
        overlayPaint.setTextSize(textSize);
        overlayPaint.setTextAlign(Paint.Align.CENTER);
        float mazeH = Maze.ROWS * tileSize;
        float cx = mazeOffsetX + Maze.COLS * tileSize / 2f;
        float cy = mazeOffsetY + mazeH / 2f;
        c.drawText(text, cx, cy, overlayPaint);
    }

    private void drawScorePopup(Canvas c) {
        if (showScoreTimer <= 0) return;
        overlayPaint.setColor(Color.CYAN);
        overlayPaint.setTextSize(tileSize * 0.8f);
        overlayPaint.setTextAlign(Paint.Align.CENTER);
        c.drawText("+" + showScoreVal, showScoreX, showScoreY, overlayPaint);
    }

    // ── Paint init ────────────────────────────────────────────────────────────
    private void initPaints() {
        wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaint.setColor(0xFF2121DE);
        wallPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(0xFFFFB8AE);
        dotPaint.setStyle(Paint.Style.FILL);

        pelletPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pelletPaint.setColor(0xFFFFB8AE);
        pelletPaint.setStyle(Paint.Style.FILL);

        pacmanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pacmanPaint.setColor(0xFFFFD700);

        ghostPaintR = ghostPaint(0xFFFF0000);
        ghostPaintP = ghostPaint(0xFFFFB8FF);
        ghostPaintC = ghostPaint(0xFF00FFFF);
        ghostPaintO = ghostPaint(0xFFFFB852);
        frightenedPaint = ghostPaint(0xFF0000CC);
        flashPaint = ghostPaint(0xFFFFFFFF);

        eyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhitePaint.setColor(Color.WHITE);
        eyePupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePupilPaint.setColor(0xFF0000AA);

        hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudPaint.setColor(Color.WHITE);
        hudPaint.setTypeface(android.graphics.Typeface.MONOSPACE);

        scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTypeface(android.graphics.Typeface.MONOSPACE);

        overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        overlayPaint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD));
        overlayPaint.setTextAlign(Paint.Align.CENTER);

        dpadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dpadPaint.setColor(0x44FFFFFF);
        dpadPaint.setStyle(Paint.Style.FILL);

        dpadPressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dpadPressedPaint.setColor(0x88FFFFFF);
        dpadPressedPaint.setStyle(Paint.Style.FILL);

        livePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        livePaint.setColor(0xFFFFD700);
    }

    private Paint ghostPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    // ── Touch / Gesture ───────────────────────────────────────────────────────
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (swipeMode) {
            // Swipe-only: ignore D-Pad, forward all touches to gesture detector
            gestureDetector.onTouchEvent(ev);
        } else {
            // D-Pad mode: check dpad area first, swipe elsewhere disabled
            handleDpadTouch(ev);
        }
        return true;
    }

    private boolean handleDpadTouch(MotionEvent ev) {
        float w = getWidth();
        float cx = w / 2f;
        float cy = dpadY + dpadSize / 2f;
        float btn = dpadSize / 3f;
        float touchRadius = btn * 0.5f;

        float tx = ev.getX();
        float ty = ev.getY();

        boolean inDpad = Math.abs(tx - cx) < dpadSize / 2f && ty > dpadY && ty < dpadY + dpadSize;
        if (!inDpad) {
            if (ev.getAction() == MotionEvent.ACTION_UP) resetDpad();
            return false;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            resetDpad();
            return true;
        }

        resetDpad();
        // Determine which button was pressed
        if (dist(tx, ty, cx, cy - btn) < touchRadius * 1.5f) { dpadUpPressed = true; }
        else if (dist(tx, ty, cx, cy + btn) < touchRadius * 1.5f) { dpadDownPressed = true; }
        else if (dist(tx, ty, cx - btn, cy) < touchRadius * 1.5f) { dpadLeftPressed = true; }
        else if (dist(tx, ty, cx + btn, cy) < touchRadius * 1.5f) { dpadRightPressed = true; }

        return true;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void resetDpad() {
        dpadUpPressed = dpadDownPressed = dpadLeftPressed = dpadRightPressed = false;
    }

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
            if (e1 == null || e2 == null) return false;
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();
            if (Math.abs(dx) > Math.abs(dy)) {
                if (Math.abs(dx) > SWIPE_THRESHOLD && Math.abs(vx) > SWIPE_VELOCITY) {
                    synchronized (GameView.this) {
                        pendingDirection = dx > 0 ? Direction.RIGHT : Direction.LEFT;
                    }
                    return true;
                }
            } else {
                if (Math.abs(dy) > SWIPE_THRESHOLD && Math.abs(vy) > SWIPE_VELOCITY) {
                    synchronized (GameView.this) {
                        pendingDirection = dy > 0 ? Direction.DOWN : Direction.UP;
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) { return true; }
    }

    // ── GameEventListener ─────────────────────────────────────────────────────
    @Override
    public void onDotEaten() {
        if (audio != null) audio.playDot();
    }

    @Override
    public void onPowerPelletEaten() {
        if (audio != null) audio.playPowerPellet();
    }

    @Override
    public void onGhostEaten(int score) {
        if (audio != null) audio.playGhostEat();
        showScoreVal = score;
        showScoreTimer = 1.2f;
        if (engine != null) {
            PacMan p = engine.getPacman();
            showScoreX = mazeOffsetX + p.getX() * tileSize + tileSize / 2f;
            showScoreY = mazeOffsetY + p.getY() * tileSize;
        }
    }

    @Override
    public void onPacmanDied() {
        if (audio != null) audio.playDeath();
    }

    @Override
    public void onLevelComplete() {
        if (audio != null) audio.playLevelComplete();
    }

    @Override
    public void onGameOver() {
        // GameActivity will detect game over state and handle name entry
    }

    public GameEngine.State getGameState() {
        return engine != null ? engine.getState() : null;
    }
}
