package com.example.medisync.doctor;

import android.content.Intent;
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
import com.example.medisync.adapter.MedicineHistoryAdapter;
import com.example.medisync.model.Medicine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ViewScheduleHistoryActivity extends AppCompatActivity implements MedicineHistoryAdapter.OnMedicineActionListener {

    private FirebaseFirestore db;
    private Spinner spinnerPatients;
    private RecyclerView recyclerViewSchedules;
    private MedicineHistoryAdapter adapter;
    private List<Medicine> medicineList = new ArrayList<>();
    private List<String> patientIds = new ArrayList<>();
    private List<String> patientNames = new ArrayList<>();
    private String selectedPatientId;

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

        db = FirebaseFirestore.getInstance();

        initializeViews();

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadPatients();

        spinnerPatients.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedPatientId = patientIds.get(position - 1);
                    loadHistory(selectedPatientId);
                } else {
                    medicineList.clear();
                    adapter.notifyDataSetChanged();
                    selectedPatientId = null;
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void initializeViews() {
        spinnerPatients = findViewById(R.id.spinnerPatients);
        recyclerViewSchedules = findViewById(R.id.recyclerViewSchedules);
        recyclerViewSchedules.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineHistoryAdapter(medicineList, this);
        recyclerViewSchedules.setAdapter(adapter);
    }

    private void loadPatients() {
        db.collection("users").whereEqualTo("role", "Patient").get().addOnSuccessListener(querySnapshot -> {
            patientIds.clear(); patientNames.clear();
            patientNames.add("Select A Patient");
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                patientIds.add(doc.getId());
                String name = doc.getString("fullName");
                patientNames.add(name != null ? name : doc.getString("email"));
            }
            spinnerPatients.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, patientNames));
        });
    }

    private void loadHistory(String patientId) {
        db.collection("users").document(patientId).collection("medicines")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    medicineList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // FIXED: Passing all 9 parameters to the Medicine constructor
                        medicineList.add(new Medicine(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("amount"),
                                doc.getString("unit"),
                                doc.getString("instruction"),
                                (List<String>) doc.get("intakeTimes"),
                                doc.getString("status"),
                                doc.getString("patientName"),
                                patientId // 9th argument: patientUid
                        ));
                    }
                    adapter.notifyDataSetChanged();
                    if (medicineList.isEmpty()) {
                        Toast.makeText(this, "No History Found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error Loading History", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onView(Medicine medicine) {
        new AlertDialog.Builder(this)
                .setTitle("Medicine Details")
                .setMessage("Name: " + medicine.getName() + "\n" +
                           "Amount: " + medicine.getAmount() + " " + medicine.getUnit() + "\n" +
                           "Instruction: " + medicine.getInstruction() + "\n" +
                           "Status: " + medicine.getStatus())
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onEdit(Medicine medicine) {
        if (medicine.getDocumentId() == null || medicine.getPatientUid() == null) {
            Toast.makeText(this, "Error: Invalid Medicine Data", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, UpdateMedicineActivity.class);
        intent.putExtra("documentId", medicine.getDocumentId());
        intent.putExtra("patientId", medicine.getPatientUid());
        startActivity(intent);
    }

    @Override
    public void onDelete(Medicine medicine) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Schedule")
                .setMessage("Are You Sure You Want To Delete This Medicine?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("users").document(medicine.getPatientUid())
                            .collection("medicines").document(medicine.getDocumentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                loadHistory(medicine.getPatientUid());
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
