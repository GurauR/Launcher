package com.example.launcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get the shared preferences for the app
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the checkbox view from the layout
        Button dockAppsButton = findViewById(R.id.dock_apps_button);
        Button leftAppButton = findViewById(R.id.left_app_button);

        dockAppsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSettings(v, DockAppsActivity.class);
            }
        });

        leftAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSettings(v, LeftAppActivity.class);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void launchSettings(View view, Class ActivityClass) {
        Intent intent = new Intent(this, ActivityClass);
        startActivity(intent);
    }
}
