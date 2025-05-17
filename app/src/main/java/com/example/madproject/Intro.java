package com.example.madproject;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Intro extends AppCompatActivity {

    TextView loginText;
    Button accBtn;
    MediaPlayer bgMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            initialize();
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🎵 Initialize and start background music
        bgMusic = MediaPlayer.create(this, R.raw.pokedexmusic);
        bgMusic.setLooping(true); // Loop music
        bgMusic.start();
    }

    private void initialize() {
        loginText = findViewById(R.id.loginText);
        accBtn = findViewById(R.id.accBtn);

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(Intro.this, MainActivity.class);
            startActivity(intent);
        });

        accBtn.setOnClickListener(v -> {
            // Add intent to registration screen if needed
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 🧹 Clean up music when activity is destroyed
        if (bgMusic != null) {
            bgMusic.release();
            bgMusic = null;
        }
    }
}
