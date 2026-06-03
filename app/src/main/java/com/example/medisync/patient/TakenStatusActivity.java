package com.example.medisync.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medisync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TakenStatusActivity extends AppCompatActivity {

    private String medicineId, intakeTime;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvMedName, tvIntakeTime, tvDosage, tvInstruction, tvStatus;
    private Button btnMarkTaken;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_taken_status);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get data from Intent
        medicineId = getIntent().getStringExtra("medicineId");
        intakeTime = getIntent().getStringExtra("intakeTime");

        initializeViews();
        loadMedicineDetails();

        btnBack.setOnClickListener(v -> finish());
        btnMarkTaken.setOnClickListener(v -> markAsTaken());
    }

    private void initializeViews() {
        tvMedName = findViewById(R.id.tvMedName);
        tvIntakeTime = findViewById(R.id.tvIntakeTime);
        tvDosage = findViewById(R.id.tvDosage);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvStatus = findViewById(R.id.tvStatus);
        btnMarkTaken = findViewById(R.id.btnMarkTaken);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadMedicineDetails() {
        if (medicineId == null || mAuth.getUid() == null) {
            Toast.makeText(this, "Invalid Data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users").document(mAuth.getUid())
                .collection("medicines").document(medicineId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String amount = documentSnapshot.getString("amount");
                        String unit = documentSnapshot.getString("unit");
                        String instruction = documentSnapshot.getString("instruction");
                        String status = documentSnapshot.getString("status");

                        tvMedName.setText(name);
                        tvIntakeTime.setText(intakeTime);
                        tvDosage.setText(amount + " " + (unit != null ? unit : ""));
                        tvInstruction.setText(instruction != null ? instruction : "No instruction");
                        
                        String displayStatus = status != null ? status.toUpperCase() : "PENDING";
                        tvStatus.setText(displayStatus);

                        if (!"TAKEN".equalsIgnoreCase(status)) {
                            btnMarkTaken.setVisibility(View.VISIBLE);
                        } else {
                            btnMarkTaken.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Medicine not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void markAsTaken() {
        if (medicineId == null || mAuth.getUid() == null) return;

        db.collection("users").document(mAuth.getUid())
                .collection("medicines").document(medicineId)
                .update("status", "TAKEN")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Marked as Taken", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("TAKEN");
                    btnMarkTaken.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
