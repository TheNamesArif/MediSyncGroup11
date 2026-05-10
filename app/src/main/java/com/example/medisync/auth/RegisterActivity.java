package com.example.medisync.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.example.medisync.doctor.DoctorHomeActivity;
import com.example.medisync.patient.PatientHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText emailEdit, passwordEdit;
    Spinner roleSpinner;
    Button registerBtn;
    TextView loginLink;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        emailEdit = findViewById(R.id.email);
        passwordEdit = findViewById(R.id.password);
        roleSpinner = findViewById(R.id.roleSpinner);
        registerBtn = findViewById(R.id.registerBtn);
        loginLink = findViewById(R.id.loginLink);

        // Setup Spinner
        String[] roles = {"Patient", "Doctor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(adapter);

        // Handle Registration
        registerBtn.setOnClickListener(v -> registerUser());

        // Link back to Login
        loginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(email)) {
            emailEdit.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEdit.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEdit.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordEdit.setError("Password must be at least 6 characters");
            return;
        }

        registerBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    
                    // Save user info to Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("role", role);
                    user.put("uid", uid);

                    db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RegisterActivity.this, "Welcome to MediSync!", Toast.LENGTH_SHORT).show();
                                navigateToHome(role);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegisterActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                registerBtn.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    String errorMsg;
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        errorMsg = "Email already registered.";
                    } else if (e instanceof FirebaseAuthWeakPasswordException) {
                        errorMsg = "Password is too weak.";
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        errorMsg = "Invalid email format.";
                    } else {
                        errorMsg = "Error: " + e.getMessage();
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    registerBtn.setEnabled(true);
                });
    }

    private void navigateToHome(String role) {
        Intent intent;
        if ("Patient".equals(role)) {
            intent = new Intent(this, PatientHomeActivity.class);
        } else {
            intent = new Intent(this, DoctorHomeActivity.class);
        }
        // FLAG_ACTIVITY_CLEAR_TASK prevents the user from going back to registration via the back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
