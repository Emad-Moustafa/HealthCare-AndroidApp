package com.emad.graduationproject

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WaterTrackerRepository {

    private val api  = RetrofitClient.api
    private val gson = Gson()
    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = AppDatabase.get(context)
    }

    private val dao      get() = db.waterDao()
    private val statsDao get() = db.statsCacheDao()

    // ── Goal ──────────────────────────────────────────────────────────────────

    suspend fun getDailyGoal(): Int = withContext(Dispatchers.IO) {
        try {
            val result = api.getGoal().data?.goalMl ?: 2500
            dao.upsertGoal(WaterGoalEntity(goalMl = result))
            result
        } catch (e: Exception) {
            dao.getGoal()?.goalMl ?: 2500
        }
    }

    suspend fun setDailyGoal(ml: Int) = withContext(Dispatchers.IO) {
        try {
            api.setGoal(GoalRequest(ml))
            dao.upsertGoal(WaterGoalEntity(goalMl = ml))
        } catch (e: Exception) { }
    }

    // ── Home ──────────────────────────────────────────────────────────────────

    suspend fun getHomeData(): HomeData? = withContext(Dispatchers.IO) {
        try {
            val data = api.getHome().data
            data?.let {
                dao.upsertHome(
                    WaterHomeEntity(
                        dailyGoalMl               = it.dailyGoalMl,
                        todayTotalMl              = it.todayTotalMl,
                        todayProgressPercent      = it.todayProgressPercent,
                        todayRemainingMl          = it.todayRemainingMl,
                        weeklyTotalMl             = it.weeklyTotalMl,
                        monthlyTotalMl            = it.monthlyTotalMl,
                        goalCompletionPercent7Days = it.goalCompletionPercent7Days,
                        dailyAverageMl7Days       = it.dailyAverageMl7Days,
                        dailyAverageLiters7Days   = it.dailyAverageLiters7Days
                    )
                )
            }
            data
        } catch (e: Exception) {
            dao.getHome()?.let {
                HomeData(
                    it.dailyGoalMl, it.todayTotalMl, it.todayProgressPercent,
                    it.todayRemainingMl, it.weeklyTotalMl, it.monthlyTotalMl,
                    it.goalCompletionPercent7Days, it.dailyAverageMl7Days,
                    it.dailyAverageLiters7Days
                )
            }
        }
    }

    // ── Logs ──────────────────────────────────────────────────────────────────

    suspend fun getTodayLogs(): List<WaterLog> = withContext(Dispatchers.IO) {
        try {
            val logs = api.getLogs().data ?: emptyList()
            dao.clearLogs()
            dao.upsertLogs(logs.map { WaterLogEntity(it.timestamp, it.amountMl, it.timeFormatted) })
            logs
        } catch (e: Exception) {
            dao.getLogs().map { WaterLog(it.amountMl, it.timestamp, it.timeFormatted) }
        }
    }

    suspend fun addLog(amountMl: Int): WaterLog? = withContext(Dispatchers.IO) {
        try {
            val log = api.addLog(IntakeRequest(amountMl)).data
            log?.let { dao.upsertLogs(listOf(WaterLogEntity(it.timestamp, it.amountMl, it.timeFormatted))) }
            log
        } catch (e: Exception) { null }
    }

    suspend fun deleteLog(timestamp: Long) = withContext(Dispatchers.IO) {
        try { api.deleteLog(timestamp) } catch (e: Exception) { }
        dao.deleteLog(timestamp)
    }

    suspend fun getTodayTotal(): Int = withContext(Dispatchers.IO) {
        try { api.getHome().data?.todayTotalMl ?: 0 }
        catch (e: Exception) { dao.getHome()?.todayTotalMl ?: 0 }
    }

    suspend fun getTodayProgress(): Float = withContext(Dispatchers.IO) {
        try {
            val home = api.getHome().data ?: return@withContext 0f
            home.todayProgressPercent / 100f
        } catch (e: Exception) {
            (dao.getHome()?.todayProgressPercent ?: 0) / 100f
        }
    }

    // ── Statistics ✅ محدَّثة: كل إحصائية بتتحفظ أوناين وبترجع أوفلاين ────────

    suspend fun getDailyStats(): DailyStats? = withContext(Dispatchers.IO) {
        try {
            val data = api.getDailyStats().data
            data?.let { statsDao.save(StatsCacheEntity("water_daily", gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get("water_daily")
                ?.let { runCatching { gson.fromJson(it.json, DailyStats::class.java) }.getOrNull() }
        }
    }

    suspend fun getWeeklyStats(): WeeklyStats? = withContext(Dispatchers.IO) {
        try {
            val data = api.getWeeklyStats().data
            data?.let { statsDao.save(StatsCacheEntity("water_weekly", gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get("water_weekly")
                ?.let { runCatching { gson.fromJson(it.json, WeeklyStats::class.java) }.getOrNull() }
        }
    }

    suspend fun getMonthlyStats(): MonthlyStats? = withContext(Dispatchers.IO) {
        try {
            val data = api.getMonthlyStats().data
            data?.let { statsDao.save(StatsCacheEntity("water_monthly", gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get("water_monthly")
                ?.let { runCatching { gson.fromJson(it.json, MonthlyStats::class.java) }.getOrNull() }
        }
    }

    suspend fun getQuarterlyStats(): QuarterlyStats? = withContext(Dispatchers.IO) {
        try {
            val data = api.getQuarterlyStats().data
            data?.let { statsDao.save(StatsCacheEntity("water_quarterly", gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get("water_quarterly")
                ?.let { runCatching { gson.fromJson(it.json, QuarterlyStats::class.java) }.getOrNull() }
        }
    }

    suspend fun getCustomQuarterlyStats(months: List<MonthPair>): QuarterlyStats? = withContext(Dispatchers.IO) {
        val key = "water_quarterly_custom_${months.joinToString { "${it.year}_${it.month}" }}"
        try {
            val data = api.getCustomQuarterlyStats(QuarterlyRequest(months)).data
            data?.let { statsDao.save(StatsCacheEntity(key, gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get(key)
                ?.let { runCatching { gson.fromJson(it.json, QuarterlyStats::class.java) }.getOrNull() }
        }
    }

    // ── Reminders ─────────────────────────────────────────────────────────────

    suspend fun getReminders(): List<ReminderItem> = withContext(Dispatchers.IO) {
        try {
            val items = api.getReminders().data ?: emptyList()
            dao.clearReminders()
            dao.upsertReminders(items.map { ReminderEntity(it.id, it.timeLabel, it.isEnabled, it.hour, it.minute) })
            items
        } catch (e: Exception) {
            dao.getReminders().map { ReminderItem(it.id, it.timeLabel, it.isEnabled, it.hour, it.minute) }
        }
    }

    suspend fun addReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        try {
            api.addReminder(ReminderRequest(item.id, item.timeLabel, item.isEnabled, item.hour, item.minute))
            dao.upsertReminders(listOf(ReminderEntity(item.id, item.timeLabel, item.isEnabled, item.hour, item.minute)))
        } catch (e: Exception) { }
    }

    suspend fun editReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        try {
            api.editReminder(item.id, ReminderRequest(item.id, item.timeLabel, item.isEnabled, item.hour, item.minute))
            dao.upsertReminders(listOf(ReminderEntity(item.id, item.timeLabel, item.isEnabled, item.hour, item.minute)))
        } catch (e: Exception) { }
    }

    suspend fun toggleReminder(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        try { api.toggleReminder(id, ToggleReminderRequest(isEnabled)) } catch (e: Exception) { }
    }

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        try { api.deleteReminder(id) } catch (e: Exception) { }
        dao.deleteReminder(id)
    }

    suspend fun isAutoReminderEnabled(): Boolean = withContext(Dispatchers.IO) {
        try { api.getAutoReminder().data?.isEnabled ?: true } catch (e: Exception) { true }
    }

    suspend fun setAutoReminderEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        try { api.setAutoReminder(AutoReminderRequest(enabled)) } catch (e: Exception) { }
    }
}