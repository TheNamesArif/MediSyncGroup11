package com.example.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medisync.R;
import com.example.medisync.auth.ChangePasswordActivity;
import com.example.medisync.auth.LoginActivity;
import com.example.medisync.doctor.DoctorHomeActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PatientHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Logout button calls logoutUser function
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logoutUser());

        // Button opens change password page
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(PatientHomeActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void logoutUser() {
        // Sign out from Firebase Auth (clears session)
        FirebaseAuth.getInstance().signOut();

        // Give sign out message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login and clear back stack
        Intent intent = new Intent(PatientHomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}