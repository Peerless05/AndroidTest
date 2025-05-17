package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView signIntxt, forgotpassword;
    private Button loginBtn;
    private EditText emailInput, passwordInput, usernameInput;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    /*@Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                // User logged in & verified - skip login
                startActivity(new Intent(MainActivity.this, Intro.class));
                finish();
            } else {
                Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
            }
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        signIntxt = findViewById(R.id.signinTxt);
        forgotpassword = findViewById(R.id.forgotPassword);
        loginBtn = findViewById(R.id.loginBtn);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        usernameInput = findViewById(R.id.username);

        signIntxt.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Register clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Register.class));
        });

        forgotpassword.setOnClickListener(v -> showForgotPasswordDialog());

        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String enteredUsername = usernameInput.getText().toString().trim();

        // Validation
        if (enteredUsername.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (!user.isEmailVerified()) {
                                showEmailNotVerifiedDialog(user);
                                mAuth.signOut();
                                return;
                            }

                            firestore.collection("users")
                                    .document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String storedUsername = documentSnapshot.getString("username");
                                            if (storedUsername != null && storedUsername.equals(enteredUsername)) {
                                                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(MainActivity.this, Pokedex.class));
                                                finish();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Incorrect username.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this,
                                            "Failed to verify username: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                        }
                    } else {
                        String errorMessage = "Authentication failed";

                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            errorMessage = "No account found with this email.";
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Incorrect email or password.";
                        } else if (exception != null) {
                            errorMessage = "Error: " + exception.getMessage();
                        }

                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password?");
        builder.setMessage("Enter your email to receive reset link.");

        final EditText resetEmail = new EditText(this);
        resetEmail.setHint("Email");
        builder.setView(resetEmail);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = resetEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(MainActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this,
                            "Reset link sent to your email", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showEmailNotVerifiedDialog(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email not verified");
        builder.setMessage("Your email is not verified. Would you like us to resend the verification email?");

        builder.setPositiveButton("Resend Email", (dialog, which) -> {
            user.sendEmailVerification()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(MainActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this, "Failed to send verification email: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}
