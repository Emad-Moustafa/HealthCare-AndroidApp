package com.emad.graduationproject

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object BloodPressureTrackerRepository {

    private val api  = RetrofitClient.api
    private val gson = Gson()
    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = AppDatabase.get(context)
    }

    private val dao      get() = db.bpDao()
    private val statsDao get() = db.statsCacheDao()

    // ── Blood Pressure ────────────────────────────────────────────────────────

    suspend fun getAllBpReadings(): List<BloodPressureReadingData> = withContext(Dispatchers.IO) {
        try {
            val list = api.getBpReadings().data ?: emptyList()
            dao.upsertBpReadings(list.map { it.toEntity() })
            list
        } catch (e: Exception) {
            dao.getBpReadings().map { it.toBpData() }
        }
    }

    suspend fun getLatestBpReading(): BloodPressureReadingData? = withContext(Dispatchers.IO) {
        try { api.getLatestBpReading().data }
        catch (e: Exception) { dao.getBpReadings().firstOrNull()?.toBpData() }
    }

    suspend fun addBpReading(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        notes: String = ""
    ): BloodPressureReadingData? = withContext(Dispatchers.IO) {
        try {
            val result = api.addBpReading(AddBpReadingRequest(systolic, diastolic, pulse, notes)).data
            result?.let { dao.upsertBpReadings(listOf(it.toEntity())) }
            result
        } catch (e: Exception) { null }
    }

    suspend fun deleteBpReading(id: Long) = withContext(Dispatchers.IO) {
        try { api.deleteBpReading(id) } catch (e: Exception) { }
        dao.deleteBpReading(id)
    }

    // ✅ محدَّثة: بتتحفظ أوناين وبترجع أوفلاين
    suspend fun getBpStats(
        year: Int  = Calendar.getInstance().get(Calendar.YEAR),
        month: Int = Calendar.getInstance().get(Calendar.MONTH)
    ): BpStatsData? = withContext(Dispatchers.IO) {
        val key = "bp_stats_${year}_${month}"
        try {
            val data = api.getBpStats(year, month).data
            data?.let { statsDao.save(StatsCacheEntity(key, gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get(key)
                ?.let { runCatching { gson.fromJson(it.json, BpStatsData::class.java) }.getOrNull() }
        }
    }

    // ── Blood Sugar ───────────────────────────────────────────────────────────

    suspend fun getAllBsReadings(): List<BloodSugarReadingData> = withContext(Dispatchers.IO) {
        try {
            val list = api.getBsReadings().data ?: emptyList()
            dao.upsertBsReadings(list.map { it.toEntity() })
            list
        } catch (e: Exception) {
            dao.getBsReadings().map { it.toBsData() }
        }
    }

    suspend fun getLatestBsReading(): BloodSugarReadingData? = withContext(Dispatchers.IO) {
        try { api.getLatestBsReading().data }
        catch (e: Exception) { dao.getBsReadings().firstOrNull()?.toBsData() }
    }

    suspend fun addBsReading(
        glucose: Int,
        mealType: String,
        notes: String = ""
    ): BloodSugarReadingData? = withContext(Dispatchers.IO) {
        try {
            val result = api.addBsReading(AddBsReadingRequest(glucose, mealType, notes)).data
            result?.let { dao.upsertBsReadings(listOf(it.toEntity())) }
            result
        } catch (e: Exception) { null }
    }

    suspend fun deleteBsReading(id: Long) = withContext(Dispatchers.IO) {
        try { api.deleteBsReading(id) } catch (e: Exception) { }
        dao.deleteBsReading(id)
    }

    // ✅ محدَّثة: بتتحفظ أوناين وبترجع أوفلاين
    suspend fun getBsStats(
        year: Int  = Calendar.getInstance().get(Calendar.YEAR),
        month: Int = Calendar.getInstance().get(Calendar.MONTH)
    ): BsStatsData? = withContext(Dispatchers.IO) {
        val key = "bs_stats_${year}_${month}"
        try {
            val data = api.getBsStats(year, month).data
            data?.let { statsDao.save(StatsCacheEntity(key, gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get(key)
                ?.let { runCatching { gson.fromJson(it.json, BsStatsData::class.java) }.getOrNull() }
        }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun BloodPressureReadingData.toEntity() = BpReadingEntity(
        id, systolic, diastolic, pulse, notes, timestamp, formattedTime, category, categoryLabel
    )

    private fun BpReadingEntity.toBpData() = BloodPressureReadingData(
        id, systolic, diastolic, pulse, notes, timestamp, formattedTime, category, categoryLabel
    )

    private fun BloodSugarReadingData.toEntity() = BsReadingEntity(
        id, glucose, mealType, notes, timestamp, formattedTime, category, categoryLabel
    )

    private fun BsReadingEntity.toBsData() = BloodSugarReadingData(
        id, glucose, mealType, notes, timestamp, formattedTime, category, categoryLabel
    )
}