package com.example.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.example.medisync.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditDoctorProfileActivity extends AppCompatActivity {

    EditText nameEdit, emailEdit, ageEdit, genderEdit;
    Button saveBtn, backBtn, cancelBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_doctor_profile);

        // Views
        nameEdit = findViewById(R.id.nameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        ageEdit = findViewById(R.id.ageEdit);
        genderEdit = findViewById(R.id.genderEdit);

        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Load existing data
        loadProfile(user.getUid());

        // Save button
        saveBtn.setOnClickListener(v -> updateProfile(user.getUid()));

        // Back buttons
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());
        if (cancelBtn != null) cancelBtn.setOnClickListener(v -> finish());
    }

    private void loadProfile(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        nameEdit.setText(doc.getString("fullName"));
                        emailEdit.setText(doc.getString("email"));
                        
                        Object ageObj = doc.get("age");
                        ageEdit.setText(ageObj != null ? String.valueOf(ageObj) : "");
                        
                        genderEdit.setText(doc.getString("gender"));
                    } else {
                        Toast.makeText(this, "Profile Not Found", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Name And Email Cannot Be Empty", Toast.LENGTH_SHORT).show();
            return;
        }

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
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        finish();
                    }, 800);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    saveBtn.setEnabled(true);
                    saveBtn.setText("Save Changes");
                });
    }
}
