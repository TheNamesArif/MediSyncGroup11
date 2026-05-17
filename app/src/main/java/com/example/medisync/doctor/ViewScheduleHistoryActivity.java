package com.example.medisync.doctor;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewScheduleHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    private Spinner spinnerPatients;
    private RecyclerView recyclerViewSchedules;
    
    private List<String> patientIds = new ArrayList<>();
    private List<String> patientNames = new ArrayList<>();
    private String selectedPatientId;
    private List<ScheduleItem> scheduleList = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_schedule_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        initializeViews();

        // Back button functionality
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Load patients from Firestore
        loadPatients();

        // Patient spinner listener
        spinnerPatients.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip default selection
                    selectedPatientId = patientIds.get(position - 1);
                    loadSchedules(selectedPatientId);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void initializeViews() {
        spinnerPatients = findViewById(R.id.spinnerPatients);
        recyclerViewSchedules = findViewById(R.id.recyclerViewSchedules);

        // Setup RecyclerView for schedules
        recyclerViewSchedules.setLayoutManager(new LinearLayoutManager(this));
        scheduleAdapter = new ScheduleAdapter(scheduleList, this::onViewSchedule, this::onEditSchedule, this::onDeleteSchedule);
        recyclerViewSchedules.setAdapter(scheduleAdapter);
    }

    private void loadPatients() {
        db.collection("users")
                .whereEqualTo("role", "Patient")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    patientIds.clear();
                    patientNames.clear();
                    patientNames.add("Select a patient"); // Default option

                    for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                        String patientId = querySnapshot.getDocuments().get(i).getId();
                        String patientName = querySnapshot.getDocuments().get(i).getString("fullName");
                        patientIds.add(patientId);
                        patientNames.add(patientName);
                    }

                    // Setup spinner adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewScheduleHistoryActivity.this,
                            android.R.layout.simple_spinner_item, patientNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPatients.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewScheduleHistoryActivity.this, "Error loading patients", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSchedules(String patientId) {
        db.collection("users").document(patientId)
                .collection("medicines")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    scheduleList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        ScheduleItem scheduleItem = new ScheduleItem(
                                document.getId(),
                                (List<Map<String, Object>>) document.get("medicines"),
                                document.getDate("createdAt")
                        );
                        scheduleList.add(scheduleItem);
                    }
                    scheduleAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewScheduleHistoryActivity.this, "Error loading schedules", Toast.LENGTH_SHORT).show();
                });
    }

    private void onViewSchedule(ScheduleItem scheduleItem) {
        // Show view dialog with all medicine details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("View Schedule");
        
        StringBuilder medicineText = new StringBuilder();
        if (scheduleItem.getMedicines() != null) {
            for (int i = 0; i < scheduleItem.getMedicines().size(); i++) {
                Map<String, Object> med = scheduleItem.getMedicines().get(i);
                medicineText.append((i + 1)).append(". ").append(med.get("name"))
                        .append(" - ").append(med.get("amount"))
                        .append(" - ").append(med.get("instruction")).append("\n");
            }
        }
        
        builder.setMessage("Date: " + scheduleItem.getFormattedDate() + "\n\nMedicines:\n\n" + medicineText.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void onEditSchedule(ScheduleItem scheduleItem) {
        // Show edit dialog with medicine list
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Schedule - " + scheduleItem.getId());
        
        StringBuilder medicineText = new StringBuilder();
        if (scheduleItem.getMedicines() != null) {
            for (int i = 0; i < scheduleItem.getMedicines().size(); i++) {
                Map<String, Object> med = scheduleItem.getMedicines().get(i);
                medicineText.append((i + 1)).append(". ").append(med.get("name"))
                        .append(" - ").append(med.get("amount"))
                        .append(" - ").append(med.get("instruction")).append("\n");
            }
        }
        
        builder.setMessage("Date: " + scheduleItem.getFormattedDate() + "\n\nMedicines:\n\n" + medicineText.toString());
        builder.setPositiveButton("Update Details", (dialog, which) -> {
            // You can add detailed edit form here
            Toast.makeText(this, "Edit form - Coming soon!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void onDeleteSchedule(String scheduleId) {
        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Schedule");
        builder.setMessage("Are you sure you want to delete this schedule? This action cannot be undone.");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteScheduleFromFirebase(scheduleId);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteScheduleFromFirebase(String scheduleId) {
        if (selectedPatientId == null) return;

        db.collection("users").document(selectedPatientId)
                .collection("medicines")
                .document(scheduleId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ViewScheduleHistoryActivity.this, "Schedule deleted successfully", Toast.LENGTH_SHORT).show();
                    loadSchedules(selectedPatientId); // Reload schedules
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewScheduleHistoryActivity.this, "Error deleting schedule", Toast.LENGTH_SHORT).show();
                });
    }
}
