package com.example.medisync.patient;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.medisync.R;
import com.example.medisync.adapter.MedicineAdapter;
import com.example.medisync.auth.LoginActivity;
import com.example.medisync.model.Medicine;
import com.example.medisync.model.MedicineIntake;
import com.example.medisync.receiver.MedicineAlarmManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class PatientHomeActivity extends AppCompatActivity implements MedicineAdapter.OnIntakeClickListener {

    private TextView tvDateTitle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Calendar selectedDateCalendar = Calendar.getInstance();
    private MedicineAdapter adapter;
    private final List<MedicineIntake> intakeList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        checkNotificationPermission();
        checkExactAlarmPermission();
        checkFullScreenIntentPermission();

        tvDateTitle = findViewById(R.id.tvDateTitle);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView rvSchedule = findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicineAdapter(intakeList, this);
        rvSchedule.setAdapter(adapter);

        setupCalendar();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            updateTimetable(selectedDateCalendar);
        });

        // Menu button
        ImageButton imgBtnMenu = findViewById(R.id.imgBtnMenu);
        imgBtnMenu.setOnClickListener(this::showDropdownMenu);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void checkFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null && !nm.canUseFullScreenIntent()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
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

    private void setupCalendar() {
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

        updateTimetable(Calendar.getInstance());
    }

    private void updateTimetable(Calendar calendar) {
        this.selectedDateCalendar = calendar;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        tvDateTitle.setText("Schedule For " + sdf.format(calendar.getTime()));
        fetchSchedulesFromFirebase(calendar.getTime());
    }

    private void fetchSchedulesFromFirebase(Date selectedDate) {
        if (mAuth.getUid() == null) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Date targetStartOfDay = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999);
        Date targetEndOfDay = cal.getTime();

        db.collection("users").document(mAuth.getUid()).collection("medicines")
                .whereLessThanOrEqualTo("startDate", targetEndOfDay)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    intakeList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Date rangeEndDate = doc.getDate("endDate");

                        if (rangeEndDate != null && !rangeEndDate.before(targetStartOfDay)) {
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
                                    "You",
                                    mAuth.getUid(),
                                    doc.getString("remarks")
                            );

                            if (intakeTimesObj instanceof Map<?, ?>) {
                                Map<?, ?> rawMap = (Map<?, ?>) intakeTimesObj;
                                int intakeCounter = 0;
                                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                                    String time = "";
                                    String status = "pending";
                                    int finalIndex = intakeCounter;

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
                                            finalIndex = intakeCounter;
                                        }
                                    }

                                    if (time != null && !time.isEmpty()) {
                                        intakeMap.put(time, status);
                                        MedicineIntake intake = new MedicineIntake(medicine, time, status, finalIndex);
                                        intakeList.add(intake);

                                        // ── NEW: Set alarm if today and pending ──
                                        if ("pending".equalsIgnoreCase(status) && isToday(selectedDate)) {
                                            MedicineAlarmManager.setAlarm(
                                                    this,
                                                    medicine.getName(),
                                                    medicine.getDocumentId(),
                                                    medicine.getPatientUid(),
                                                    finalIndex,
                                                    time,
                                                    medicine.getAmount(),
                                                    medicine.getUnit(),
                                                    medicine.getInstruction()
                                            );
                                        }
                                    }
                                    intakeCounter++;
                                }
                            }
                        }
                    }

                    // Sort chronologically by time
                    Collections.sort(intakeList, (o1, o2) -> {
                        try {
                            Date d1 = timeFormat.parse(o1.getIntakeTime());
                            Date d2 = timeFormat.parse(o2.getIntakeTime());
                            if (d1 == null || d2 == null) return 0;
                            return d1.compareTo(d2);
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error loading schedule", e);
                    Toast.makeText(this, "Error Loading Schedule", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    @Override
    public void onIntakeClick(MedicineIntake intake) {
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

    private boolean isToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void showDropdownMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Profile");
        popup.getMenu().add(0, 2, 1, "Log Out");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    startActivity(new Intent(this, PatientProfileActivity.class));
                    return true;
                case 2:
                    logoutUser();
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PatientHomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}