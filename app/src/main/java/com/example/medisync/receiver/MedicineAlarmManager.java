package com.example.medisync.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MedicineAlarmManager {

    private static final String TAG = "MedicineAlarmManager";

    /**
     * Schedules an exact alarm for a medicine intake.
     *
     * @param context  Application context
     * @param medName  Name of the medicine
     * @param timeStr  Time string in "hh:mm a" format (e.g., "10:00 AM")
     */
    public static void setAlarm(Context context, String medName, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            if (date == null) return;

            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(date);

            target.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            target.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);

            // If the time has already passed today, don't schedule it for today.
            // In a production app, you might schedule it for tomorrow if the medication is daily,
            // but for this implementation, we'll focus on today's schedule.
            if (target.before(now)) {
                Log.d(TAG, "Skipping alarm for " + medName + " at " + timeStr + " as time has passed today.");
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("medName", medName);

            // Create a unique RequestCode so multiple alarms can coexist.
            // Using a hash of name + time is a simple way to get a consistent ID.
            int requestCode = (medName + timeStr).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            target.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            target.getTimeInMillis(),
                            pendingIntent
                    );
                }
                Log.d(TAG, "Alarm set for " + medName + " at " + timeStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting alarm: " + e.getMessage());
        }
    }

    /**
     * Cancels an existing alarm.
     */
    public static void cancelAlarm(Context context, String medName, String timeStr) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        int requestCode = (medName + timeStr).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Alarm cancelled for " + medName + " at " + timeStr);
        }
    }
}
