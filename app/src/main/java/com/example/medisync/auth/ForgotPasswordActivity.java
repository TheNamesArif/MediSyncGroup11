package com.example.medisync.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailEdit;
    Button resetBtn, backBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEdit = findViewById(R.id.email);
        resetBtn = findViewById(R.id.resetBtn);
        backBtn = findViewById(R.id.backBtn);

        mAuth = FirebaseAuth.getInstance();

        resetBtn.setOnClickListener(v -> resetPassword());
        backBtn.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = emailEdit.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEdit.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEdit.setError("Enter valid email");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                    finish(); // Optionally close activity after success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
