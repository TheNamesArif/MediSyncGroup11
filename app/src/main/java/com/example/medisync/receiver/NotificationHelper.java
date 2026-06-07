package com.example.medisync.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.medisync.R;
import com.example.medisync.patient.TakenStatusActivity;

public class NotificationHelper {
    public static final String CHANNEL_ID = "MEDICINE_ALARM_CHANNEL_V2";

    // Action strings — matched in ActionReceiver
    public static final String ACTION_TAKEN  = "com.example.medisync.ACTION_TAKEN";
    public static final String ACTION_DISMISS = "com.example.medisync.ACTION_DISMISS";

    // Extra keys shared between MedicineAlarmManager, ReminderReceiver, ActionReceiver
    public static final String EXTRA_MED_NAME    = "medName";
    public static final String EXTRA_MED_ID      = "medicineId";
    public static final String EXTRA_PATIENT_UID = "patientUid";
    public static final String EXTRA_INTAKE_IDX  = "intakeIndex";
    public static final String EXTRA_INTAKE_TIME = "intakeTime";
    public static final String EXTRA_MED_AMOUNT  = "medicineAmount";
    public static final String EXTRA_MED_UNIT    = "medicineUnit";
    public static final String EXTRA_MED_INSTR   = "medicineInstruction";
    public static final String EXTRA_NOTIF_ID    = "notificationId";

    public static void showNotification(Context context,
                                        String medName,
                                        String intakeTime,
                                        String medicineId,
                                        String patientUid,
                                        int intakeIndex,
                                        String amount,
                                        String unit,
                                        String instruction) {

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel (required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Medicine Alarms",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("High priority alarms for your medicine");
            channel.enableVibration(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            channel.setSound(alarmSound, audioAttributes);

            nm.createNotificationChannel(channel);
        }

        // Unique notification ID per medicine+time so each one is independent
        int notifId = (medicineId + intakeTime).hashCode();

        // ── Tap the notification → open TakenStatusActivity with full card ──
        Intent openIntent = new Intent(context, TakenStatusActivity.class);
        openIntent.putExtra(EXTRA_MED_NAME,    medName);
        openIntent.putExtra(EXTRA_MED_ID,      medicineId);
        openIntent.putExtra(EXTRA_PATIENT_UID, patientUid);
        openIntent.putExtra(EXTRA_INTAKE_IDX,  intakeIndex);
        openIntent.putExtra(EXTRA_INTAKE_TIME, intakeTime);
        openIntent.putExtra(EXTRA_MED_AMOUNT,  amount);
        openIntent.putExtra(EXTRA_MED_UNIT,    unit);
        openIntent.putExtra(EXTRA_MED_INSTR,   instruction);
        openIntent.putExtra(EXTRA_NOTIF_ID,    notifId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openPending = PendingIntent.getActivity(context, notifId,
                openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // ── "Taken" action button → ActionReceiver ──
        Intent takenIntent = new Intent(context, ActionReceiver.class);
        takenIntent.setAction(ACTION_TAKEN);
        takenIntent.putExtra(EXTRA_MED_ID,      medicineId);
        takenIntent.putExtra(EXTRA_PATIENT_UID, patientUid);
        takenIntent.putExtra(EXTRA_INTAKE_IDX,  intakeIndex);
        takenIntent.putExtra(EXTRA_NOTIF_ID,    notifId);

        PendingIntent takenPending = PendingIntent.getBroadcast(context, notifId + 1,
                takenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // ── "Dismiss" action button → ActionReceiver ──
        Intent dismissIntent = new Intent(context, ActionReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS);
        dismissIntent.putExtra(EXTRA_NOTIF_ID, notifId);

        PendingIntent dismissPending = PendingIntent.getBroadcast(context, notifId + 2,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // ── Build the notification ──
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("💊 Time to take " + medName)
                .setContentText(intakeTime + (instruction != null ? "  •  " + instruction : ""))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(intakeTime
                                + (amount != null ? "  •  " + amount + " " + (unit != null ? unit : "") : "")
                                + (instruction != null ? "\n" + instruction : "")))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setOngoing(true)
                .setFullScreenIntent(openPending, true)
                .setContentIntent(openPending)
                // Action buttons
                .addAction(R.mipmap.ic_launcher, "✔ Taken",  takenPending)
                .addAction(R.mipmap.ic_launcher, "✖ Dismiss", dismissPending);

        nm.notify(notifId, builder.build());
    }
}