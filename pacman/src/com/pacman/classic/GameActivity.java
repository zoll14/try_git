package com.pacman.classic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.pacman.classic.game.AudioHelper;
import com.pacman.classic.game.GameEngine;
import com.pacman.classic.game.GameView;
import com.pacman.classic.game.HighScoreManager;
import com.pacman.classic.game.SettingsManager;

public class GameActivity extends Activity {

    private GameView gameView;
    private GameEngine gameEngine;
    private AudioHelper audioHelper;
    private HighScoreManager highScoreManager;
    private SettingsManager settingsManager;

    private boolean gameOverHandled = false;
    private final Runnable checkGameOverRunnable = new Runnable() {
        public void run() {
            GameEngine.State st = gameView.getGameState();
            if (st == GameEngine.State.GAME_OVER && !gameOverHandled) {
                gameOverHandled = true;
                runOnUiThread(new Runnable() {
                    public void run() {
                        showGameOverDialog();
                    }
                });
            } else if (st != GameEngine.State.GAME_OVER) {
                gameOverHandled = false;
                gameView.postDelayed(this, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        settingsManager = new SettingsManager(this);
        highScoreManager = new HighScoreManager(this);

        audioHelper = new AudioHelper(this,
            settingsManager.isSoundEnabled(),
            settingsManager.isMusicEnabled());

        gameEngine = new GameEngine(null); // listener wired through GameView
        gameView = (GameView) findViewById(R.id.game_view);
        gameEngine = new GameEngine(gameView);
        gameView.setSwipeMode(settingsManager.isSwipeEnabled());
        gameView.startGame(gameEngine, audioHelper);

        // Load saved high score into engine
        java.util.List<HighScoreManager.ScoreEntry> scores = highScoreManager.getScores();
        if (!scores.isEmpty()) {
            gameEngine.updateHighScore(scores.get(0).score);
        }

        gameView.postDelayed(checkGameOverRunnable, 500);
    }

    private void showGameOverDialog() {
        int finalScore = gameEngine.getScore();
        boolean isHigh = highScoreManager.isHighScore(finalScore);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GAME OVER  Score: " + finalScore);
        builder.setCancelable(false);

        if (isHigh) {
            builder.setMessage("New High Score! Enter your name:");
            final EditText input = new EditText(this);
            input.setHint("AAA");
            input.setMaxLines(1);
            builder.setView(input);
            builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "AAA";
                    highScoreManager.addScore(name, gameEngine.getScore());
                    showRestartDialog();
                }
            });
        } else {
            builder.setMessage("Keep trying!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    showRestartDialog();
                }
            });
        }
        builder.show();
    }

    private void showRestartDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Play again?");
        b.setCancelable(false);
        b.setPositiveButton("PLAY AGAIN", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int w) {
                gameOverHandled = false;
                gameEngine.startNewGame();
                java.util.List<HighScoreManager.ScoreEntry> sc = highScoreManager.getScores();
                if (!sc.isEmpty()) gameEngine.updateHighScore(sc.get(0).score);
                gameView.postDelayed(checkGameOverRunnable, 500);
            }
        });
        b.setNegativeButton("MENU", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int w) {
                finish();
            }
        });
        b.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameEngine != null) gameEngine.pause();
        if (audioHelper != null) audioHelper.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameEngine != null) gameEngine.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioHelper != null) audioHelper.release();
    }
}
