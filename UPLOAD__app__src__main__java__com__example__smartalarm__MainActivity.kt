package com.example.smartalarm

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Calendar

class MainActivity : Activity() {
    private lateinit var list: LinearLayout
    private val bg = Color.rgb(16, 19, 38)
    private val purple = Color.rgb(124, 92, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsIfNeeded()
        buildUi()
        refresh()
    }

    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 21)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                try { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName"))) } catch (_: Exception) {}
            }
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 20)
            setBackgroundColor(bg)
        }
        val heading = TextView(this).apply {
            text = "SMART ALARM"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 4)
        }
        val sub = TextView(this).apply {
            text = "Your time. Your routine."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        val add = Button(this).apply {
            text = "+  ADD NEW ALARM"
            setBackgroundColor(purple)
            setTextColor(Color.WHITE)
            setOnClickListener { addAlarm() }
        }
        val section = TextView(this).apply {
            text = "YOUR ALARMS"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(2, 26, 0, 12)
        }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(heading); root.addView(sub); root.addView(add); root.addView(section)
        root.addView(list)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun addAlarm() {
        val now = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val input = EditText(this).apply { hint = "Alarm name (e.g. Wake up)" }
            val daily = CheckBox(this).apply {
                text = "Repeat every day"
                setTextColor(Color.WHITE)
                isChecked = true
            }
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(28, 8, 28, 8)
                addView(input); addView(daily)
            }
            android.app.AlertDialog.Builder(this)
                .setTitle("Alarm details")
                .setView(box)
                .setPositiveButton("SAVE") { _, _ ->
                    val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                    val alarm = AlarmData(id, hour, minute,
                        input.text.toString().ifBlank { "Alarm" }, daily.isChecked, true)
                    AlarmData.save(this, alarm)
                    AlarmScheduler.schedule(this, alarm)
                    refresh()
                }
                .setNegativeButton("CANCEL", null).show()
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }

    private fun refresh() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val alarms = AlarmData.all(this)
        if (alarms.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No alarms yet. Tap the button above to add one."
                setTextColor(Color.LTGRAY); textSize = 15f; setPadding(4, 12, 4, 12)
            }
            list.addView(empty)
        }
        alarms.forEach { alarm ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(18, 14, 18, 14)
                setBackgroundColor(Color.rgb(31, 35, 61))
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            val time = TextView(this).apply {
                text = "%02d:%02d".format(alarm.hour, alarm.minute)
                textSize = 30f; setTextColor(Color.WHITE)
            }
            val detail = TextView(this).apply {
                text = "${alarm.label}  •  ${if (alarm.daily) "Every day" else "Once"}"
                textSize = 13f; setTextColor(Color.LTGRAY)
            }
            info.addView(time); info.addView(detail)
            val toggle = Switch(this).apply {
                isChecked = alarm.enabled
                setOnCheckedChangeListener { _, checked ->
                    val updated = alarm.copy(enabled = checked)
                    AlarmData.save(this@MainActivity, updated)
                    if (checked) AlarmScheduler.schedule(this@MainActivity, updated)
                    else AlarmScheduler.cancel(this@MainActivity, alarm.id)
                }
            }
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(toggle)
            val delete = Button(this).apply {
                text = "DELETE"
                setOnClickListener {
                    AlarmScheduler.cancel(this@MainActivity, alarm.id)
                    AlarmData.remove(this@MainActivity, alarm.id)
                    refresh()
                }
            }
            card.addView(row); card.addView(delete)
            val lp = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 }
            list.addView(card, lp)
        }
    }

    override fun onResume() { super.onResume(); if (::list.isInitialized) refresh() }
}
