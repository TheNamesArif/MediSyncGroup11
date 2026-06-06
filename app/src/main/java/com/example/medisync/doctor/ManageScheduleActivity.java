package com.example.medisync.doctor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medisync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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

    private Spinner spinnerPatients, spinnerUnit, spinnerInstruction;
    private LinearLayout patientInfoSection;
    private TextView tvPatientName, tvPatientEmail, tvSelectedTimes;
    private EditText etMedicineName, etIntakeAmount, etRemarks;
    private Button btnStartDate, btnEndDate, btnAddTime, btnAddMedicine, btnSaveSchedule;

    private List<String> patientIds = new ArrayList<>();
    private List<String> patientNames = new ArrayList<>();
    private List<String> currentIntakeTimes = new ArrayList<>();
    private List<Map<String, Object>> medicineList = new ArrayList<>();

    private Date startDate, endDate;
    private String selectedPatientId, selectedPatientName;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

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

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadPatients();

        spinnerPatients.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedPatientId = patientIds.get(position - 1);
                    selectedPatientName = patientNames.get(position);
                    loadPatientInfo(selectedPatientId);
                    patientInfoSection.setVisibility(View.VISIBLE);
                } else {
                    patientInfoSection.setVisibility(View.GONE);
                    selectedPatientId = null;
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
        btnAddTime.setOnClickListener(v -> showTimePicker());
        btnAddMedicine.setOnClickListener(v -> addMedicineToList());
        btnSaveSchedule.setOnClickListener(v -> saveScheduleToFirebase());
    }

    private void initializeViews() {
        spinnerPatients = findViewById(R.id.spinnerPatients);
        patientInfoSection = findViewById(R.id.patientInfoSection);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        etRemarks = findViewById(R.id.etRemarks);
        etMedicineName = findViewById(R.id.etMedicineName);
        etIntakeAmount = findViewById(R.id.etIntakeAmount);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        spinnerInstruction = findViewById(R.id.spinnerInstruction);
        tvSelectedTimes = findViewById(R.id.tvSelectedTimes);
        btnAddTime = findViewById(R.id.btnAddTime);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);

        String[] units = {"Pills", "ML"};
        spinnerUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));

        ArrayAdapter<CharSequence> insAdapter = ArrayAdapter.createFromResource(this,
                R.array.instruction_array, android.R.layout.simple_spinner_item);
        insAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInstruction.setAdapter(insAdapter);
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            if (isStartDate) {
                startDate = cal.getTime();
                btnStartDate.setText(dateFormat.format(startDate));
            } else {
                endDate = cal.getTime();
                btnEndDate.setText(dateFormat.format(endDate));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            String time = timeFormat.format(cal.getTime());
            currentIntakeTimes.add(time);
            updateSelectedTimesUI();
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
    }

    private void updateSelectedTimesUI() {
        if (currentIntakeTimes.isEmpty()) {
            tvSelectedTimes.setText("No Times Added");
        } else {
            tvSelectedTimes.setText(TextUtils.join(", ", currentIntakeTimes));
        }
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

    private void loadPatientInfo(String id) {
        db.collection("users").document(id).get().addOnSuccessListener(doc -> {
            tvPatientName.setText(doc.getString("fullName"));
            tvPatientEmail.setText(doc.getString("email"));
        });
    }

    private void addMedicineToList() {
        String name = etMedicineName.getText().toString().trim();
        String amount = etIntakeAmount.getText().toString().trim();
        if (name.isEmpty() || amount.isEmpty() || currentIntakeTimes.isEmpty()) {
            Toast.makeText(this, "Please Fill All Medicine Details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build intakeTimes as a Map where key is time and value is "pending"
        Map<String, String> intakeMap = new HashMap<>();
        for (String time : currentIntakeTimes) {
            intakeMap.put(time, "pending");
        }

        Map<String, Object> medicine = new HashMap<>();
        medicine.put("name", name);
        medicine.put("amount", amount);
        medicine.put("unit", spinnerUnit.getSelectedItem().toString());
        medicine.put("instruction", spinnerInstruction.getSelectedItem().toString());
        medicine.put("intakeTimes", intakeMap);
        medicine.put("patientName", selectedPatientName);
        medicine.put("patientUid", selectedPatientId);
        // no top-level "status" anymore

        medicineList.add(medicine);
        etMedicineName.setText(""); etIntakeAmount.setText("");
        currentIntakeTimes.clear(); updateSelectedTimesUI();
        Toast.makeText(this, "Added: " + name, Toast.LENGTH_SHORT).show();
    }

    private void saveScheduleToFirebase() {
        if (selectedPatientId == null || medicineList.isEmpty() || startDate == null || endDate == null) {
            Toast.makeText(this, "Please Complete All Steps", Toast.LENGTH_SHORT).show();
            return;
        }

        WriteBatch batch = db.batch();
        String remarks = etRemarks.getText().toString().trim();
        String doctorId = auth.getCurrentUser().getUid();

        for (Map<String, Object> med : medicineList) {
            Map<String, Object> finalDoc = new HashMap<>(med);
            finalDoc.put("startDate", startDate);
            finalDoc.put("endDate", endDate);
            finalDoc.put("remarks", remarks);
            finalDoc.put("doctorId", doctorId);
            finalDoc.put("createdAt", new Date());

            batch.set(db.collection("users").document(selectedPatientId)
                    .collection("medicines").document(), finalDoc);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Schedule Saved Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}