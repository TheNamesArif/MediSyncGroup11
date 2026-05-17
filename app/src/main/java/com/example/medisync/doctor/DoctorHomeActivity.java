package com.example.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medisync.R;
import com.example.medisync.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class DoctorHomeActivity extends AppCompatActivity {

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

        // Define start and end date range
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);  // 1 month ago

        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);     // 1 month ahead

        // Build the calendar
        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                // Handle selected date
            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView, int dx, int dy) { }

            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });

        // Menu button
        ImageButton imgBtnMenu = findViewById(R.id.imgBtnMenu);
        imgBtnMenu.setOnClickListener(v -> showDropdownMenu(v));

        // Manage Schedule button
        View btnCreateSchedule = findViewById(R.id.btnCreateSchedule);
        btnCreateSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, ManageScheduleActivity.class);
            startActivity(intent);
        });

        // View Schedule History button
        View btnViewScheduleHistory = findViewById(R.id.btnViewPatients);
        btnViewScheduleHistory.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, ViewScheduleHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void showDropdownMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);

        // Add menu items manually
        popup.getMenu().add(0, 1, 0, "Profile");
        popup.getMenu().add(0, 2, 1, "Log out");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    // go to Profile
                    startActivity(new Intent(this, DoctorProfileActivity.class));
                    return true;
                case 2:
                    // Logout
                    logoutUser();
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    private void logoutUser() {
        // Sign out from Firebase Auth (clears session)
        FirebaseAuth.getInstance().signOut();

        // Give sign out message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login and clear back stack
        Intent intent = new Intent(DoctorHomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}