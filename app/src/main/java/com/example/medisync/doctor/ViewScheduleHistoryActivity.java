package com.example.medisync.doctor;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;
import com.example.medisync.adapter.MedicineAdapter;
import com.example.medisync.model.Medicine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ViewScheduleHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner spinnerPatients;
    private RecyclerView recyclerViewSchedules;
    private MedicineAdapter adapter;
    private List<Medicine> medicineList = new ArrayList<>();
    private List<String> patientIds = new ArrayList<>();
    private List<String> patientNames = new ArrayList<>();

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
                    loadHistory(patientIds.get(position - 1));
                } else {
                    medicineList.clear();
                    adapter.notifyDataSetChanged();
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
        adapter = new MedicineAdapter(medicineList);
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
        // Fetch all medicines for this patient ordered by creation date
        db.collection("users").document(patientId).collection("medicines")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    medicineList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        medicineList.add(new Medicine(
                                doc.getString("name"),
                                doc.getString("amount"),
                                doc.getString("unit"),
                                doc.getString("instruction"),
                                (List<String>) doc.get("intakeTimes"),
                                doc.getString("status"),
                                doc.getString("patientName")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                    if (medicineList.isEmpty()) {
                        Toast.makeText(this, "No History Found For This Patient", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error Loading History", Toast.LENGTH_SHORT).show());
    }
}
