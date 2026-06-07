package com.example.medisync.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medisync.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class TakenStatusActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String medicineId, patientUid, intakeTime, currentStatus, patientName;
    private String medName, medAmount, medInstruction, medRemarks;

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

        // Receive data
        medicineId     = getIntent().getStringExtra("medicineId");
        patientUid     = getIntent().getStringExtra("patientUid");
        patientName    = getIntent().getStringExtra("patientName");
        intakeTime     = getIntent().getStringExtra("intakeTime");
        currentStatus  = getIntent().getStringExtra("currentStatus");
        medName        = getIntent().getStringExtra("medName");
        medAmount      = getIntent().getStringExtra("medAmount");
        medInstruction = getIntent().getStringExtra("medInstruction");
        medRemarks     = getIntent().getStringExtra("medRemarks");

        // --- Card views (from item_medicine_dashboard.xml via <include>) ---
        View medicineCard = findViewById(R.id.medicineCard);
        TextView tvPatientName       = findViewById(R.id.tvPatientName);
        TextView tvIntakeTimePrimary = findViewById(R.id.tvIntakeTimePrimary);
        TextView tvMedName           = findViewById(R.id.tvMedName);
        TextView tvMedAmount         = findViewById(R.id.tvMedAmount);
        TextView tvMedInstruction    = findViewById(R.id.tvMedInstruction);
        TextView tvStatus            = findViewById(R.id.tvStatus);
        TextView tvRemarks           = findViewById(R.id.tvRemarks);

        // Populate card fields
        if (tvPatientName != null && patientName != null) {
            tvPatientName.setText("Patient: " + patientName);
            tvPatientName.setVisibility(View.VISIBLE);
        }

        if (tvIntakeTimePrimary != null)
            tvIntakeTimePrimary.setText(intakeTime != null ? intakeTime : "--:--");

        if (tvMedName != null)
            tvMedName.setText(medName != null ? medName : "Unknown Medicine");

        if (tvMedAmount != null)
            tvMedAmount.setText(medAmount != null ? medAmount : "");

        if (tvMedInstruction != null)
            tvMedInstruction.setText(medInstruction != null ? medInstruction : "");

        if (tvStatus != null)
            setStatusBadge(tvStatus, currentStatus);

        if (tvRemarks != null && medRemarks != null && !medRemarks.isEmpty()) {
            tvRemarks.setText("Remarks: " + medRemarks);
            tvRemarks.setVisibility(View.VISIBLE);
        }

        // --- Action buttons ---
        Button btnMarkTaken   = findViewById(R.id.btnMarkTaken);
        Button btnMarkMissed  = findViewById(R.id.btnMarkMissed);
        Button btnMarkPending = findViewById(R.id.btnMarkPending);
        Button btnBack        = findViewById(R.id.btnBack);

        if (btnMarkTaken != null)
            btnMarkTaken.setOnClickListener(v -> updateStatus("taken", tvStatus));

        if (btnMarkMissed != null)
            btnMarkMissed.setOnClickListener(v -> updateStatus("missed", tvStatus));

        if (btnMarkPending != null)
            btnMarkPending.setOnClickListener(v -> updateStatus("pending", tvStatus));

        if (btnBack != null)
            btnBack.setOnClickListener(v -> finish());

        if (medicineCard != null) {
            medicineCard.setOnClickListener(v -> {
                // Handle card click if needed, or just Toast for now
                Toast.makeText(this, "Medicine Details: " + medName, Toast.LENGTH_SHORT).show();
            });
        }
    }

    /** Updates the status badge colour and label on the card immediately. */
    private void setStatusBadge(TextView tvStatus, String status) {
        if (status == null) status = "pending";
        tvStatus.setText(status.toUpperCase());
        switch (status.toLowerCase()) {
            case "taken":
                tvStatus.setBackgroundResource(R.drawable.bg_card_green); // use your green drawable
                break;
            case "missed":
                tvStatus.setBackgroundResource(R.drawable.bg_card_orange);   // use your red drawable
                break;
            default: // pending
                tvStatus.setBackgroundResource(R.drawable.bg_card_blue);
                break;
        }
    }

    private void updateStatus(String newStatus, TextView tvStatus) {
        if (medicineId == null || patientUid == null || intakeTime == null) {
            Toast.makeText(this, "Error: Missing medicine info", Toast.LENGTH_SHORT).show();
            return;
        }

        int intakeIndex = getIntent().getIntExtra("intakeIndex", -1);
        String fieldPath;
        if (intakeIndex != -1) {
            fieldPath = "intakeTimes." + intakeIndex + ".status";
        } else {
            fieldPath = "intakeTimes." + intakeTime;
        }

        db.collection("users").document(patientUid)
                .collection("medicines").document(medicineId)
                .update(fieldPath, newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Update the card badge immediately before finishing
                    if (tvStatus != null) setStatusBadge(tvStatus, newStatus);
                    Toast.makeText(this, "Marked as " + newStatus, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}