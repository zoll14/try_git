package com.pacman.classic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.CompoundButton;
import com.pacman.classic.game.SettingsManager;

public class SettingsActivity extends Activity {

    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsManager = new SettingsManager(this);

        Switch switchSound = (Switch) findViewById(R.id.switch_sound);
        switchSound.setChecked(settingsManager.isSoundEnabled());
        switchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                settingsManager.setSoundEnabled(checked);
            }
        });

        Switch switchMusic = (Switch) findViewById(R.id.switch_music);
        switchMusic.setChecked(settingsManager.isMusicEnabled());
        switchMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                settingsManager.setMusicEnabled(checked);
            }
        });

        Button btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
    }
}
