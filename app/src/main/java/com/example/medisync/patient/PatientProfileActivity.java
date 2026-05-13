package com.example.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.example.medisync.auth.ChangePasswordActivity;
import com.example.medisync.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PatientProfileActivity extends AppCompatActivity {

    TextView nameText, emailText, ageText, genderText;
    TextView displayName, displayEmail;

    Button logoutBtn, updateBtn, backBtn, changePasswordBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        // TextViews
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        ageText = findViewById(R.id.ageText);
        genderText = findViewById(R.id.genderText);

        displayName = findViewById(R.id.displayName);
        displayEmail = findViewById(R.id.displayEmail);

        // Buttons
        logoutBtn = findViewById(R.id.logoutBtn);
        updateBtn = findViewById(R.id.updateBtn);
        backBtn = findViewById(R.id.backBtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            loadProfile(user.getUid());
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Open Edit Profile Page
        updateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(
                    PatientProfileActivity.this,
                    EditPatientProfileActivity.class
            );
            startActivity(intent);
        });

        // Open Change Password Page
        changePasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(
                    PatientProfileActivity.this,
                    ChangePasswordActivity.class
            );
            startActivity(intent);
        });

        // Logout
        logoutBtn.setOnClickListener(v -> {

            mAuth.signOut();

            Intent intent = new Intent(
                    PatientProfileActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });

        // Back Button
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

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String age = String.valueOf(documentSnapshot.get("age"));
                        String gender = documentSnapshot.getString("gender");

                        // Set info card
                        nameText.setText(name);
                        emailText.setText(email);
                        ageText.setText(age);
                        genderText.setText(gender);

                        // Set top profile card
                        displayName.setText(name);
                        displayEmail.setText(email);

                    } else {

                        Toast.makeText(
                                this,
                                "Profile not found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }
}