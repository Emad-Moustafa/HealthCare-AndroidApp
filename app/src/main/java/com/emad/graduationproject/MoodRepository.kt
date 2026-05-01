package com.emad.graduationproject

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ── MoodType enum  (لم يتغير) ─────────────────────────────────────────────────
enum class MoodType(val label: String, val emoji: String, val colorKey: String) {
    AMAZING("Amazing", "😍", "amazing"),
    OKAY("Okay",       "😐", "okay"),
    SAD("Sad",         "😢", "sad"),
    ANGRY("Angry",     "😠", "angry")
}

// ── Repository ────────────────────────────────────────────────────────────────
object MoodRepository {

    private val api  = RetrofitClient.api
    private val gson = Gson()
    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = AppDatabase.get(context)
    }

    private val dao      get() = db.moodDao()
    private val statsDao get() = db.statsCacheDao()

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun addEntry(mood: MoodType): MoodEntry? = withContext(Dispatchers.IO) {
        try {
            val result = api.addMoodEntry(AddMoodRequest(mood.name)).data
            result?.let {
                dao.upsertEntries(listOf(MoodEntryEntity(it.id, it.mood, it.dateKey, it.timestamp)))
            }
            result
        } catch (e: Exception) { null }
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    suspend fun getAllEntries(): List<MoodEntry> = withContext(Dispatchers.IO) {
        try {
            val entries = api.getMoodEntries().data ?: emptyList()
            dao.upsertEntries(entries.map { MoodEntryEntity(it.id, it.mood, it.dateKey, it.timestamp) })
            entries
        } catch (e: Exception) {
            dao.getEntries().map { MoodEntry(it.id, it.mood, it.dateKey, it.timestamp) }
        }
    }

    suspend fun getTotalLogs(): Int = withContext(Dispatchers.IO) {
        try {
            val data = api.getMoodHome().data
            data?.let {
                dao.upsertHome(MoodHomeEntity(dayStreak = it.dayStreak, totalLogs = it.totalLogs, todayMood = it.todayMood))
            }
            data?.totalLogs ?: 0
        } catch (e: Exception) {
            dao.getHome()?.totalLogs ?: 0
        }
    }

    suspend fun getDayStreak(): Int = withContext(Dispatchers.IO) {
        try { api.getMoodHome().data?.dayStreak ?: 0 }
        catch (e: Exception) { dao.getHome()?.dayStreak ?: 0 }
    }

    suspend fun getTodayMood(): MoodType? = withContext(Dispatchers.IO) {
        try {
            val name = api.getMoodHome().data?.todayMood ?: return@withContext null
            MoodType.valueOf(name)
        } catch (e: Exception) {
            dao.getHome()?.todayMood?.let { runCatching { MoodType.valueOf(it) }.getOrNull() }
        }
    }

    // ✅ محدَّثة: بتتحفظ كلها أوناين وبترجع من الكاش أوفلاين

    suspend fun getMostCommonMood(): MoodType? = withContext(Dispatchers.IO) {
        try {
            val data = api.getMoodStats().data
            data?.let { statsDao.save(StatsCacheEntity("mood_stats", gson.toJson(it))) }
            data?.mostCommonMood?.let { runCatching { MoodType.valueOf(it) }.getOrNull() }
        } catch (e: Exception) {
            getCachedMoodStats()?.mostCommonMood
                ?.let { runCatching { MoodType.valueOf(it) }.getOrNull() }
        }
    }

    suspend fun getEntriesForMonth(monthKey: String): Map<String, MoodType> = withContext(Dispatchers.IO) {
        try {
            val data = api.getMoodStats().data
            data?.let { statsDao.save(StatsCacheEntity("mood_stats", gson.toJson(it))) }
            data?.calendarEntries
                ?.filter { it.key.startsWith(monthKey) }
                ?.mapValues { runCatching { MoodType.valueOf(it.value) }.getOrElse { MoodType.OKAY } }
                ?: emptyMap()
        } catch (e: Exception) {
            getCachedMoodStats()?.calendarEntries
                ?.filter { it.key.startsWith(monthKey) }
                ?.mapValues { runCatching { MoodType.valueOf(it.value) }.getOrElse { MoodType.OKAY } }
                ?: emptyMap()
        }
    }

    suspend fun getMoodCounts(): Map<MoodType, Int> = withContext(Dispatchers.IO) {
        try {
            val data = api.getMoodStats().data
            data?.let { statsDao.save(StatsCacheEntity("mood_stats", gson.toJson(it))) }
            data?.moodCounts?.entries
                ?.associate { (k, v) -> runCatching { MoodType.valueOf(k) }.getOrElse { MoodType.OKAY } to v }
                ?: emptyMap()
        } catch (e: Exception) {
            getCachedMoodStats()?.moodCounts?.entries
                ?.associate { (k, v) -> runCatching { MoodType.valueOf(k) }.getOrElse { MoodType.OKAY } to v }
                ?: emptyMap()
        }
    }

    suspend fun getStatsData(): MoodStatsData? = withContext(Dispatchers.IO) {
        try {
            val data = api.getMoodStats().data
            data?.let { statsDao.save(StatsCacheEntity("mood_stats", gson.toJson(it))) }
            data
        } catch (e: Exception) {
            getCachedMoodStats()
        }
    }

    // helper داخلي — يرجع الكاش المحفوظ
    private suspend fun getCachedMoodStats(): MoodStatsData? =
        statsDao.get("mood_stats")
            ?.let { runCatching { gson.fromJson(it.json, MoodStatsData::class.java) }.getOrNull() }
}