package com.pacman.classic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.pacman.classic.game.HighScoreManager;
import java.util.List;

public class HighScoreActivity extends Activity {

    private HighScoreManager highScoreManager;
    private ListView listView;
    private TextView txtNoScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        highScoreManager = new HighScoreManager(this);
        listView = (ListView) findViewById(R.id.list_scores);
        txtNoScores = (TextView) findViewById(R.id.txt_no_scores);

        Button btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });

        Button btnClear = (Button) findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(HighScoreActivity.this)
                    .setTitle("Clear Scores")
                    .setMessage("Clear all high scores?")
                    .setPositiveButton("CLEAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) {
                            highScoreManager.clearScores();
                            refreshList();
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
            }
        });

        refreshList();
    }

    private void refreshList() {
        final List<HighScoreManager.ScoreEntry> scores = highScoreManager.getScores();
        if (scores.isEmpty()) {
            listView.setVisibility(View.GONE);
            txtNoScores.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            txtNoScores.setVisibility(View.GONE);
            listView.setAdapter(new ArrayAdapter<HighScoreManager.ScoreEntry>(
                    this, android.R.layout.simple_list_item_2, android.R.id.text1, scores) {
                @Override
                public View getView(int pos, View convertView, ViewGroup parent) {
                    View v = super.getView(pos, convertView, parent);
                    HighScoreManager.ScoreEntry entry = scores.get(pos);
                    TextView t1 = (TextView) v.findViewById(android.R.id.text1);
                    TextView t2 = (TextView) v.findViewById(android.R.id.text2);
                    t1.setTextColor(0xFFFFD700);
                    t1.setTypeface(android.graphics.Typeface.MONOSPACE);
                    t1.setText((pos + 1) + ".  " + entry.name);
                    t2.setTextColor(0xFFFFFFFF);
                    t2.setTypeface(android.graphics.Typeface.MONOSPACE);
                    t2.setText(String.valueOf(entry.score));
                    v.setBackgroundColor(0xFF000000);
                    return v;
                }
            });
        }
    }
}
