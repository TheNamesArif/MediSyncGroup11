package com.example.medisync.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.example.medisync.doctor.DoctorHomeActivity;
import com.example.medisync.patient.PatientHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is logged in, check role
                checkUserRoleAndNavigate(currentUser.getUid());
            } else {
                // No user logged in, go to Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 2000);
    }

    private void checkUserRoleAndNavigate(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("Patient".equals(role)) {
                            startActivity(new Intent(SplashActivity.this, PatientHomeActivity.class));
                        } else if ("Doctor".equals(role)) {
                            startActivity(new Intent(SplashActivity.this, DoctorHomeActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        }
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Session error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                });
    }
}
