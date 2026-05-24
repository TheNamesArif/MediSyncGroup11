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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class DoctorHomeActivity extends AppCompatActivity {

    private TextView tvDateTitle;
    private RecyclerView rvSchedule;
    private MedicineAdapter adapter;
    private List<Medicine> medicineList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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
        rvSchedule = findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineAdapter(medicineList);
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

        // Load today
        updateTimetable(Calendar.getInstance());

        // Menu button
        ImageButton imgBtnMenu = findViewById(R.id.imgBtnMenu);
        imgBtnMenu.setOnClickListener(v -> showDropdownMenu(v));
    }

    private void updateTimetable(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        tvDateTitle.setText("Schedule For " + sdf.format(calendar.getTime()));
        fetchSchedulesFromFirebase(calendar.getTime());
    }

    private void fetchSchedulesFromFirebase(Date selectedDate) {
        if (mAuth.getUid() == null) return;

        // Strip time from selected date for accurate range comparison
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Date targetStart = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
        Date targetEnd = cal.getTime();

        // Query: Show all medicines where (StartDate <= end of day)
        db.collectionGroup("medicines")
                .whereEqualTo("doctorId", mAuth.getUid())
                .whereLessThanOrEqualTo("startDate", targetEnd)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    medicineList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Date rangeEndDate = doc.getDate("endDate");
                        
                        // Client-side Check: Ensure schedule hasn't ended yet
                        if (rangeEndDate != null && !rangeEndDate.before(targetStart)) {
                            // Extract medicine data (Flattened structure)
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
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("DashboardError", "Error fetching dashboard", e);
                    if (e.getMessage() != null && e.getMessage().contains("index")) {
                        Toast.makeText(this, "Setup Required: Create Firestore Index (Check Logcat Link)", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error Loading Schedule", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDropdownMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Profile");
        popup.getMenu().add(0, 2, 1, "Manage Schedule");
        popup.getMenu().add(0, 3, 2, "Schedule History");
        popup.getMenu().add(0, 4, 3, "Log Out");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: startActivity(new Intent(this, DoctorProfileActivity.class)); return true;
                case 2: startActivity(new Intent(this, ManageScheduleActivity.class)); return true;
                case 3: startActivity(new Intent(this, ViewScheduleHistoryActivity.class)); return true;
                case 4: logoutUser(); return true;
                default: return false;
            }
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
