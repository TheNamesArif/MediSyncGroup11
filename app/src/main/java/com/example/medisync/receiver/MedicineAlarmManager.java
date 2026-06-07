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
     * Schedules an exact alarm for a specific medicine intake time.
     *
     * @param intakeIndex position in the intakeTimes array — needed so ActionReceiver
     *                    can update the correct Firestore field (intakeTimes.N.status)
     */
    public static void setAlarm(Context context,
                                String medName,
                                String medicineId,
                                String patientUid,
                                int    intakeIndex,
                                String timeStr,
                                String amount,
                                String unit,
                                String instruction) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            if (date == null) return;

            Calendar now    = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(date);

            target.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            target.set(Calendar.MINUTE,      timeCal.get(Calendar.MINUTE));
            target.set(Calendar.SECOND,      0);
            target.set(Calendar.MILLISECOND, 0);

            if (target.before(now)) {
                Log.d(TAG, "Skipping past alarm for " + medName + " at " + timeStr);
                return;
            }

            AlarmManager alarmManager = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra(NotificationHelper.EXTRA_MED_NAME,    medName);
            intent.putExtra(NotificationHelper.EXTRA_MED_ID,      medicineId);
            intent.putExtra(NotificationHelper.EXTRA_PATIENT_UID, patientUid);
            intent.putExtra(NotificationHelper.EXTRA_INTAKE_IDX,  intakeIndex);
            intent.putExtra(NotificationHelper.EXTRA_INTAKE_TIME, timeStr);
            intent.putExtra(NotificationHelper.EXTRA_MED_AMOUNT,  amount);
            intent.putExtra(NotificationHelper.EXTRA_MED_UNIT,    unit);
            intent.putExtra(NotificationHelper.EXTRA_MED_INSTR,   instruction);

            // Unique requestCode per medicine + intake index
            int requestCode = (medicineId + intakeIndex).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Check for exact alarm permission on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms. Using setAndAllowWhileIdle instead.");
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            target.getTimeInMillis(),
                            pendingIntent);
                    return;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.getTimeInMillis(),
                        pendingIntent);
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        target.getTimeInMillis(),
                        pendingIntent);
            }

            Log.d(TAG, "Alarm set: " + medName + " [" + intakeIndex + "] at " + timeStr);

        } catch (Exception e) {
            Log.e(TAG, "Error setting alarm: " + e.getMessage());
        }
    }

    /**
     * Cancels a previously scheduled alarm.
     */
    public static void cancelAlarm(Context context, String medicineId, int intakeIndex) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        int requestCode = (medicineId + intakeIndex).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Alarm cancelled: " + medicineId + " [" + intakeIndex + "]");
        }
    }
}