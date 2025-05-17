package com.example.madproject;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText, favoritePokemonEditText;
    private Button signupBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private MediaPlayer bgMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        favoritePokemonEditText = findViewById(R.id.favoritePokemon);
        signupBtn = findViewById(R.id.signupBtn);

        // Play background music
        bgMusic = MediaPlayer.create(this, R.raw.pokedexmusic);
        bgMusic.setLooping(true);
        bgMusic.start();

        signupBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String favoritePokemon = favoritePokemonEditText.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Valid email is required");
            emailEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(favoritePokemon)) {
            favoritePokemonEditText.setError("Favorite Pokémon is required");
            favoritePokemonEditText.requestFocus();
            return;
        }

        signupBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    signupBtn.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnSuccessListener(aVoid -> {
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("username", username);
                                        userData.put("email", email);
                                        userData.put("favoritePokemon", favoritePokemon);

                                        firestore.collection("users")
                                                .document(firebaseUser.getUid())
                                                .set(userData)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(Register.this, "Registered successfully! Please verify your email before logging in.", Toast.LENGTH_LONG).show();

                                                    stopMusic(); // stop music before going to login screen
                                                    Intent intent = new Intent(Register.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(Register.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Register.this, "Failed to send verification email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        if (firebaseUser != null) {
                                            firebaseUser.delete();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bgMusic != null) {
            bgMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.release();
            bgMusic = null;
        }
    }

    private void stopMusic() {
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.release();
            bgMusic = null;
        }
    }
}
