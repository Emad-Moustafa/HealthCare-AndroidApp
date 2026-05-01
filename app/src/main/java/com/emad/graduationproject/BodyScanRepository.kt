package com.emad.graduationproject

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BodyScanRepository {

    private val api  = RetrofitClient.api
    private val gson = Gson()
    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = AppDatabase.get(context)
    }

    private val dao      get() = db.bodyScanDao()
    private val statsDao get() = db.statsCacheDao()

    // ── Read ──────────────────────────────────────────────────────────────────

    suspend fun getAllRecords(): List<BodyScanRecord> = withContext(Dispatchers.IO) {
        try {
            val list = api.getBodyScanRecords().data?.map { it.toBodyScanRecord() } ?: emptyList()
            dao.upsertRecords(list.map { it.toEntity() })
            list
        } catch (e: Exception) {
            dao.getRecords().map { it.toRecord() }
        }
    }

    suspend fun getLatestRecord(): BodyScanRecord? = withContext(Dispatchers.IO) {
        try { api.getLatestBodyScanRecord().data?.toBodyScanRecord() }
        catch (e: Exception) { dao.getRecords().firstOrNull()?.toRecord() }
    }

    suspend fun getTodayRecords(): List<BodyScanRecord> = withContext(Dispatchers.IO) {
        try {
            api.getTodayBodyScanRecords().data?.records?.map { it.toBodyScanRecord() } ?: emptyList()
        } catch (e: Exception) {
            dao.getRecords().map { it.toRecord() }
        }
    }

    suspend fun getTodayLatestRecord(): BodyScanRecord? = withContext(Dispatchers.IO) {
        try { api.getTodayBodyScanRecords().data?.latestRecord?.toBodyScanRecord() }
        catch (e: Exception) { dao.getRecords().firstOrNull()?.toRecord() }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun saveRecord(record: BodyScanRecord): BodyScanRecord? = withContext(Dispatchers.IO) {
        try {
            val result = api.saveBodyScanRecord(
                AddBodyScanRequest(
                    bodyFatPercent    = record.bodyFatPercent,
                    muscleMassPercent = record.muscleMassPercent,
                    waterPercent      = record.waterPercent,
                    bmi               = record.bmi,
                    bodyType          = record.bodyType,
                    photoUri          = record.photoUri
                )
            ).data?.toBodyScanRecord()
            result?.let { dao.upsertRecords(listOf(it.toEntity())) }
            result
        } catch (e: Exception) { null }
    }

    suspend fun deleteRecord(timestamp: Long) = withContext(Dispatchers.IO) {
        try { api.deleteBodyScanRecord(timestamp) } catch (e: Exception) { }
        dao.deleteRecord(timestamp)
    }

    // ── Analytics ✅ محدَّثة: بتتحفظ أوناين وبترجع أوفلاين ─────────────────────

    suspend fun getBodyFatChange(): Double = withContext(Dispatchers.IO) {
        try {
            val data = api.getBodyScanStats().data
            data?.let { statsDao.save(StatsCacheEntity("bodyscan_stats", gson.toJson(it))) }
            data?.bodyFatChange ?: 0.0
        } catch (e: Exception) {
            getCachedBodyScanStats()?.bodyFatChange ?: 0.0
        }
    }

    suspend fun getMonthlyBodyFatHistory(): List<MonthlyBodyFatEntry> = withContext(Dispatchers.IO) {
        try {
            val data = api.getBodyScanStats().data
            data?.let { statsDao.save(StatsCacheEntity("bodyscan_stats", gson.toJson(it))) }
            data?.monthlyHistory?.map {
                MonthlyBodyFatEntry(it.monthLabel, it.bodyFatAvg, it.changeFromPrev)
            } ?: emptyList()
        } catch (e: Exception) {
            getCachedBodyScanStats()?.monthlyHistory?.map {
                MonthlyBodyFatEntry(it.monthLabel, it.bodyFatAvg, it.changeFromPrev)
            } ?: emptyList()
        }
    }

    // helper داخلي — يرجع الكاش المحفوظ
    private suspend fun getCachedBodyScanStats(): BodyScanStatsData? =
        statsDao.get("bodyscan_stats")
            ?.let { runCatching { gson.fromJson(it.json, BodyScanStatsData::class.java) }.getOrNull() }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun BodyScanRecordData.toBodyScanRecord() =
        BodyScanRecord(timestamp, bodyFatPercent, muscleMassPercent, waterPercent, bmi, bodyType, photoUri)

    private fun BodyScanRecord.toEntity() =
        BodyScanRecordEntity(timestamp, bodyFatPercent, muscleMassPercent, waterPercent, bmi, bodyType, photoUri)

    private fun BodyScanRecordEntity.toRecord() =
        BodyScanRecord(timestamp, bodyFatPercent, muscleMassPercent, waterPercent, bmi, bodyType, photoUri)
}