package com.example.staymateapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Spinner spinnerRole;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        Button btnRegister = findViewById(R.id.btnRegister);

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.role_options,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setEnabled(false);

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim().replace(" ", "");
        String password = etPassword.getText().toString().trim();
        String selectedRole = spinnerRole.getSelectedItem().toString();

        // ===== VALIDATION =====

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters required");
            return;
        }

        if (selectedRole.equals("Select Role")) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Normalize role (VERY IMPORTANT)
        String role = selectedRole.trim().toLowerCase();

        // ===== FIREBASE AUTH =====

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    btnRegister.setEnabled(true);
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {
                            Toast.makeText(this,
                                    "User creation failed",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        String userId = user.getUid();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("role", role);
                        userMap.put("createdAt", System.currentTimeMillis());

                        db.collection("users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(this,
                                            "Registration Successful",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(RegisterActivity.this,
                                            LoginActivity.class);

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Firestore Error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show());

                    } else {

                        Exception exception = task.getException();

                        if (exception != null) {

                            String message = exception.getMessage();

                            if (message != null && message.contains("email address is already")) {
                                etEmail.setError("Email already registered");
                            } else {
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(this,
                                    "Registration failed",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}