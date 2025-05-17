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

        // Initialize views and click listeners immediately
        initialize();

        // Start background music and loop it
        bgMusic = MediaPlayer.create(this, R.raw.pokedexmusic);
        bgMusic.setLooping(true);
        bgMusic.start();
    }

    private void initialize() {
        loginText = findViewById(R.id.loginText);
        accBtn = findViewById(R.id.accBtn);

        loginText.setOnClickListener(v -> {
            Toast.makeText(Intro.this, "Login clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intro.this, MainActivity.class);
            startActivity(intent);
        });

        accBtn.setOnClickListener(v -> {
            Toast.makeText(Intro.this, "Register clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intro.this, Register.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources properly
        if (bgMusic != null) {
            if (bgMusic.isPlaying()) {
                bgMusic.stop();
            }
            bgMusic.release();
            bgMusic = null;
        }
    }
}
