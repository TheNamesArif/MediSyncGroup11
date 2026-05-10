package com.example.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.example.medisync.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PatientProfileActivity extends AppCompatActivity {

    TextView nameText, emailText, ageText, genderText;
    Button logoutBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        ageText = findViewById(R.id.ageText);
        genderText = findViewById(R.id.genderText);
        logoutBtn = findViewById(R.id.logoutBtn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadProfile(user.getUid());
        } else {
            // If no user, send back to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(PatientProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfile(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Use getString with a fallback to "Not set" if null
                        String name = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        Object age = documentSnapshot.get("age");
                        String gender = documentSnapshot.getString("gender");

                        nameText.setText(name != null ? name : "Name not set");
                        emailText.setText(email != null ? email : "Email not set");
                        ageText.setText(age != null ? String.valueOf(age) : "Age not set");
                        genderText.setText(gender != null ? gender : "Gender not set");
                    } else {
                        Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
