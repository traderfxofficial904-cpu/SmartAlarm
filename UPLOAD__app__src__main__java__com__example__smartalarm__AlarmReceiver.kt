package com.example.smartalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("alarm_id", -1)
        val label = intent.getStringExtra("alarm_label") ?: "Alarm"
        val daily = intent.getBooleanExtra("daily", false)

        if (id >= 0) {
            val alarm = AlarmData.all(context).firstOrNull { it.id == id }
            if (alarm != null) {
                if (daily) AlarmScheduler.schedule(context, alarm)
                else {
                    AlarmData.remove(context, id)
                }
            }
        }
        val service = Intent(context, AlarmService::class.java).apply {
            putExtra("alarm_label", label)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(service)
        else context.startService(service)
    }
}
