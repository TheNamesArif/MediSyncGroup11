package com.example.medisync.doctor;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.LinearLayout;
import android.app.DatePickerDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medisync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageScheduleActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    private Spinner spinnerPatients;
    private LinearLayout patientInfoSection;
    private TextView tvPatientName, tvPatientEmail, tvPatientAge;
    private EditText etMedicineName, etIntakeAmount;
    private Spinner spinnerInstruction;
    private Button btnAddMedicine, btnSaveSchedule, btnSelectDate;
    private TextView tvSelectedDate;
    
    private List<String> patientIds = new ArrayList<>();
    private List<String> patientNames = new ArrayList<>();
    private String selectedPatientId;
    private List<Map<String, String>> medicineList = new ArrayList<>();
    private Calendar selectedCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_schedule);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews();

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        loadPatients();

        spinnerPatients.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { 
                    selectedPatientId = patientIds.get(position - 1);
                    loadPatientInfo(selectedPatientId);
                    patientInfoSection.setVisibility(View.VISIBLE);
                    medicineList.clear();
                } else {
                    patientInfoSection.setVisibility(View.GONE);
                    selectedPatientId = null;
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnAddMedicine.setOnClickListener(v -> addMedicineToList());
        btnSaveSchedule.setOnClickListener(v -> saveScheduleToFirebase());
    }

    private void initializeViews() {
        spinnerPatients = findViewById(R.id.spinnerPatients);
        patientInfoSection = findViewById(R.id.patientInfoSection);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        tvPatientAge = findViewById(R.id.tvPatientAge);
        etMedicineName = findViewById(R.id.etMedicineName);
        etIntakeAmount = findViewById(R.id.etIntakeAmount);
        spinnerInstruction = findViewById(R.id.spinnerInstruction);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.instruction_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInstruction.setAdapter(adapter);

        patientInfoSection.setVisibility(View.GONE);
        updateDateDisplay();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedCalendar.set(year, month, day);
                    updateDateDisplay();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedCalendar.getTime()));
    }

    private void loadPatients() {
        db.collection("users")
                .whereEqualTo("role", "Patient")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    patientIds.clear();
                    patientNames.clear();
                    patientNames.add("Select A Patient"); 

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String id = doc.getId();
                        String name = doc.getString("fullName");
                        String email = doc.getString("email");
                        
                        patientIds.add(id);
                        // FIX: Ensure no null values are added to the spinner list
                        String displayName = (name != null && !name.isEmpty()) ? name : (email != null ? email : "Unknown Patient");
                        patientNames.add(displayName);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, patientNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPatients.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error Loading Patients", Toast.LENGTH_SHORT).show());
    }

    private void loadPatientInfo(String patientId) {
        db.collection("users").document(patientId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("fullName");
                        String email = doc.getString("email");
                        
                        // FIX: Safely handle age as Object to support both Number and String formats
                        Object ageObj = doc.get("age");
                        String ageStr = (ageObj != null) ? String.valueOf(ageObj) : "Not Set";

                        tvPatientName.setText(name != null ? name : "Not Set");
                        tvPatientEmail.setText(email != null ? email : "Not Set");
                        tvPatientAge.setText(ageStr.equals("Not Set") ? ageStr : ageStr + " Years");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error Loading Patient Info", Toast.LENGTH_SHORT).show());
    }

    private void addMedicineToList() {
        String name = etMedicineName.getText().toString().trim();
        String amount = etIntakeAmount.getText().toString().trim();
        String instr = spinnerInstruction.getSelectedItem().toString();

        if (name.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Please Fill In All Fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> medicine = new HashMap<>();
        medicine.put("name", name);
        medicine.put("amount", amount);
        medicine.put("instruction", instr);
        medicineList.add(medicine);

        etMedicineName.setText("");
        etIntakeAmount.setText("");
        Toast.makeText(this, "Medicine Added To List", Toast.LENGTH_SHORT).show();
    }

    private void saveScheduleToFirebase() {
        if (selectedPatientId == null || medicineList.isEmpty()) {
            Toast.makeText(this, "Please Select A Patient And Add Medicine", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> schedule = new HashMap<>();
        schedule.put("medicines", medicineList);
        schedule.put("createdAt", new Date(selectedCalendar.getTimeInMillis()));
        schedule.put("doctorId", auth.getCurrentUser().getUid());

        db.collection("users").document(selectedPatientId).collection("medicines").add(schedule)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Schedule Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error Saving Schedule", Toast.LENGTH_SHORT).show());
    }

    private void clearForm() {
        etMedicineName.setText("");
        etIntakeAmount.setText("");
        spinnerInstruction.setSelection(0);
        spinnerPatients.setSelection(0);
        patientInfoSection.setVisibility(View.GONE);
        selectedCalendar = Calendar.getInstance();
        updateDateDisplay();
    }
}
