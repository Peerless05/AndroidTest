package com.example.madproject;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Intro extends AppCompatActivity {

    private TextView loginText;
    private Button accBtn;
    private MediaPlayer bgMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);

        // Apply system bars padding (edge-to-edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views and click listeners
        initialize();

        // Start and loop background music
        bgMusic = MediaPlayer.create(this, R.raw.pokedexmusic);
        bgMusic.setLooping(true);
        bgMusic.start();
    }

    private void initialize() {
        loginText = findViewById(R.id.loginText);
        accBtn = findViewById(R.id.accBtn);

        loginText.setOnClickListener(v -> {
            stopMusic();
            //Toast.makeText(Intro.this, "Login clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Intro.this, MainActivity.class));
            finish(); // Optional: closes Intro to prevent returning to it
        });

        accBtn.setOnClickListener(v -> {
            stopMusic();
            //Toast.makeText(Intro.this, "Register clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Intro.this, Register.class));
            finish(); // Optional
        });
    }

    private void stopMusic() {
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.stop();
            bgMusic.release();
            bgMusic = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic(); // Ensures cleanup in case activity is killed unexpectedly
    }
}
