package com.example.medisync.doctor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medisync.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpdateMedicineActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText etName, etAmount, etRemarks;
    private Spinner spinnerUnit, spinnerInstruction;
    private Button btnStart, btnEnd, btnAddTime, btnUpdate;
    private TextView tvTimes;

    private String docId, patientId;
    private List<String> intakeTimes = new ArrayList<>();
    private Date startDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_medicine);

        db = FirebaseFirestore.getInstance();
        docId = getIntent().getStringExtra("documentId");
        patientId = getIntent().getStringExtra("patientId");

        if (docId == null || patientId == null) {
            Toast.makeText(this, "Error: Missing Medicine Information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadMedicineData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnStart.setOnClickListener(v -> showDatePicker(true));
        btnEnd.setOnClickListener(v -> showDatePicker(false));
        btnAddTime.setOnClickListener(v -> showTimePicker());
        btnUpdate.setOnClickListener(v -> updateInFirebase());
    }

    private void initializeViews() {
        etName = findViewById(R.id.etMedicineName);
        etAmount = findViewById(R.id.etIntakeAmount);
        etRemarks = findViewById(R.id.etRemarks);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        spinnerInstruction = findViewById(R.id.spinnerInstruction);
        btnStart = findViewById(R.id.btnStartDate);
        btnEnd = findViewById(R.id.btnEndDate);
        btnAddTime = findViewById(R.id.btnAddTime);
        btnUpdate = findViewById(R.id.btnUpdate);
        tvTimes = findViewById(R.id.tvSelectedTimes);

        // Setup Spinners
        String[] units = {"Pills", "ML"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units);
        spinnerUnit.setAdapter(unitAdapter);

        ArrayAdapter<CharSequence> insAdapter = ArrayAdapter.createFromResource(this, R.array.instruction_array, android.R.layout.simple_spinner_item);
        insAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInstruction.setAdapter(insAdapter);
    }

    private void loadMedicineData() {
        db.collection("users").document(patientId).collection("medicines").document(docId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etName.setText(doc.getString("name"));
                        etAmount.setText(doc.getString("amount"));
                        etRemarks.setText(doc.getString("remarks"));
                        startDate = doc.getDate("startDate");
                        endDate = doc.getDate("endDate");
                        
                        List<String> times = (List<String>) doc.get("intakeTimes");
                        if (times != null) intakeTimes.addAll(times);

                        // Set Spinner Selections
                        setSpinnerSelection(spinnerUnit, doc.getString("unit"));
                        setSpinnerSelection(spinnerInstruction, doc.getString("instruction"));

                        if (startDate != null) btnStart.setText(dateFormat.format(startDate));
                        if (endDate != null) btnEnd.setText(dateFormat.format(endDate));
                        updateTimesUI();
                    } else {
                        Toast.makeText(this, "Medicine Record Not Found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error Loading Data", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        if (!isStart && startDate != null) cal.setTime(startDate);
        
        new DatePickerDialog(this, (view, y, m, d) -> {
            cal.set(y, m, d);
            if (isStart) { 
                startDate = cal.getTime(); 
                btnStart.setText(dateFormat.format(startDate)); 
            } else { 
                endDate = cal.getTime(); 
                btnEnd.setText(dateFormat.format(endDate)); 
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, h, m) -> {
            cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, m);
            intakeTimes.add(timeFormat.format(cal.getTime()));
            updateTimesUI();
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
    }

    private void updateTimesUI() {
        if (intakeTimes.isEmpty()) {
            tvTimes.setText("No Times Added");
        } else {
            tvTimes.setText(TextUtils.join(", ", intakeTimes));
        }
    }

    private void updateInFirebase() {
        String name = etName.getText().toString().trim();
        String amount = etAmount.getText().toString().trim();

        if (name.isEmpty() || amount.isEmpty() || startDate == null || endDate == null) {
            Toast.makeText(this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdate.setEnabled(false);
        btnUpdate.setText("Updating...");

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("amount", amount);
        map.put("unit", spinnerUnit.getSelectedItem().toString());
        map.put("instruction", spinnerInstruction.getSelectedItem().toString());
        map.put("intakeTimes", intakeTimes);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("remarks", etRemarks.getText().toString().trim());

        db.collection("users").document(patientId).collection("medicines").document(docId).update(map)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Medicine Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("Update Medicine");
                });
    }
}
