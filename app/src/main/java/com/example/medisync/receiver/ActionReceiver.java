package com.example.medisync.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles the "Taken" and "Dismiss" button taps directly from the notification.
 * "Taken" → updates Firestore intakeTimes.N.status = "taken" and cancels the notification.
 * "Dismiss" → just cancels the notification (status stays "pending").
 */
public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action    = intent.getAction();
        int    notifId   = intent.getIntExtra(NotificationHelper.EXTRA_NOTIF_ID, -1);

        // Always dismiss the notification first
        if (notifId != -1) {
            NotificationManager nm = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(notifId);
        }

        if (NotificationHelper.ACTION_TAKEN.equals(action)) {
            String medicineId = intent.getStringExtra(NotificationHelper.EXTRA_MED_ID);
            String patientUid = intent.getStringExtra(NotificationHelper.EXTRA_PATIENT_UID);
            int    intakeIndex = intent.getIntExtra(NotificationHelper.EXTRA_INTAKE_IDX, 0);

            if (medicineId == null || patientUid == null) return;

            // Update only the specific intake time's status in Firestore
            String fieldPath = "intakeTimes." + intakeIndex + ".status";

            FirebaseFirestore.getInstance()
                    .collection("users").document(patientUid)
                    .collection("medicines").document(medicineId)
                    .update(fieldPath, "taken")
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Medicine marked as taken ✔", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        // ACTION_DISMISS → notification already cancelled above, nothing else to do
    }
}