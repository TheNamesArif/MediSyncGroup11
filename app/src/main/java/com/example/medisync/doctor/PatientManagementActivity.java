package com.example.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;
import com.example.medisync.adapter.PatientAdapter;
import com.example.medisync.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PatientManagementActivity extends AppCompatActivity implements PatientAdapter.OnPatientClickListener {

    private RecyclerView rvPatients;
    private PatientAdapter adapter;
    private List<User> patientList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_management);

        db = FirebaseFirestore.getInstance();

        rvPatients = findViewById(R.id.rvPatients);
        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientAdapter(patientList, this);
        rvPatients.setAdapter(adapter);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        fetchPatients();
    }

    private void fetchPatients() {
        db.collection("users")
                .whereEqualTo("role", "Patient")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    patientList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        // Ensure UID is set even if the 'uid' field is missing in the document
                        if (user.getUid() == null) {
                            user.setUid(doc.getId());
                        }
                        patientList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditClick(User user) {
        Intent intent = new Intent(this, EditPatientByDoctorActivity.class);
        intent.putExtra("patientUid", user.getUid());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete this patient account? This will also delete their medicine records.")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient(User user) {
        // First delete medicines then the user document
        db.collection("users").document(user.getUid()).collection("medicines")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    // After deleting subcollection, delete the user doc
                    db.collection("users").document(user.getUid())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                                fetchPatients(); // Refresh list
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error deleting patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPatients(); // Refresh list when returning from edit
    }
}
