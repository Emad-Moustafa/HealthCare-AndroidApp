package com.emad.graduationproject

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

data class DaySchedule(
    val dayOfWeek      : Int,
    val bedtimeHour    : Int       = 21,
    val bedtimeMinute  : Int       = 0,
    val hoursOfSleep   : Int       = 8,
    val minutesOfSleep : Int       = 0,
    val repeatDays     : List<Int> = listOf(1, 2, 3, 4, 5),
    val vibrateEnabled : Boolean   = true,
    val bedtimeEnabled : Boolean   = true,
    val alarmEnabled   : Boolean   = true
) {
    val wakeHour: Int get() {
        val total = bedtimeHour * 60 + bedtimeMinute + hoursOfSleep * 60 + minutesOfSleep
        return (total / 60) % 24
    }
    val wakeMinute: Int get() {
        val total = bedtimeHour * 60 + bedtimeMinute + hoursOfSleep * 60 + minutesOfSleep
        return total % 60
    }
    fun bedtimeFormatted(): String = formatTime(bedtimeHour, bedtimeMinute)
    fun alarmFormatted()  : String = formatTime(wakeHour, wakeMinute)
    fun sleepDurationLabel(): String = "${hoursOfSleep}h ${minutesOfSleep}min"
    fun sleepQualityPercent(): Int {
        val actual = hoursOfSleep * 60 + minutesOfSleep
        return minOf(actual * 100 / (8 * 60), 100)
    }
    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "am" else "pm"
        val h12  = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
        return String.format("%02d:%02d%s", h12, minute, amPm)
    }
}

typealias SleepSchedule = DaySchedule

object SleepRepository {

    private val api = RetrofitClient.api
    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = AppDatabase.get(context)
    }

    private val dao get() = db.sleepDao()

    suspend fun getTodaySchedule(): SleepScheduleData? = withContext(Dispatchers.IO) {
        try {
            val data = api.getSleepToday().data
            data?.let { dao.upsertSchedule(it.toEntity()) }
            data
        } catch (e: Exception) {
            val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            dao.getSchedule(todayDay)?.toData()
        }
    }

    suspend fun getAllSchedules(): List<SleepScheduleData> = withContext(Dispatchers.IO) {
        try {
            val list = api.getAllSleepSchedules().data ?: emptyList()
            dao.upsertSchedules(list.map { it.toEntity() })
            list
        } catch (e: Exception) {
            dao.getAllSchedules().map { it.toData() }
        }
    }

    suspend fun getScheduleForDay(day: Int): SleepScheduleData? = withContext(Dispatchers.IO) {
        try { api.getSleepSchedule(day).data }
        catch (e: Exception) { dao.getSchedule(day)?.toData() }
    }

    suspend fun updateSchedule(day: Int, req: UpdateSleepRequest): SleepScheduleData? = withContext(Dispatchers.IO) {
        try {
            val result = api.updateSleepSchedule(day, req).data
            result?.let { dao.upsertSchedule(it.toEntity()) }
            result
        } catch (e: Exception) { null }
    }

    suspend fun toggleBedtime(day: Int, enabled: Boolean) = withContext(Dispatchers.IO) {
        try { api.toggleBedtime(day, SleepToggleRequest(enabled)) } catch (e: Exception) { }
    }

    suspend fun toggleAlarm(day: Int, enabled: Boolean) = withContext(Dispatchers.IO) {
        try { api.toggleAlarm(day, SleepToggleRequest(enabled)) } catch (e: Exception) { }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun SleepScheduleData.toEntity() = SleepScheduleEntity(
        dayOfWeek, bedtimeFormatted, alarmFormatted, sleepDuration, sleepQualityPct,
        bedtimeHour, bedtimeMinute, wakeHour, wakeMinute,
        bedtimeEnabled, alarmEnabled, countdownBedtime, countdownAlarm
    )

    private fun SleepScheduleEntity.toData() = SleepScheduleData(
        dayOfWeek, bedtimeFormatted, alarmFormatted, sleepDuration, sleepQualityPct,
        bedtimeHour, bedtimeMinute, wakeHour, wakeMinute,
        bedtimeEnabled, alarmEnabled, countdownBedtime, countdownAlarm
    )
}