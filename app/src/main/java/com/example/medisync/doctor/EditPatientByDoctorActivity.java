package com.example.medisync.doctor;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditPatientByDoctorActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etGender;
    private MaterialButton btnSave, btnBack;
    private FirebaseFirestore db;
    private String patientUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient_by_doctor);

        db = FirebaseFirestore.getInstance();
        patientUid = getIntent().getStringExtra("patientUid");

        if (patientUid == null) {
            Toast.makeText(this, "Error: Patient ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> updatePatientDetails());

        loadPatientDetails();
    }

    private void loadPatientDetails() {
        db.collection("users").document(patientUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etFullName.setText(documentSnapshot.getString("fullName"));
                        etAge.setText(documentSnapshot.getString("age"));
                        etGender.setText(documentSnapshot.getString("gender"));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePatientDetails() {
        String fullName = etFullName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("age", age);
        updates.put("gender", gender);

        db.collection("users").document(patientUid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Patient details updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
