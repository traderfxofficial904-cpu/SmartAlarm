package com.example.smartalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmData.all(context).filter { it.enabled }.forEach {
            AlarmScheduler.schedule(context, it)
        }
    }
}
