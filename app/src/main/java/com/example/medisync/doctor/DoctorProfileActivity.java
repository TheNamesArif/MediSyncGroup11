package com.example.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medisync.R;
import com.example.medisync.auth.ChangePasswordActivity;
import com.example.medisync.auth.LoginActivity;
import com.example.medisync.patient.PatientProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DoctorProfileActivity extends AppCompatActivity {

    EditText nameEdit, emailEdit, ageEdit, genderEdit;
    TextView displayName, displayEmail;
    Button logoutBtn, updateBtn, backBtn;
    Button changePasswordBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_profile);

        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        nameEdit = findViewById(R.id.nameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        ageEdit = findViewById(R.id.ageEdit);
        genderEdit = findViewById(R.id.genderEdit);

        displayName = findViewById(R.id.displayName);
        displayEmail = findViewById(R.id.displayEmail);

        logoutBtn = findViewById(R.id.logoutBtn);
        updateBtn = findViewById(R.id.updateBtn);
        backBtn = findViewById(R.id.backBtn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        changePasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        if (user != null) {
            loadProfile(user.getUid());

            updateBtn.setOnClickListener(v -> updateProfile(user.getUid()));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(DoctorProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        backBtn.setOnClickListener(v -> finish());
    }

    private void loadProfile(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        nameEdit.setText(documentSnapshot.getString("fullName"));
                        emailEdit.setText(documentSnapshot.getString("email"));
                        ageEdit.setText(String.valueOf(documentSnapshot.get("age")));
                        genderEdit.setText(documentSnapshot.getString("gender"));

                        displayName.setText(documentSnapshot.getString("fullName"));
                        displayEmail.setText(documentSnapshot.getString("email"));

                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void updateProfile(String uid) {

        String name = nameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String age = ageEdit.getText().toString().trim();
        String gender = genderEdit.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        updates.put("email", email);
        updates.put("age", age);
        updates.put("gender", gender);

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
        // refresh page
        new Handler(Looper.getMainLooper()).postDelayed(() -> recreate(), 1000);
    }
}