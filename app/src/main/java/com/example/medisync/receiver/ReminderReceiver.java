package com.example.medisync.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("medName");
        NotificationHelper.showNotification(context, "Medicine Reminder", "Time to take your " + medName);
    }
}
