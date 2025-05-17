package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private TextView signIntxt, forgotpassword;
    private Button loginBtn;
    private EditText emailInput, passwordInput, usernameInput;
    private ImageView googleLogin, facebookLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        callbackManager = CallbackManager.Factory.create();

        signIntxt = findViewById(R.id.signinTxt);
        forgotpassword = findViewById(R.id.forgotPassword);
        loginBtn = findViewById(R.id.loginBtn);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        usernameInput = findViewById(R.id.username);
        googleLogin = findViewById(R.id.google);
        facebookLogin = findViewById(R.id.facebook); // Add this in your XML layout

        signIntxt.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Register clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Register.class));
        });

        forgotpassword.setOnClickListener(v -> showForgotPasswordDialog());

        loginBtn.setOnClickListener(v -> loginUser());

        // Google Sign-In configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // from strings.xml
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleLogin.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Google login clicked", Toast.LENGTH_SHORT).show();
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        facebookLogin.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(MainActivity.this, "Facebook login cancelled.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(MainActivity.this, "Facebook login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String enteredUsername = usernameInput.getText().toString().trim();

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

                            firestore.collection("users").document(user.getUid())
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
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to send verification email: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Signed in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, Pokedex.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Signed in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, Pokedex.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Facebook sign-in failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data); // Facebook
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
