package com.example.medisync.patient;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditPatientProfileActivity extends AppCompatActivity {

    EditText nameEdit, emailEdit, ageEdit, genderEdit;
    Button saveBtn, backBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient_profile);

        // EditTexts
        nameEdit = findViewById(R.id.nameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        ageEdit = findViewById(R.id.ageEdit);
        genderEdit = findViewById(R.id.genderEdit);

        // Buttons
        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            loadProfile(user.getUid());
            saveBtn.setOnClickListener(v -> updateProfile(user.getUid()));
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Back button
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadProfile(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        nameEdit.setText(documentSnapshot.getString("fullName"));
                        emailEdit.setText(documentSnapshot.getString("email"));
                        
                        // Handle potential null to avoid "null" string appearing in EditText
                        Object ageObj = documentSnapshot.get("age");
                        ageEdit.setText(ageObj != null ? String.valueOf(ageObj) : "");
                        
                        genderEdit.setText(documentSnapshot.getString("gender"));
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

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double-tap
        saveBtn.setEnabled(false);
        saveBtn.setText("Updating...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        updates.put("email", email);
        updates.put("age", age);
        updates.put("gender", gender);

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to profile page
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    saveBtn.setEnabled(true);
                    saveBtn.setText("Save Changes");
                });
    }
}
