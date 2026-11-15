package com.example.smartalarm

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AlarmRingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(Color.rgb(16, 19, 38))
        }
        val title = TextView(this).apply {
            text = "⏰"
            textSize = 64f
            gravity = Gravity.CENTER
        }
        val label = TextView(this).apply {
            text = intent.getStringExtra("alarm_label") ?: "Wake up!"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 36)
        }
        val snooze = Button(this).apply {
            text = "SNOOZE 5 MIN"
            setOnClickListener {
                startService(Intent(this@AlarmRingActivity, AlarmService::class.java)
                    .setAction(AlarmService.ACTION_SNOOZE))
                finish()
            }
        }
        val stop = Button(this).apply {
            text = "STOP ALARM"
            setOnClickListener {
                startService(Intent(this@AlarmRingActivity, AlarmService::class.java)
                    .setAction(AlarmService.ACTION_STOP))
                finish()
            }
        }
        root.addView(title); root.addView(label); root.addView(snooze)
        root.addView(stop)
        setContentView(root)
    }
}
