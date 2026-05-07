package com.example.medisync.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.example.medisync.doctor.DoctorHomeActivity;
import com.example.medisync.patient.PatientHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText emailEdit, passwordEdit;
    Button loginBtn;
    TextView registerLink;
    TextView forgotPassword;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        forgotPassword = findViewById(R.id.forgotPassword);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        emailEdit = findViewById(R.id.email);
        passwordEdit = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerLink);

        // Handle Login button click
        loginBtn.setOnClickListener(v -> loginUser());

        // Handle "Register" link click
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        //Click listener
        forgotPassword.setOnClickListener(v -> resetPassword());

    }
    private void resetPassword() {
        String email = emailEdit.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loginUser() {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        loginBtn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Success! Now fetch the role.
                    checkUserRole(authResult.getUser().getUid());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loginBtn.setEnabled(true);
                });
    }

    private void checkUserRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        
                        if ("Patient".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, PatientHomeActivity.class));
                        } else if ("Doctor".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, DoctorHomeActivity.class));
                        } else {
                            Toast.makeText(this, "Unauthorized or unknown role", Toast.LENGTH_SHORT).show();
                            loginBtn.setEnabled(true);
                            return;
                        }
                        finish();
                    } else {
                        Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show();
                        loginBtn.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loginBtn.setEnabled(true);
                });
    }
}
