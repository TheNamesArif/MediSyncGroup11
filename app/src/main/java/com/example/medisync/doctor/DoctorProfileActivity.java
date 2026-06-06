package com.example.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.example.medisync.auth.ChangePasswordActivity;
import com.example.medisync.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DoctorProfileActivity extends AppCompatActivity {

    TextView nameText, emailText, ageText, genderText;
    TextView displayName, displayEmail;
    Button logoutBtn, backBtn, updateBtn, changePasswordBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        ageText = findViewById(R.id.ageText);
        genderText = findViewById(R.id.genderText);
        displayName = findViewById(R.id.displayName);
        displayEmail = findViewById(R.id.displayEmail);

        logoutBtn = findViewById(R.id.logoutBtn);
        backBtn = findViewById(R.id.backBtn);
        updateBtn = findViewById(R.id.updateBtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadProfile(user.getUid());

        // Navigation Listeners
        updateBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, EditDoctorProfileActivity.class));
        });

        changePasswordBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        backBtn.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadProfile(user.getUid());
        }
    }

    private void loadProfile(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("fullName");
                        String email = doc.getString("email");
                        Object ageObj = doc.get("age");
                        String gender = doc.getString("gender");

                        // Apply values with Title Case fallbacks
                        nameText.setText(name != null ? name : "Name Not Set");
                        emailText.setText(email != null ? email : "Email Not Set");
                        ageText.setText(ageObj != null ? String.valueOf(ageObj) : "Age Not Set");
                        genderText.setText(gender != null ? gender : "Gender Not Set");

                        displayName.setText(name != null ? name : "Doctor Profile");
                        displayEmail.setText(email != null ? email : "");
                    } else {
                        Toast.makeText(this, "Profile Not Found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
