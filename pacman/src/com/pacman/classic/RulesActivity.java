package com.pacman.classic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RulesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        Button btnBack = (Button) findViewById(R.id.btn_rules_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}
