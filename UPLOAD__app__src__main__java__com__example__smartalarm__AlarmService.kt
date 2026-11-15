package com.example.smartalarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder

class AlarmService : Service() {
    private var player: MediaPlayer? = null
    private val channelId = "smart_alarm_ring"

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopAlarm(); return START_NOT_STICKY }
            ACTION_SNOOZE -> {
                stopAlarm()
                val snooze = AlarmData(-System.currentTimeMillis().toInt(), 
                    java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
                    java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE),
                    "Snoozed alarm", false, true)
                val c = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MINUTE, 5) }
                val snoozeData = snooze.copy(hour = c.get(java.util.Calendar.HOUR_OF_DAY),
                    minute = c.get(java.util.Calendar.MINUTE))
                val id = snoozeData.id
                AlarmData.save(this, snoozeData)
                AlarmScheduler.schedule(this, snoozeData)
                return START_NOT_STICKY
            }
        }
        val label = intent?.getStringExtra("alarm_label") ?: "Time to wake up!"
        startForeground(1001, buildNotification(label))
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            player = MediaPlayer().apply {
                setDataSource(this@AlarmService, uri)
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) { stopSelf() }
        return START_STICKY
    }

    private fun buildNotification(label: String): Notification {
        val open = PendingIntent.getActivity(this, 10,
            Intent(this, AlarmRingActivity::class.java).putExtra("alarm_label", label),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 11,
            Intent(this, AlarmService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val snooze = PendingIntent.getService(this, 12,
            Intent(this, AlarmService::class.java).setAction(ACTION_SNOOZE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText("Alarm is ringing")
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(Notification.PRIORITY_MAX)
            .setOngoing(true)
            .setFullScreenIntent(open, true)
            .setContentIntent(open)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stop)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 5 min", snooze)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarm ringing",
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications used when an alarm rings"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun stopAlarm() {
        try { player?.stop() } catch (_: Exception) {}
        player?.release()
        player = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.example.smartalarm.STOP"
        const val ACTION_SNOOZE = "com.example.smartalarm.SNOOZE"
    }
}
