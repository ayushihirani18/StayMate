package com.example.staymateapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private View loginCard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            openDashboard(user.getUid());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginProgress = findViewById(R.id.loginProgress);
        loginCard = findViewById(R.id.loginCard);

        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        SharedPreferences prefs = getSharedPreferences("StayMatePrefs", MODE_PRIVATE);
        etEmail.setText(prefs.getString("lastEmail", ""));

        btnLogin.setOnClickListener(v -> loginUser());
        tvForgotPassword.setOnClickListener(v -> resetPassword());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));


        // Login card animation
        loginCard.setTranslationY(300);
        loginCard.setAlpha(0f);

        loginCard.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(700)
                .start();
    }

    private void loginUser() {

        hideKeyboard();

        if (!isInternetAvailable()) {
            Toast.makeText(this,
                    "No internet connection",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String email = etEmail.getText().toString().trim().replace(" ", "");
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        loginProgress.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    loginProgress.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {

                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        if (currentUser != null) {

                            String userId = currentUser.getUid();

                            SharedPreferences prefs =
                                    getSharedPreferences("StayMatePrefs", MODE_PRIVATE);

                            prefs.edit()
                                    .putString("lastEmail", email)
                                    .apply();

                            Toast.makeText(this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT).show();

                            openDashboard(userId);

                        } else {

                            Toast.makeText(this,
                                    "Authentication error",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {

                        Exception exception = task.getException();

                        if (exception instanceof FirebaseAuthInvalidUserException) {

                            Toast.makeText(this,
                                    "User not registered",
                                    Toast.LENGTH_LONG).show();

                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {

                            Toast.makeText(this,
                                    "Incorrect email or password",
                                    Toast.LENGTH_LONG).show();

                        } else {

                            Toast.makeText(this,
                                    "Login failed",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void openDashboard(String userId) {

        loginProgress.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    loginProgress.setVisibility(View.GONE);

                    if (!documentSnapshot.exists()) {

                        Toast.makeText(this,
                                "User data not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String role = documentSnapshot.getString("role");

                    if (role == null) {

                        Toast.makeText(this,
                                "User role missing",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    role = role.trim().toLowerCase();

                    if (role.equals("tenant")) {

                        Intent intent = new Intent(this, TenantDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else if (role.equals("owner")) {

                        Intent intent = new Intent(this, OwnerDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {

                        Toast.makeText(this,
                                "Invalid role",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    finish();
                })
                .addOnFailureListener(e -> {

                    loginProgress.setVisibility(View.GONE);

                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void resetPassword() {

        String email = etEmail.getText().toString().trim().replace(" ", "");

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email first");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Reset email sent",
                                Toast.LENGTH_LONG).show();

                    } else {

                        Toast.makeText(this,
                                "Failed to send reset email",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void hideKeyboard() {

        View view = getCurrentFocus();

        if (view != null) {

            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            getSystemService(INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean isInternetAvailable() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (cm != null) {

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            return activeNetwork != null && activeNetwork.isConnected();
        }

        return false;
    }
}