package com.example.smartalarm

import android.content.Context
import org.json.JSONObject

data class AlarmData(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val daily: Boolean,
    val enabled: Boolean
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("hour", hour); put("minute", minute)
        put("label", label); put("daily", daily); put("enabled", enabled)
    }

    companion object {
        fun fromJson(raw: String): AlarmData? = try {
            val j = JSONObject(raw)
            AlarmData(j.getInt("id"), j.getInt("hour"), j.getInt("minute"),
                j.optString("label", "Alarm"), j.optBoolean("daily"),
                j.optBoolean("enabled", true))
        } catch (_: Exception) { null }

        fun all(context: Context): MutableList<AlarmData> {
            val p = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
            return p.all.values.filterIsInstance<String>()
                .mapNotNull { fromJson(it) }.sortedWith(compareBy({ it.hour }, { it.minute }))
                .toMutableList()
        }

        fun save(context: Context, alarm: AlarmData) {
            context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
                .edit().putString("alarm_${alarm.id}", alarm.toJson().toString()).apply()
        }

        fun remove(context: Context, id: Int) {
            context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
                .edit().remove("alarm_$id").apply()
        }
    }
}
