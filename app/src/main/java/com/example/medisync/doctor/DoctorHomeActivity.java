package com.example.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;
import com.example.medisync.adapter.MedicineAdapter;
import com.example.medisync.auth.LoginActivity;
import com.example.medisync.model.Medicine;
import com.example.medisync.model.MedicineIntake;
import com.example.medisync.patient.TakenStatusActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class DoctorHomeActivity extends AppCompatActivity implements MedicineAdapter.OnIntakeClickListener {

    private TextView tvDateTitle;
    private MedicineAdapter adapter;
    private final List<MedicineIntake> intakeList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_home);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI
        tvDateTitle = findViewById(R.id.tvDateTitle);
        RecyclerView rvSchedule = findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        
        // Fix: Pass 'this' as the second argument (OnIntakeClickListener)
        adapter = new MedicineAdapter(intakeList, this);
        rvSchedule.setAdapter(adapter);

        // Setup Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                updateTimetable(date);
            }
        });

        // Load initial data for today
        updateTimetable(Calendar.getInstance());

        // Menu button
        ImageButton imgBtnMenu = findViewById(R.id.imgBtnMenu);
        imgBtnMenu.setOnClickListener(this::showDropdownMenu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateTimetable(Calendar.getInstance());     // reload data
        } catch (Exception e){
            Toast.makeText(this, "Error Refreshing", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onIntakeClick(MedicineIntake intake) {
        // Now opens the same TakenStatusActivity shared with patients
        Intent intent = new Intent(this, TakenStatusActivity.class);
        intent.putExtra("medicineId", intake.getMedicine().getDocumentId());
        intent.putExtra("patientUid", intake.getMedicine().getPatientUid());
        intent.putExtra("patientName", intake.getMedicine().getPatientName());
        intent.putExtra("intakeTime", intake.getIntakeTime());
        intent.putExtra("intakeIndex", intake.getIndex());
        intent.putExtra("currentStatus", intake.getStatus());
        intent.putExtra("medName", intake.getMedicine().getName());
        intent.putExtra("medAmount", intake.getMedicine().getAmount() + " " + intake.getMedicine().getUnit());
        intent.putExtra("medInstruction", intake.getMedicine().getInstruction());
        intent.putExtra("medRemarks", intake.getMedicine().getRemarks());
        startActivity(intent);
    }

    private void updateTimetable(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        tvDateTitle.setText("Schedule For " + sdf.format(calendar.getTime()));
        fetchSchedulesFromFirebase(calendar.getTime());
    }

    private void fetchSchedulesFromFirebase(Date selectedDate) {
        if (mAuth.getUid() == null) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Date targetStart = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
        Date targetEnd = cal.getTime();

        db.collectionGroup("medicines")
                .whereEqualTo("doctorId", mAuth.getUid())
                .whereLessThanOrEqualTo("startDate", targetEnd)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    intakeList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Date rangeEndDate = doc.getDate("endDate");

                        if (rangeEndDate != null && !rangeEndDate.before(targetStart)) {
                            // Safe casting for intakeTimes Map
                            Object intakeTimesObj = doc.get("intakeTimes");
                            Map<String, String> intakeMap = new HashMap<>();

                            Medicine medicine = new Medicine(
                                    doc.getId(),
                                    doc.getString("name"),
                                    doc.getString("amount"),
                                    doc.getString("unit"),
                                    doc.getString("instruction"),
                                    intakeMap,
                                    doc.getString("patientName"),
                                    doc.getString("patientUid"),
                                    doc.getString("remarks")
                            );

                            if (intakeTimesObj instanceof Map<?, ?>) {
                                Map<?, ?> rawMap = (Map<?, ?>) intakeTimesObj;
                                int idx = 0;
                                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                                    String time = "";
                                    String status = "pending";
                                    int finalIndex = idx;

                                    if (entry.getValue() instanceof String) {
                                        // Old structure: Time -> Status
                                        time = (String) entry.getKey();
                                        status = (String) entry.getValue();
                                    } else if (entry.getValue() instanceof Map) {
                                        // New structure: Index -> {time, status}
                                        Map<?, ?> valMap = (Map<?, ?>) entry.getValue();
                                        time = (String) valMap.get("time");
                                        status = (String) valMap.get("status");
                                        try {
                                            finalIndex = Integer.parseInt(entry.getKey().toString());
                                        } catch (Exception e) {
                                            finalIndex = idx;
                                        }
                                    }

                                    if (time != null && !time.isEmpty()) {
                                        intakeMap.put(time, status);
                                        intakeList.add(new MedicineIntake(medicine, time, status, finalIndex));
                                    }
                                    idx++;
                                }
                            }
                        }
                    }

                    // Modern sorting logic
                    intakeList.sort((o1, o2) -> {
                        try {
                            Date t1 = timeFormat.parse(o1.getIntakeTime());
                            Date t2 = timeFormat.parse(o2.getIntakeTime());
                            return (t1 != null && t2 != null) ? t1.compareTo(t2) : 0;
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("DashboardError", "Fetch failed", e);
                    Toast.makeText(this, "Error Loading Schedule", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDropdownMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Profile");
        popup.getMenu().add(0, 2, 1, "Create Schedule");
        popup.getMenu().add(0, 3, 2, "Manage Schedule");
        popup.getMenu().add(0, 4, 3, "Patient Management");
        popup.getMenu().add(0, 5, 4, "Log Out");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                startActivity(new Intent(this, DoctorProfileActivity.class));
                return true;
            } else if (id == 2) {
                // Fixed: Uncommented and linked to ManageScheduleActivity
                startActivity(new Intent(this, ManageScheduleActivity.class));
                return true;
            } else if (id == 3) {
                // Fixed: Uncommented and linked to ViewScheduleHistoryActivity
                startActivity(new Intent(this, ViewScheduleHistoryActivity.class));
                return true;
            } else if (id == 4) {
                // Fixed: Uncommented and linked to PatientManagementActivity
                startActivity(new Intent(this, PatientManagementActivity.class));
                return true;
            } else if (id == 5) {
                logoutUser();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(DoctorHomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
