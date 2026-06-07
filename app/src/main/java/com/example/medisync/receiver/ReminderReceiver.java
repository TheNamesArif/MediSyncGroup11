package com.example.medisync.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract all extras passed by MedicineAlarmManager
        String medName    = intent.getStringExtra(NotificationHelper.EXTRA_MED_NAME);
        String medicineId = intent.getStringExtra(NotificationHelper.EXTRA_MED_ID);
        String patientUid = intent.getStringExtra(NotificationHelper.EXTRA_PATIENT_UID);
        int    intakeIndex = intent.getIntExtra(NotificationHelper.EXTRA_INTAKE_IDX, 0);
        String intakeTime = intent.getStringExtra(NotificationHelper.EXTRA_INTAKE_TIME);
        String amount     = intent.getStringExtra(NotificationHelper.EXTRA_MED_AMOUNT);
        String unit       = intent.getStringExtra(NotificationHelper.EXTRA_MED_UNIT);
        String instruction = intent.getStringExtra(NotificationHelper.EXTRA_MED_INSTR);

        NotificationHelper.showNotification(
                context,
                medName,
                intakeTime,
                medicineId,
                patientUid,
                intakeIndex,
                amount,
                unit,
                instruction
        );
    }
}