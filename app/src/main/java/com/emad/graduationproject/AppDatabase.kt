package com.emad.graduationproject

import android.content.Context
import androidx.room.*

// ══════════════════════════════════════════════════════════════
//  ENTITIES  (لم تتغير)
// ══════════════════════════════════════════════════════════════

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey val timestamp: Long,
    val amountMl: Int,
    val timeFormatted: String = ""
)

@Entity(tableName = "water_goal")
data class WaterGoalEntity(
    @PrimaryKey val id: Int = 1,
    val goalMl: Int
)

@Entity(tableName = "water_home")
data class WaterHomeEntity(
    @PrimaryKey val id: Int = 1,
    val dailyGoalMl: Int,
    val todayTotalMl: Int,
    val todayProgressPercent: Int,
    val todayRemainingMl: Int,
    val weeklyTotalMl: Int,
    val monthlyTotalMl: Int,
    val goalCompletionPercent7Days: Int,
    val dailyAverageMl7Days: Int,
    val dailyAverageLiters7Days: Float
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val timeLabel: String,
    val isEnabled: Boolean,
    val hour: Int,
    val minute: Int
)

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey val id: String,
    val mood: String,
    val dateKey: String,
    val timestamp: Long
)

@Entity(tableName = "mood_home")
data class MoodHomeEntity(
    @PrimaryKey val id: Int = 1,
    val dayStreak: Int,
    val totalLogs: Int,
    val todayMood: String?
)

@Entity(tableName = "bp_readings")
data class BpReadingEntity(
    @PrimaryKey val id: Long,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val notes: String,
    val timestamp: Long,
    val formattedTime: String,
    val category: String,
    val categoryLabel: String
)

@Entity(tableName = "bs_readings")
data class BsReadingEntity(
    @PrimaryKey val id: Long,
    val glucose: Int,
    val mealType: String,
    val notes: String,
    val timestamp: Long,
    val formattedTime: String,
    val category: String,
    val categoryLabel: String
)

@Entity(tableName = "sleep_schedules")
data class SleepScheduleEntity(
    @PrimaryKey val dayOfWeek: Int,
    val bedtimeFormatted: String,
    val alarmFormatted: String,
    val sleepDuration: String,
    val sleepQualityPct: Int,
    val bedtimeHour: Int,
    val bedtimeMinute: Int,
    val wakeHour: Int,
    val wakeMinute: Int,
    val bedtimeEnabled: Boolean,
    val alarmEnabled: Boolean,
    val countdownBedtime: String,
    val countdownAlarm: String
)

@Entity(tableName = "body_scan_records")
data class BodyScanRecordEntity(
    @PrimaryKey val timestamp: Long,
    val bodyFatPercent: Double,
    val muscleMassPercent: Double,
    val waterPercent: Double,
    val bmi: Double,
    val bodyType: String,
    val photoUri: String
)

// ══════════════════════════════════════════════════════════════
//  ✅ جديد — Stats Cache
//  جدول واحد بسيط يخزن أي إحصائية كـ JSON بمفتاح نصي
// ══════════════════════════════════════════════════════════════

@Entity(tableName = "stats_cache")
data class StatsCacheEntity(
    @PrimaryKey val cacheKey: String,   // مثال: "water_daily", "bp_stats_2025_3"
    val json: String,                   // البيانات كاملة بصيغة JSON
    val savedAt: Long = System.currentTimeMillis()
)

// ══════════════════════════════════════════════════════════════
//  DAOs  (لم تتغير)
// ══════════════════════════════════════════════════════════════

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    suspend fun getLogs(): List<WaterLogEntity>

    @Upsert
    suspend fun upsertLogs(logs: List<WaterLogEntity>)

    @Query("DELETE FROM water_logs WHERE timestamp = :ts")
    suspend fun deleteLog(ts: Long)

    @Query("DELETE FROM water_logs")
    suspend fun clearLogs()

    @Upsert
    suspend fun upsertGoal(goal: WaterGoalEntity)

    @Query("SELECT * FROM water_goal WHERE id = 1")
    suspend fun getGoal(): WaterGoalEntity?

    @Upsert
    suspend fun upsertHome(home: WaterHomeEntity)

    @Query("SELECT * FROM water_home WHERE id = 1")
    suspend fun getHome(): WaterHomeEntity?

    @Upsert
    suspend fun upsertReminders(reminders: List<ReminderEntity>)

    @Query("SELECT * FROM reminders")
    suspend fun getReminders(): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    @Query("DELETE FROM reminders")
    suspend fun clearReminders()
}

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    suspend fun getEntries(): List<MoodEntryEntity>

    @Upsert
    suspend fun upsertEntries(entries: List<MoodEntryEntity>)

    @Upsert
    suspend fun upsertHome(home: MoodHomeEntity)

    @Query("SELECT * FROM mood_home WHERE id = 1")
    suspend fun getHome(): MoodHomeEntity?
}

@Dao
interface BpDao {
    @Query("SELECT * FROM bp_readings ORDER BY timestamp DESC")
    suspend fun getBpReadings(): List<BpReadingEntity>

    @Upsert
    suspend fun upsertBpReadings(readings: List<BpReadingEntity>)

    @Query("DELETE FROM bp_readings WHERE id = :id")
    suspend fun deleteBpReading(id: Long)

    @Query("SELECT * FROM bs_readings ORDER BY timestamp DESC")
    suspend fun getBsReadings(): List<BsReadingEntity>

    @Upsert
    suspend fun upsertBsReadings(readings: List<BsReadingEntity>)

    @Query("DELETE FROM bs_readings WHERE id = :id")
    suspend fun deleteBsReading(id: Long)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_schedules")
    suspend fun getAllSchedules(): List<SleepScheduleEntity>

    @Query("SELECT * FROM sleep_schedules WHERE dayOfWeek = :day")
    suspend fun getSchedule(day: Int): SleepScheduleEntity?

    @Upsert
    suspend fun upsertSchedules(schedules: List<SleepScheduleEntity>)

    @Upsert
    suspend fun upsertSchedule(schedule: SleepScheduleEntity)
}

@Dao
interface BodyScanDao {
    @Query("SELECT * FROM body_scan_records ORDER BY timestamp DESC")
    suspend fun getRecords(): List<BodyScanRecordEntity>

    @Upsert
    suspend fun upsertRecords(records: List<BodyScanRecordEntity>)

    @Query("DELETE FROM body_scan_records WHERE timestamp = :ts")
    suspend fun deleteRecord(ts: Long)
}

// ══════════════════════════════════════════════════════════════
//  ✅ جديد — DAO للـ Stats Cache
// ══════════════════════════════════════════════════════════════

@Dao
interface StatsCacheDao {
    @Query("SELECT * FROM stats_cache WHERE cacheKey = :key")
    suspend fun get(key: String): StatsCacheEntity?

    @Upsert
    suspend fun save(entity: StatsCacheEntity)
}

// ══════════════════════════════════════════════════════════════
//  DATABASE  (version رُفعت إلى 2 مع Migration آمن)
// ══════════════════════════════════════════════════════════════

@Database(
    entities = [
        WaterLogEntity::class,
        WaterGoalEntity::class,
        WaterHomeEntity::class,
        ReminderEntity::class,
        MoodEntryEntity::class,
        MoodHomeEntity::class,
        BpReadingEntity::class,
        BsReadingEntity::class,
        SleepScheduleEntity::class,
        BodyScanRecordEntity::class,
        StatsCacheEntity::class      // ✅ جديد
    ],
    version = 2,                     // ✅ رُفعت من 1 إلى 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
    abstract fun moodDao(): MoodDao
    abstract fun bpDao(): BpDao
    abstract fun sleepDao(): SleepDao
    abstract fun bodyScanDao(): BodyScanDao
    abstract fun statsCacheDao(): StatsCacheDao   // ✅ جديد

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context, AppDatabase::class.java, "health_cache.db")
                .addMigrations(MIGRATION_1_2)   // ✅ Migration آمن — لا يمسح البيانات القديمة
                .build().also { INSTANCE = it }
        }

        // يُضيف جدول stats_cache فقط — كل البيانات الموجودة تبقى كما هي
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS stats_cache (
                        cacheKey TEXT NOT NULL PRIMARY KEY,
                        json TEXT NOT NULL,
                        savedAt INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}