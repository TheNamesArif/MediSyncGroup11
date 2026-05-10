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

    EditText fullNameEdit, ageEdit, emailEdit, passwordEdit;
    Spinner roleSpinner, genderSpinner;
    Button registerBtn;
    TextView loginLink;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        fullNameEdit = findViewById(R.id.fullName);
        ageEdit = findViewById(R.id.age);
        emailEdit = findViewById(R.id.email);
        passwordEdit = findViewById(R.id.password);

        roleSpinner = findViewById(R.id.roleSpinner);
        genderSpinner = findViewById(R.id.genderSpinner);

        registerBtn = findViewById(R.id.registerBtn);
        loginLink = findViewById(R.id.loginLink);

        // Role spinner
        String[] roles = {"Patient", "Doctor"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(roleAdapter);

        // Gender spinner
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genders);
        genderSpinner.setAdapter(genderAdapter);

        registerBtn.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {

        String fullName = fullNameEdit.getText().toString().trim();
        String age = ageEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();
        String gender = genderSpinner.getSelectedItem().toString();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            fullNameEdit.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(age)) {
            ageEdit.setError("Age is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEdit.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEdit.setError("Enter valid email");
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

                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("email", email);
                    user.put("role", role);
                    user.put("fullName", fullName);
                    user.put("age", age);
                    user.put("gender", gender);

                    db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Welcome to MediSync!", Toast.LENGTH_SHORT).show();
                                navigateToHome(role);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                registerBtn.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    String errorMsg;

                    if (e instanceof FirebaseAuthUserCollisionException) {
                        errorMsg = "Email already registered.";
                    } else if (e instanceof FirebaseAuthWeakPasswordException) {
                        errorMsg = "Weak password.";
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        errorMsg = "Invalid email.";
                    } else {
                        errorMsg = e.getMessage();
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

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}