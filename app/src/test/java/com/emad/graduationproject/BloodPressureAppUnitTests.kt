package com.emad.graduationproject

// ─────────────────────────────────────────────────────────────────────────────
//  Unit Test Suite — Blood Pressure & Blood Sugar App
//
//  File layout
//  ───────────
//  Place this file at:
//    app/src/test/java/com/emad/graduationproject/BloodPressureAppUnitTests.kt
//
//  build.gradle (app-level) dependencies required
//  ───────────────────────────────────────────────
//  testImplementation "junit:junit:4.13.2"
//  testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0"
//  testImplementation "org.mockito.kotlin:mockito-kotlin:5.3.1"
//  testImplementation "org.robolectric:robolectric:4.12.2"
//  testImplementation "androidx.test:core-ktx:1.5.0"
//  testImplementation "com.google.code.gson:gson:2.10.1"
//
//  NOTE ON HealthApiService.kt
//  ───────────────────────────
//  You DO need HealthApiService.kt in your project. It defines all the shared
//  data classes (BloodPressureReadingData, BloodSugarReadingData, BpStatsData,
//  BsStatsData, AddBpReadingRequest, AddBsReadingRequest, BpTrendInfo,
//  ApiResponse, etc.) plus the Retrofit interface and RetrofitClient singleton.
//  Without it, both the main sources and these tests will not compile.
// ─────────────────────────────────────────────────────────────────────────────

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.kotlin.*

// ══════════════════════════════════════════════════════════════════════════════
// 1. DATA MODEL TESTS
//    Verify that data classes are constructed correctly and hold expected values.
// ══════════════════════════════════════════════════════════════════════════════

class BloodPressureReadingDataTest {

    @Test
    fun `BloodPressureReadingData stores all fields correctly`() {
        val reading = BloodPressureReadingData(
            id            = 1L,
            systolic      = 120,
            diastolic     = 80,
            pulse         = 72,
            notes         = "After rest",
            timestamp     = 1_700_000_000L,
            formattedTime = "2024-01-01 10:00",
            category      = "NORMAL",
            categoryLabel = "Normal"
        )

        assertEquals(1L,               reading.id)
        assertEquals(120,              reading.systolic)
        assertEquals(80,               reading.diastolic)
        assertEquals(72,               reading.pulse)
        assertEquals("After rest",     reading.notes)
        assertEquals(1_700_000_000L,   reading.timestamp)
        assertEquals("2024-01-01 10:00", reading.formattedTime)
        assertEquals("NORMAL",         reading.category)
        assertEquals("Normal",         reading.categoryLabel)
    }

    @Test
    fun `BloodPressureReadingData with empty notes is valid`() {
        val reading = BloodPressureReadingData(
            id = 2L, systolic = 130, diastolic = 85, pulse = 80,
            notes = "", timestamp = 0L, formattedTime = "",
            category = "HIGH_NORMAL", categoryLabel = "High Normal"
        )
        assertEquals("", reading.notes)
    }

    @Test
    fun `BloodPressureReadingData equality holds for same data`() {
        val r1 = BloodPressureReadingData(1L, 120, 80, 70, "note", 100L, "time", "NORMAL", "Normal")
        val r2 = BloodPressureReadingData(1L, 120, 80, 70, "note", 100L, "time", "NORMAL", "Normal")
        assertEquals(r1, r2)
    }
}

class BloodSugarReadingDataTest {

    @Test
    fun `BloodSugarReadingData stores all fields correctly`() {
        val reading = BloodSugarReadingData(
            id            = 5L,
            glucose       = 95,
            mealType      = "Fasting",
            notes         = "Morning reading",
            timestamp     = 1_700_000_100L,
            formattedTime = "2024-01-01 08:00",
            category      = "NORMAL",
            categoryLabel = "Normal"
        )

        assertEquals(5L,                  reading.id)
        assertEquals(95,                  reading.glucose)
        assertEquals("Fasting",           reading.mealType)
        assertEquals("Morning reading",   reading.notes)
        assertEquals("NORMAL",            reading.category)
        assertEquals("Normal",            reading.categoryLabel)
    }

    @Test
    fun `BloodSugarReadingData supports all defined meal types`() {
        val mealTypes = listOf("Fasting", "Before Meal", "After Meal", "Bedtime")
        mealTypes.forEach { mealType ->
            val reading = BloodSugarReadingData(
                id = 1L, glucose = 100, mealType = mealType,
                notes = "", timestamp = 0L, formattedTime = "",
                category = "NORMAL", categoryLabel = "Normal"
            )
            assertEquals(mealType, reading.mealType)
        }
    }
}

class AddBpReadingRequestTest {

    @Test
    fun `AddBpReadingRequest is constructed with required fields`() {
        val req = AddBpReadingRequest(systolic = 130, diastolic = 85, pulse = 75, notes = "Test")
        assertEquals(130,    req.systolic)
        assertEquals(85,     req.diastolic)
        assertEquals(75,     req.pulse)
        assertEquals("Test", req.notes)
    }

    @Test
    fun `AddBpReadingRequest defaults notes to empty string`() {
        val req = AddBpReadingRequest(systolic = 120, diastolic = 80, pulse = 70)
        assertEquals("", req.notes)
    }
}

class AddBsReadingRequestTest {

    @Test
    fun `AddBsReadingRequest stores all fields`() {
        val req = AddBsReadingRequest(glucose = 110, mealType = "After Meal", notes = "Lunch")
        assertEquals(110,         req.glucose)
        assertEquals("After Meal", req.mealType)
        assertEquals("Lunch",     req.notes)
    }

    @Test
    fun `AddBsReadingRequest defaults notes to empty string`() {
        val req = AddBsReadingRequest(glucose = 90, mealType = "Fasting")
        assertEquals("", req.notes)
    }
}

class BpTrendInfoTest {

    @Test
    fun `BpTrendInfo holds UP direction correctly`() {
        val trend = BpTrendInfo(direction = "UP", delta = "+5 mmHg", color = "#FF0000")
        assertEquals("UP",       trend.direction)
        assertEquals("+5 mmHg",  trend.delta)
        assertEquals("#FF0000",  trend.color)
    }

    @Test
    fun `BpTrendInfo holds DOWN direction correctly`() {
        val trend = BpTrendInfo(direction = "DOWN", delta = "-3 mmHg", color = "#00FF00")
        assertEquals("DOWN", trend.direction)
    }

    @Test
    fun `BpTrendInfo holds FLAT direction correctly`() {
        val trend = BpTrendInfo(direction = "FLAT", delta = "0 mmHg", color = "#888888")
        assertEquals("FLAT", trend.direction)
    }
}

class BpStatsDataTest {

    @Test
    fun `BpStatsData stores monthly averages`() {
        val stats = BpStatsData(
            monthlyAvgSystolic  = 122,
            monthlyAvgDiastolic = 81,
            systolicTrend  = BpTrendInfo("UP",   "+2", "#FF0000"),
            diastolicTrend = BpTrendInfo("FLAT", "0",  "#888888"),
            latestReading       = null,
            weeklyAvgSystolic   = 119,
            weeklyAvgDiastolic  = 79,
            highestReading      = null,
            lowestReading       = null
        )

        assertEquals(122, stats.monthlyAvgSystolic)
        assertEquals(81,  stats.monthlyAvgDiastolic)
        assertEquals(119, stats.weeklyAvgSystolic)
        assertEquals(79,  stats.weeklyAvgDiastolic)
    }

    @Test
    fun `BpStatsData allows null averages when no data exists`() {
        val stats = BpStatsData(
            monthlyAvgSystolic  = null,
            monthlyAvgDiastolic = null,
            systolicTrend  = BpTrendInfo("FLAT", "--", "#888888"),
            diastolicTrend = BpTrendInfo("FLAT", "--", "#888888"),
            latestReading       = null,
            weeklyAvgSystolic   = null,
            weeklyAvgDiastolic  = null,
            highestReading      = null,
            lowestReading       = null
        )
        assertNull(stats.monthlyAvgSystolic)
        assertNull(stats.monthlyAvgDiastolic)
        assertNull(stats.latestReading)
    }
}

class BsStatsDataTest {

    @Test
    fun `BsStatsData stores monthly and weekly averages`() {
        val stats = BsStatsData(
            monthlyAvg     = 105,
            fastingTrend   = BpTrendInfo("DOWN", "-3", "#00FF00"),
            afterMealTrend = BpTrendInfo("UP",   "+7", "#FF0000"),
            latestReading  = null,
            weeklyAvg      = 101,
            highestReading = null,
            lowestReading  = null
        )

        assertEquals(105, stats.monthlyAvg)
        assertEquals(101, stats.weeklyAvg)
        assertEquals("DOWN", stats.fastingTrend.direction)
        assertEquals("UP",   stats.afterMealTrend.direction)
    }

    @Test
    fun `BsStatsData allows null averages when no data present`() {
        val stats = BsStatsData(
            monthlyAvg     = null,
            fastingTrend   = BpTrendInfo("FLAT", "--", "#888888"),
            afterMealTrend = BpTrendInfo("FLAT", "--", "#888888"),
            latestReading  = null,
            weeklyAvg      = null,
            highestReading = null,
            lowestReading  = null
        )
        assertNull(stats.monthlyAvg)
        assertNull(stats.weeklyAvg)
    }
}

class ApiResponseTest {

    @Test
    fun `ApiResponse wraps success with data`() {
        val reading = BloodPressureReadingData(
            1L, 120, 80, 72, "", 0L, "", "NORMAL", "Normal"
        )
        val response: ApiResponse<BloodPressureReadingData> =
            ApiResponse(success = true, data = reading, message = null)
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(120, response.data!!.systolic)
    }

    @Test
    fun `ApiResponse represents failure with null data`() {
        val response: ApiResponse<BloodPressureReadingData> =
            ApiResponse(success = false, data = null, message = "Server error")
        assertFalse(response.success)
        assertNull(response.data)
        assertEquals("Server error", response.message)
    }

    @Test
    fun `ApiResponse wraps list data correctly`() {
        val list = listOf(
            BloodSugarReadingData(1L, 90, "Fasting",     "", 0L, "", "NORMAL", "Normal"),
            BloodSugarReadingData(2L, 145, "After Meal", "", 0L, "", "HIGH",   "High")
        )
        val response = ApiResponse(success = true, data = list)
        assertEquals(2, response.data!!.size)
        assertEquals(90,  response.data!![0].glucose)
        assertEquals(145, response.data!![1].glucose)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// 2. BLOOD PRESSURE VALIDATION LOGIC TESTS
//    The validate() method in BloodPressureAddActivity is extracted here into
//    a pure testable function so it can run without Android framework.
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Pure function extracted from BloodPressureAddActivity.validate().
 * Returns a pair: (isValid, errorMessage).
 * errorMessage is non-null only when isValid == false.
 */
fun validateBloodPressure(
    systolicText: String,
    diastolicText: String,
    pulseText: String
): Pair<Boolean, String?> {
    if (systolicText.isEmpty())  return false to "Please enter systolic pressure"
    if (diastolicText.isEmpty()) return false to "Please enter diastolic pressure"
    if (pulseText.isEmpty())     return false to "Please enter pulse rate"

    val sys  = systolicText.toIntOrNull()
    val dia  = diastolicText.toIntOrNull()
    val pls  = pulseText.toIntOrNull()

    if (sys  == null || sys  !in 60..300)  return false to "Systolic must be 60–300 mmHg"
    if (dia  == null || dia  !in 40..200)  return false to "Diastolic must be 40–200 mmHg"
    if (pls  == null || pls  !in 30..250)  return false to "Pulse must be 30–250 bpm"
    if (sys <= dia)                         return false to "Systolic must be greater than diastolic"

    return true to null
}

class BloodPressureValidationTest {

    // ── Empty field tests ─────────────────────────────────────────────────────

    @Test
    fun `empty systolic returns invalid`() {
        val (valid, msg) = validateBloodPressure("", "80", "72")
        assertFalse(valid)
        assertEquals("Please enter systolic pressure", msg)
    }

    @Test
    fun `empty diastolic returns invalid`() {
        val (valid, msg) = validateBloodPressure("120", "", "72")
        assertFalse(valid)
        assertEquals("Please enter diastolic pressure", msg)
    }

    @Test
    fun `empty pulse returns invalid`() {
        val (valid, msg) = validateBloodPressure("120", "80", "")
        assertFalse(valid)
        assertEquals("Please enter pulse rate", msg)
    }

    // ── Range boundary tests — systolic ───────────────────────────────────────

    @Test
    fun `systolic at lower boundary 60 is valid`() {
        val (valid, _) = validateBloodPressure("60", "40", "60")
        assertTrue(valid)
    }

    @Test
    fun `systolic at upper boundary 300 is valid`() {
        val (valid, _) = validateBloodPressure("300", "200", "72")
        assertTrue(valid)
    }

    @Test
    fun `systolic below lower boundary 59 is invalid`() {
        val (valid, msg) = validateBloodPressure("59", "40", "60")
        assertFalse(valid)
        assertEquals("Systolic must be 60–300 mmHg", msg)
    }

    @Test
    fun `systolic above upper boundary 301 is invalid`() {
        val (valid, msg) = validateBloodPressure("301", "80", "72")
        assertFalse(valid)
        assertEquals("Systolic must be 60–300 mmHg", msg)
    }

    // ── Range boundary tests — diastolic ──────────────────────────────────────

    @Test
    fun `diastolic at lower boundary 40 is valid`() {
        val (valid, _) = validateBloodPressure("80", "40", "60")
        assertTrue(valid)
    }

    @Test
    fun `diastolic at upper boundary 200 is valid`() {
        // systolic must still exceed diastolic
        val (valid, _) = validateBloodPressure("250", "200", "72")
        assertTrue(valid)
    }

    @Test
    fun `diastolic below lower boundary 39 is invalid`() {
        val (valid, msg) = validateBloodPressure("120", "39", "72")
        assertFalse(valid)
        assertEquals("Diastolic must be 40–200 mmHg", msg)
    }

    @Test
    fun `diastolic above upper boundary 201 is invalid`() {
        val (valid, msg) = validateBloodPressure("120", "201", "72")
        assertFalse(valid)
        assertEquals("Diastolic must be 40–200 mmHg", msg)
    }

    // ── Range boundary tests — pulse ──────────────────────────────────────────

    @Test
    fun `pulse at lower boundary 30 is valid`() {
        val (valid, _) = validateBloodPressure("120", "80", "30")
        assertTrue(valid)
    }

    @Test
    fun `pulse at upper boundary 250 is valid`() {
        val (valid, _) = validateBloodPressure("120", "80", "250")
        assertTrue(valid)
    }

    @Test
    fun `pulse below lower boundary 29 is invalid`() {
        val (valid, msg) = validateBloodPressure("120", "80", "29")
        assertFalse(valid)
        assertEquals("Pulse must be 30–250 bpm", msg)
    }

    @Test
    fun `pulse above upper boundary 251 is invalid`() {
        val (valid, msg) = validateBloodPressure("120", "80", "251")
        assertFalse(valid)
        assertEquals("Pulse must be 30–250 bpm", msg)
    }

    // ── Systolic > Diastolic rule ─────────────────────────────────────────────

    @Test
    fun `systolic equal to diastolic is invalid`() {
        val (valid, msg) = validateBloodPressure("120", "120", "72")
        assertFalse(valid)
        assertEquals("Systolic must be greater than diastolic", msg)
    }

    @Test
    fun `systolic less than diastolic is invalid`() {
        val (valid, msg) = validateBloodPressure("80", "120", "72")
        assertFalse(valid)
        assertEquals("Systolic must be greater than diastolic", msg)
    }

    @Test
    fun `systolic one above diastolic is valid`() {
        // smallest valid difference
        val (valid, _) = validateBloodPressure("81", "80", "60")
        assertTrue(valid)
    }

    // ── Non-numeric input ─────────────────────────────────────────────────────

    @Test
    fun `non-numeric systolic is invalid`() {
        val (valid, msg) = validateBloodPressure("abc", "80", "72")
        assertFalse(valid)
        assertEquals("Systolic must be 60–300 mmHg", msg)
    }

    @Test
    fun `non-numeric diastolic is invalid`() {
        val (valid, msg) = validateBloodPressure("120", "xyz", "72")
        assertFalse(valid)
        assertEquals("Diastolic must be 40–200 mmHg", msg)
    }

    @Test
    fun `non-numeric pulse is invalid`() {
        val (valid, msg) = validateBloodPressure("120", "80", "!!")
        assertFalse(valid)
        assertEquals("Pulse must be 30–250 bpm", msg)
    }

    // ── Happy-path (normal medical reading) ───────────────────────────────────

    @Test
    fun `normal blood pressure reading is valid`() {
        val (valid, msg) = validateBloodPressure("120", "80", "72")
        assertTrue(valid)
        assertNull(msg)
    }

    @Test
    fun `hypertensive reading within allowed range is valid`() {
        val (valid, _) = validateBloodPressure("180", "110", "90")
        assertTrue(valid)
    }

    @Test
    fun `low blood pressure reading within allowed range is valid`() {
        val (valid, _) = validateBloodPressure("90", "60", "55")
        assertTrue(valid)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// 3. BLOOD SUGAR VALIDATION LOGIC TESTS
// ══════════════════════════════════════════════════════════════════════════════

private val VALID_MEAL_TYPES = listOf("Fasting", "Before Meal", "After Meal", "Bedtime")

/**
 * Pure function extracted from BloodSugarAddActivity.validate().
 */
fun validateBloodSugar(
    glucoseText: String,
    mealTypeText: String
): Pair<Boolean, String?> {
    if (glucoseText.isEmpty()) return false to "Please enter your glucose level"

    val g = glucoseText.toIntOrNull()
    if (g == null || g !in 20..600) return false to "Glucose must be 20–600 mg/dL"

    if (mealTypeText.isEmpty() || mealTypeText !in VALID_MEAL_TYPES)
        return false to "Please select a meal type"

    return true to null
}

class BloodSugarValidationTest {

    // ── Empty field tests ─────────────────────────────────────────────────────

    @Test
    fun `empty glucose returns invalid`() {
        val (valid, msg) = validateBloodSugar("", "Fasting")
        assertFalse(valid)
        assertEquals("Please enter your glucose level", msg)
    }

    @Test
    fun `empty meal type returns invalid`() {
        val (valid, msg) = validateBloodSugar("100", "")
        assertFalse(valid)
        assertEquals("Please select a meal type", msg)
    }

    // ── Range boundary tests — glucose ────────────────────────────────────────

    @Test
    fun `glucose at lower boundary 20 is valid`() {
        val (valid, _) = validateBloodSugar("20", "Fasting")
        assertTrue(valid)
    }

    @Test
    fun `glucose at upper boundary 600 is valid`() {
        val (valid, _) = validateBloodSugar("600", "After Meal")
        assertTrue(valid)
    }

    @Test
    fun `glucose below lower boundary 19 is invalid`() {
        val (valid, msg) = validateBloodSugar("19", "Fasting")
        assertFalse(valid)
        assertEquals("Glucose must be 20–600 mg/dL", msg)
    }

    @Test
    fun `glucose above upper boundary 601 is invalid`() {
        val (valid, msg) = validateBloodSugar("601", "Fasting")
        assertFalse(valid)
        assertEquals("Glucose must be 20–600 mg/dL", msg)
    }

    // ── Meal type validation ──────────────────────────────────────────────────

    @Test
    fun `Fasting meal type is accepted`() {
        val (valid, _) = validateBloodSugar("90", "Fasting")
        assertTrue(valid)
    }

    @Test
    fun `Before Meal meal type is accepted`() {
        val (valid, _) = validateBloodSugar("100", "Before Meal")
        assertTrue(valid)
    }

    @Test
    fun `After Meal meal type is accepted`() {
        val (valid, _) = validateBloodSugar("140", "After Meal")
        assertTrue(valid)
    }

    @Test
    fun `Bedtime meal type is accepted`() {
        val (valid, _) = validateBloodSugar("110", "Bedtime")
        assertTrue(valid)
    }

    @Test
    fun `unknown meal type is rejected`() {
        val (valid, msg) = validateBloodSugar("100", "Lunch")
        assertFalse(valid)
        assertEquals("Please select a meal type", msg)
    }

    @Test
    fun `case-sensitive meal type check rejects lowercase fasting`() {
        val (valid, msg) = validateBloodSugar("100", "fasting")
        assertFalse(valid)
        assertEquals("Please select a meal type", msg)
    }

    // ── Non-numeric glucose ───────────────────────────────────────────────────

    @Test
    fun `non-numeric glucose is invalid`() {
        val (valid, msg) = validateBloodSugar("high", "Fasting")
        assertFalse(valid)
        assertEquals("Glucose must be 20–600 mg/dL", msg)
    }

    @Test
    fun `decimal glucose is invalid (integer only field)`() {
        val (valid, msg) = validateBloodSugar("100.5", "Fasting")
        assertFalse(valid)
        assertEquals("Glucose must be 20–600 mg/dL", msg)
    }

    // ── Happy-path ────────────────────────────────────────────────────────────

    @Test
    fun `normal fasting glucose is valid`() {
        val (valid, msg) = validateBloodSugar("95", "Fasting")
        assertTrue(valid)
        assertNull(msg)
    }

    @Test
    fun `post-meal glucose is valid`() {
        val (valid, msg) = validateBloodSugar("145", "After Meal")
        assertTrue(valid)
        assertNull(msg)
    }

    @Test
    fun `diabetic-range glucose is valid if within allowed range`() {
        val (valid, _) = validateBloodSugar("400", "After Meal")
        assertTrue(valid)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// 4. REPOSITORY TESTS  (online + offline fallback)
//    Uses Mockito-Kotlin to mock the DAO and API, then drives the
//    BloodPressureTrackerRepository suspend functions via runTest.
// ══════════════════════════════════════════════════════════════════════════════

// ── Fake / stub entity types (mirrors Room entities used by the repository) ──

data class BpReadingEntity(
    val id: Long, val systolic: Int, val diastolic: Int, val pulse: Int,
    val notes: String, val timestamp: Long, val formattedTime: String,
    val category: String, val categoryLabel: String
) {
    fun toBpData() = BloodPressureReadingData(
        id, systolic, diastolic, pulse, notes, timestamp, formattedTime, category, categoryLabel
    )
}

data class BsReadingEntity(
    val id: Long, val glucose: Int, val mealType: String, val notes: String,
    val timestamp: Long, val formattedTime: String, val category: String, val categoryLabel: String
) {
    fun toBsData() = BloodSugarReadingData(
        id, glucose, mealType, notes, timestamp, formattedTime, category, categoryLabel
    )
}

data class StatsCacheEntity(val key: String, val json: String)

// ── Interfaces matching the DAO contracts used by the repository ──────────────

interface BpDao {
    suspend fun getBpReadings(): List<BpReadingEntity>
    suspend fun upsertBpReadings(readings: List<BpReadingEntity>)
    suspend fun deleteBpReading(id: Long)
    suspend fun getBsReadings(): List<BsReadingEntity>
    suspend fun upsertBsReadings(readings: List<BsReadingEntity>)
    suspend fun deleteBsReading(id: Long)
}

interface StatsCacheDao {
    suspend fun save(entity: StatsCacheEntity)
    suspend fun get(key: String): StatsCacheEntity?
}

// ── Fake repository that drives the same logic without Android context ────────
//    This mirrors BloodPressureTrackerRepository but receives its dependencies
//    via constructor injection — making it directly testable.

class TestableBloodPressureRepository(
    private val api: HealthApiService,
    private val bpDao: BpDao,
    private val statsDao: StatsCacheDao,
    private val gson: com.google.gson.Gson = com.google.gson.Gson()
) {
    suspend fun getAllBpReadings(): List<BloodPressureReadingData> =
        try {
            val list = api.getBpReadings().data ?: emptyList()
            bpDao.upsertBpReadings(list.map {
                BpReadingEntity(it.id, it.systolic, it.diastolic, it.pulse, it.notes,
                    it.timestamp, it.formattedTime, it.category, it.categoryLabel)
            })
            list
        } catch (e: Exception) {
            bpDao.getBpReadings().map { it.toBpData() }
        }

    suspend fun getLatestBpReading(): BloodPressureReadingData? =
        try { api.getLatestBpReading().data }
        catch (e: Exception) { bpDao.getBpReadings().firstOrNull()?.toBpData() }

    suspend fun addBpReading(systolic: Int, diastolic: Int, pulse: Int, notes: String = "")
            : BloodPressureReadingData? =
        try {
            val result = api.addBpReading(AddBpReadingRequest(systolic, diastolic, pulse, notes)).data
            result?.let {
                bpDao.upsertBpReadings(listOf(
                    BpReadingEntity(it.id, it.systolic, it.diastolic, it.pulse, it.notes,
                        it.timestamp, it.formattedTime, it.category, it.categoryLabel)
                ))
            }
            result
        } catch (e: Exception) { null }

    suspend fun deleteBpReading(id: Long) {
        try { api.deleteBpReading(id) } catch (_: Exception) { }
        bpDao.deleteBpReading(id)
    }

    suspend fun getAllBsReadings(): List<BloodSugarReadingData> =
        try {
            val list = api.getBsReadings().data ?: emptyList()
            bpDao.upsertBsReadings(list.map {
                BsReadingEntity(it.id, it.glucose, it.mealType, it.notes,
                    it.timestamp, it.formattedTime, it.category, it.categoryLabel)
            })
            list
        } catch (e: Exception) {
            bpDao.getBsReadings().map { it.toBsData() }
        }

    suspend fun getLatestBsReading(): BloodSugarReadingData? =
        try { api.getLatestBsReading().data }
        catch (e: Exception) { bpDao.getBsReadings().firstOrNull()?.toBsData() }

    suspend fun addBsReading(glucose: Int, mealType: String, notes: String = "")
            : BloodSugarReadingData? =
        try {
            val result = api.addBsReading(AddBsReadingRequest(glucose, mealType, notes)).data
            result?.let {
                bpDao.upsertBsReadings(listOf(
                    BsReadingEntity(it.id, it.glucose, it.mealType, it.notes,
                        it.timestamp, it.formattedTime, it.category, it.categoryLabel)
                ))
            }
            result
        } catch (e: Exception) { null }

    suspend fun deleteBsReading(id: Long) {
        try { api.deleteBsReading(id) } catch (_: Exception) { }
        bpDao.deleteBsReading(id)
    }

    suspend fun getBpStats(year: Int, month: Int): BpStatsData? {
        val key = "bp_stats_${year}_${month}"
        return try {
            val data = api.getBpStats(year, month).data
            data?.let { statsDao.save(StatsCacheEntity(key, gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get(key)
                ?.let { runCatching { gson.fromJson(it.json, BpStatsData::class.java) }.getOrNull() }
        }
    }

    suspend fun getBsStats(year: Int, month: Int): BsStatsData? {
        val key = "bs_stats_${year}_${month}"
        return try {
            val data = api.getBsStats(year, month).data
            data?.let { statsDao.save(StatsCacheEntity(key, gson.toJson(it))) }
            data
        } catch (e: Exception) {
            statsDao.get(key)
                ?.let { runCatching { gson.fromJson(it.json, BsStatsData::class.java) }.getOrNull() }
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

private fun makeBpReading(id: Long = 1L, systolic: Int = 120, diastolic: Int = 80) =
    BloodPressureReadingData(id, systolic, diastolic, 72, "", 0L, "", "NORMAL", "Normal")

private fun makeBsReading(id: Long = 1L, glucose: Int = 95, mealType: String = "Fasting") =
    BloodSugarReadingData(id, glucose, mealType, "", 0L, "", "NORMAL", "Normal")

private fun makeBpEntity(id: Long = 1L, systolic: Int = 120, diastolic: Int = 80) =
    BpReadingEntity(id, systolic, diastolic, 72, "", 0L, "", "NORMAL", "Normal")

private fun makeBsEntity(id: Long = 1L, glucose: Int = 95, mealType: String = "Fasting") =
    BsReadingEntity(id, glucose, mealType, "", 0L, "", "NORMAL", "Normal")

private fun makeBpStats() = BpStatsData(
    monthlyAvgSystolic = 121, monthlyAvgDiastolic = 79,
    systolicTrend  = BpTrendInfo("FLAT", "0", "#888888"),
    diastolicTrend = BpTrendInfo("FLAT", "0", "#888888"),
    latestReading  = null, weeklyAvgSystolic = 119, weeklyAvgDiastolic = 78,
    highestReading = null, lowestReading = null
)

private fun makeBsStats() = BsStatsData(
    monthlyAvg = 103,
    fastingTrend   = BpTrendInfo("FLAT", "0", "#888888"),
    afterMealTrend = BpTrendInfo("FLAT", "0", "#888888"),
    latestReading  = null, weeklyAvg = 99,
    highestReading = null, lowestReading = null
)

// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryBpReadingsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var api: HealthApiService
    private lateinit var dao: BpDao
    private lateinit var statsDao: StatsCacheDao
    private lateinit var repo: TestableBloodPressureRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        api      = mock()
        dao      = mock()
        statsDao = mock()
        repo     = TestableBloodPressureRepository(api, dao, statsDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── getAllBpReadings ───────────────────────────────────────────────────────

    @Test
    fun `getAllBpReadings returns API list on success`() = runTest {
        val readings = listOf(makeBpReading(1L, 120, 80), makeBpReading(2L, 130, 85))
        whenever(api.getBpReadings()).thenReturn(ApiResponse(true, readings))

        val result = repo.getAllBpReadings()

        assertEquals(2, result.size)
        assertEquals(120, result[0].systolic)
        assertEquals(130, result[1].systolic)
        verify(dao).upsertBpReadings(any())
    }

    @Test
    fun `getAllBpReadings falls back to DAO when API throws`() = runTest {
        whenever(api.getBpReadings()).thenThrow(RuntimeException("No network"))
        whenever(dao.getBpReadings()).thenReturn(listOf(makeBpEntity(1L, 115, 75)))

        val result = repo.getAllBpReadings()

        assertEquals(1, result.size)
        assertEquals(115, result[0].systolic)
    }

    @Test
    fun `getAllBpReadings returns empty list when API returns null data`() = runTest {
        whenever(api.getBpReadings()).thenReturn(ApiResponse(true, null))

        val result = repo.getAllBpReadings()

        assertTrue(result.isEmpty())
    }

    // ── getLatestBpReading ────────────────────────────────────────────────────

    @Test
    fun `getLatestBpReading returns API value on success`() = runTest {
        val reading = makeBpReading(1L, 118, 76)
        whenever(api.getLatestBpReading()).thenReturn(ApiResponse(true, reading))

        val result = repo.getLatestBpReading()

        assertNotNull(result)
        assertEquals(118, result!!.systolic)
    }

    @Test
    fun `getLatestBpReading falls back to first DAO entry on API failure`() = runTest {
        whenever(api.getLatestBpReading()).thenThrow(RuntimeException("Timeout"))
        whenever(dao.getBpReadings()).thenReturn(listOf(makeBpEntity(3L, 125, 82)))

        val result = repo.getLatestBpReading()

        assertNotNull(result)
        assertEquals(125, result!!.systolic)
    }

    @Test
    fun `getLatestBpReading returns null when API returns null and DAO is empty`() = runTest {
        whenever(api.getLatestBpReading()).thenReturn(ApiResponse(true, null))

        val result = repo.getLatestBpReading()

        assertNull(result)
    }

    // ── addBpReading ──────────────────────────────────────────────────────────

    @Test
    fun `addBpReading returns saved reading on API success`() = runTest {
        val saved = makeBpReading(10L, 122, 81)
        whenever(api.addBpReading(any())).thenReturn(ApiResponse(true, saved))

        val result = repo.addBpReading(122, 81, 70, "Evening")

        assertNotNull(result)
        assertEquals(122, result!!.systolic)
        verify(dao).upsertBpReadings(any())
    }

    @Test
    fun `addBpReading returns null on API failure`() = runTest {
        whenever(api.addBpReading(any())).thenThrow(RuntimeException("Connection refused"))

        val result = repo.addBpReading(120, 80, 72)

        assertNull(result)
    }

    @Test
    fun `addBpReading passes correct values to API`() = runTest {
        val saved = makeBpReading(1L, 135, 88)
        whenever(api.addBpReading(any())).thenReturn(ApiResponse(true, saved))

        repo.addBpReading(135, 88, 80, "After medication")

        verify(api).addBpReading(AddBpReadingRequest(135, 88, 80, "After medication"))
    }

    // ── deleteBpReading ───────────────────────────────────────────────────────

    @Test
    fun `deleteBpReading calls DAO even when API throws`() = runTest {
        whenever(api.deleteBpReading(any())).thenThrow(RuntimeException("Server error"))

        repo.deleteBpReading(5L)

        verify(dao).deleteBpReading(5L)
    }

    @Test
    fun `deleteBpReading calls both API and DAO on success`() = runTest {
        whenever(api.deleteBpReading(any())).thenReturn(ApiResponse(true))

        repo.deleteBpReading(7L)

        verify(api).deleteBpReading(7L)
        verify(dao).deleteBpReading(7L)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryBsReadingsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var api: HealthApiService
    private lateinit var dao: BpDao
    private lateinit var statsDao: StatsCacheDao
    private lateinit var repo: TestableBloodPressureRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        api      = mock()
        dao      = mock()
        statsDao = mock()
        repo     = TestableBloodPressureRepository(api, dao, statsDao)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    // ── getAllBsReadings ───────────────────────────────────────────────────────

    @Test
    fun `getAllBsReadings returns API list on success`() = runTest {
        val readings = listOf(makeBsReading(1L, 90, "Fasting"), makeBsReading(2L, 140, "After Meal"))
        whenever(api.getBsReadings()).thenReturn(ApiResponse(true, readings))

        val result = repo.getAllBsReadings()

        assertEquals(2, result.size)
        assertEquals(90,  result[0].glucose)
        assertEquals(140, result[1].glucose)
        verify(dao).upsertBsReadings(any())
    }

    @Test
    fun `getAllBsReadings falls back to DAO when API throws`() = runTest {
        whenever(api.getBsReadings()).thenThrow(RuntimeException("No network"))
        whenever(dao.getBsReadings()).thenReturn(listOf(makeBsEntity(1L, 105, "Bedtime")))

        val result = repo.getAllBsReadings()

        assertEquals(1, result.size)
        assertEquals(105, result[0].glucose)
    }

    @Test
    fun `getAllBsReadings returns empty list when API returns null data`() = runTest {
        whenever(api.getBsReadings()).thenReturn(ApiResponse(true, null))

        val result = repo.getAllBsReadings()

        assertTrue(result.isEmpty())
    }

    // ── getLatestBsReading ────────────────────────────────────────────────────

    @Test
    fun `getLatestBsReading returns API value on success`() = runTest {
        val reading = makeBsReading(1L, 92, "Fasting")
        whenever(api.getLatestBsReading()).thenReturn(ApiResponse(true, reading))

        val result = repo.getLatestBsReading()

        assertNotNull(result)
        assertEquals(92, result!!.glucose)
    }

    @Test
    fun `getLatestBsReading falls back to first DAO entry on API failure`() = runTest {
        whenever(api.getLatestBsReading()).thenThrow(RuntimeException("Timeout"))
        whenever(dao.getBsReadings()).thenReturn(listOf(makeBsEntity(2L, 110, "Before Meal")))

        val result = repo.getLatestBsReading()

        assertNotNull(result)
        assertEquals(110, result!!.glucose)
    }

    // ── addBsReading ──────────────────────────────────────────────────────────

    @Test
    fun `addBsReading returns saved reading on API success`() = runTest {
        val saved = makeBsReading(8L, 115, "After Meal")
        whenever(api.addBsReading(any())).thenReturn(ApiResponse(true, saved))

        val result = repo.addBsReading(115, "After Meal", "Heavy lunch")

        assertNotNull(result)
        assertEquals(115, result!!.glucose)
        verify(dao).upsertBsReadings(any())
    }

    @Test
    fun `addBsReading returns null on API failure`() = runTest {
        whenever(api.addBsReading(any())).thenThrow(RuntimeException("Unavailable"))

        val result = repo.addBsReading(100, "Fasting")

        assertNull(result)
    }

    @Test
    fun `addBsReading passes correct values to API`() = runTest {
        val saved = makeBsReading(1L, 99, "Bedtime")
        whenever(api.addBsReading(any())).thenReturn(ApiResponse(true, saved))

        repo.addBsReading(99, "Bedtime", "Before sleep")

        verify(api).addBsReading(AddBsReadingRequest(99, "Bedtime", "Before sleep"))
    }

    // ── deleteBsReading ───────────────────────────────────────────────────────

    @Test
    fun `deleteBsReading calls DAO even when API throws`() = runTest {
        whenever(api.deleteBsReading(any())).thenThrow(RuntimeException("Server gone"))

        repo.deleteBsReading(3L)

        verify(dao).deleteBsReading(3L)
    }

    @Test
    fun `deleteBsReading calls both API and DAO on success`() = runTest {
        whenever(api.deleteBsReading(any())).thenReturn(ApiResponse(true))

        repo.deleteBsReading(4L)

        verify(api).deleteBsReading(4L)
        verify(dao).deleteBsReading(4L)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryStatsTest {


    private val testDispatcher = StandardTestDispatcher()
    private val gson = com.google.gson.Gson()
    private lateinit var api: HealthApiService
    private lateinit var dao: BpDao
    private lateinit var statsDao: StatsCacheDao
    private lateinit var repo: TestableBloodPressureRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        api      = mock()
        dao      = mock()
        statsDao = mock()
        repo     = TestableBloodPressureRepository(api, dao, statsDao, gson)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    // ── getBpStats ────────────────────────────────────────────────────────────

    @Test
    fun `getBpStats returns API data and saves to cache`() = runTest {
        val stats = makeBpStats()
        whenever(api.getBpStats(2024, 0)).thenReturn(ApiResponse(true, stats))

        val result = repo.getBpStats(2024, 0)

        assertNotNull(result)
        assertEquals(121, result!!.monthlyAvgSystolic)
        verify(statsDao).save(any())
    }

    @Test
    fun `getBpStats returns cached data when API throws`() = runTest {
        val stats = makeBpStats()
        val cachedJson = gson.toJson(stats)
        val key = "bp_stats_2024_0"
        whenever(api.getBpStats(2024, 0)).thenThrow(RuntimeException("Offline"))
        whenever(statsDao.get(key)).thenReturn(StatsCacheEntity(key, cachedJson))

        val result = repo.getBpStats(2024, 0)

        assertNotNull(result)
        assertEquals(121, result!!.monthlyAvgSystolic)
    }

    @Test
    fun `getBpStats returns null when API throws and cache is empty`() = runTest {
        whenever(api.getBpStats(any(), any())).thenThrow(RuntimeException("Offline"))
        whenever(statsDao.get(any())).thenReturn(null)

        val result = repo.getBpStats(2024, 1)

        assertNull(result)
    }

    @Test
    fun `getBpStats uses correct cache key format`() = runTest {
        whenever(api.getBpStats(2023, 11)).thenThrow(RuntimeException("Offline"))
        whenever(statsDao.get("bp_stats_2023_11")).thenReturn(null)

        repo.getBpStats(2023, 11)

        verify(statsDao).get("bp_stats_2023_11")
    }

    // ── getBsStats ────────────────────────────────────────────────────────────

    @Test
    fun `getBsStats returns API data and saves to cache`() = runTest {
        val stats = makeBsStats()
        whenever(api.getBsStats(2024, 2)).thenReturn(ApiResponse(true, stats))

        val result = repo.getBsStats(2024, 2)

        assertNotNull(result)
        assertEquals(103, result!!.monthlyAvg)
        verify(statsDao).save(any())
    }

    @Test
    fun `getBsStats returns cached data when API throws`() = runTest {
        val stats = makeBsStats()
        val cachedJson = gson.toJson(stats)
        val key = "bs_stats_2024_2"
        whenever(api.getBsStats(2024, 2)).thenThrow(RuntimeException("Offline"))
        whenever(statsDao.get(key)).thenReturn(StatsCacheEntity(key, cachedJson))

        val result = repo.getBsStats(2024, 2)

        assertNotNull(result)
        assertEquals(103, result!!.monthlyAvg)
    }

    @Test
    fun `getBsStats returns null when API throws and cache is empty`() = runTest {
        whenever(api.getBsStats(any(), any())).thenThrow(RuntimeException("Offline"))
        whenever(statsDao.get(any())).thenReturn(null)

        val result = repo.getBsStats(2024, 3)

        assertNull(result)
    }

    @Test
    fun `getBsStats uses correct cache key format`() = runTest {
        whenever(api.getBsStats(2025, 6)).thenThrow(RuntimeException("Offline"))
        whenever(statsDao.get("bs_stats_2025_6")).thenReturn(null)

        repo.getBsStats(2025, 6)

        verify(statsDao).get("bs_stats_2025_6")
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// 5. STATS ACTIVITY COMPANION CONSTANTS TEST
// ══════════════════════════════════════════════════════════════════════════════

class BloodPressureStatsActivityConstantsTest {

    @Test
    fun `EXTRA_TYPE constant value is correct`() {
        assertEquals("extra_type", BloodPressureStatsActivity.EXTRA_TYPE)
    }

    @Test
    fun `TYPE_BLOOD_PRESSURE constant value is correct`() {
        assertEquals("blood_pressure", BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE)
    }

    @Test
    fun `TYPE_BLOOD_SUGAR constant value is correct`() {
        assertEquals("blood_sugar", BloodPressureStatsActivity.TYPE_BLOOD_SUGAR)
    }

    @Test
    fun `type constants are distinct`() {
        assertNotEquals(
            BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE,
            BloodPressureStatsActivity.TYPE_BLOOD_SUGAR
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// 6. BloodPressureHomeActivity — LOGIC TESTS
//
//    The activity itself requires Android framework (Robolectric) to instantiate,
//    so we extract every testable decision into pure functions/models and test
//    those directly — keeping this file 100% JVM unit tests with no Android deps.
// ══════════════════════════════════════════════════════════════════════════════

// ── Extracted logic: suppressNavEvent state machine ───────────────────────────
//
// In BloodPressureHomeActivity, onResume() does:
//   if (bottomNavigation.selectedItemId != R.id.nav_home) {
//       suppressNavEvent = true
//       bottomNavigation.selectedItemId = R.id.nav_home
//       suppressNavEvent = false
//   }
//
// We model this as a pure function so it can be tested without a real View.

data class NavBarState(val selectedItemId: Int, val suppressNavEvent: Boolean)

/** Mirrors the onResume correction logic from BloodPressureHomeActivity. */
fun correctNavBarOnResume(state: NavBarState, navHomeId: Int): NavBarState {
    return if (state.selectedItemId != navHomeId) {
        // suppressNavEvent is toggled on, item corrected, then toggled off
        state.copy(selectedItemId = navHomeId, suppressNavEvent = false)
    } else {
        state // already correct — nothing changes
    }
}

/** Mirrors the bottom-nav selected listener logic. Returns the action to take. */
sealed class NavAction {
    object Suppressed           : NavAction()
    object NavigateToWaterHome  : NavAction()
    object NavigateToBpStats    : NavAction()
    object NoOp                 : NavAction()
}

fun resolveNavAction(itemId: Int, navHomeId: Int, navStatsId: Int, suppress: Boolean): NavAction {
    if (suppress) return NavAction.Suppressed
    return when (itemId) {
        navHomeId   -> NavAction.NavigateToWaterHome
        navStatsId  -> NavAction.NavigateToBpStats
        else        -> NavAction.NoOp
    }
}

/** Mirrors the reselected listener logic. */
fun resolveNavReselectedAction(itemId: Int, navHomeId: Int, suppress: Boolean): NavAction {
    if (suppress) return NavAction.Suppressed
    return if (itemId == navHomeId) NavAction.NavigateToWaterHome else NavAction.NoOp
}

// ── Extracted logic: button → destination + intent extra ─────────────────────

data class ButtonDestination(val activityClass: String, val extraType: String?)

fun resolveButtonDestination(buttonId: String): ButtonDestination = when (buttonId) {
    "btn_add_blood_pressure" -> ButtonDestination(
        activityClass = "BloodPressureAddActivity",
        extraType     = null
    )
    "btn_bp_statistics" -> ButtonDestination(
        activityClass = "BloodPressureStatsActivity",
        extraType     = BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE
    )
    "btn_add_blood_sugar" -> ButtonDestination(
        activityClass = "BloodSugarAddActivity",
        extraType     = null
    )
    "btn_bs_statistics" -> ButtonDestination(
        activityClass = "BloodPressureStatsActivity",
        extraType     = BloodPressureStatsActivity.TYPE_BLOOD_SUGAR
    )
    else -> throw IllegalArgumentException("Unknown button: $buttonId")
}

// ── Fake IDs (stand-ins for R.id values) ─────────────────────────────────────
private const val NAV_HOME  = 1001
private const val NAV_STATS = 1002

// ─────────────────────────────────────────────────────────────────────────────

class HomeActivityNavBarCorrectionTest {

    // ── correctNavBarOnResume ─────────────────────────────────────────────────

    @Test
    fun `onResume does nothing when nav bar already shows home`() {
        val before = NavBarState(selectedItemId = NAV_HOME, suppressNavEvent = false)
        val after  = correctNavBarOnResume(before, NAV_HOME)
        assertEquals(NAV_HOME, after.selectedItemId)
        assertFalse(after.suppressNavEvent)
    }

    @Test
    fun `onResume corrects selected item to home when stats is highlighted`() {
        val before = NavBarState(selectedItemId = NAV_STATS, suppressNavEvent = false)
        val after  = correctNavBarOnResume(before, NAV_HOME)
        assertEquals(NAV_HOME, after.selectedItemId)
    }

    @Test
    fun `onResume leaves suppressNavEvent false after correction`() {
        val before = NavBarState(selectedItemId = NAV_STATS, suppressNavEvent = false)
        val after  = correctNavBarOnResume(before, NAV_HOME)
        assertFalse(after.suppressNavEvent)
    }

    @Test
    fun `onResume does not flip suppressNavEvent when bar is already correct`() {
        val before = NavBarState(selectedItemId = NAV_HOME, suppressNavEvent = false)
        val after  = correctNavBarOnResume(before, NAV_HOME)
        assertFalse(after.suppressNavEvent)
    }
}

class HomeActivityNavSelectedListenerTest {

    // ── resolveNavAction ──────────────────────────────────────────────────────

    @Test
    fun `nav_home selected triggers NavigateToWaterHome`() {
        val action = resolveNavAction(NAV_HOME, NAV_HOME, NAV_STATS, suppress = false)
        assertEquals(NavAction.NavigateToWaterHome, action)
    }

    @Test
    fun `nav_statistics selected triggers NavigateToBpStats`() {
        val action = resolveNavAction(NAV_STATS, NAV_HOME, NAV_STATS, suppress = false)
        assertEquals(NavAction.NavigateToBpStats, action)
    }

    @Test
    fun `suppressed nav_home event returns Suppressed`() {
        val action = resolveNavAction(NAV_HOME, NAV_HOME, NAV_STATS, suppress = true)
        assertEquals(NavAction.Suppressed, action)
    }

    @Test
    fun `suppressed nav_statistics event returns Suppressed`() {
        val action = resolveNavAction(NAV_STATS, NAV_HOME, NAV_STATS, suppress = true)
        assertEquals(NavAction.Suppressed, action)
    }

    @Test
    fun `unknown item id returns NoOp`() {
        val action = resolveNavAction(9999, NAV_HOME, NAV_STATS, suppress = false)
        assertEquals(NavAction.NoOp, action)
    }
}

class HomeActivityNavReselectedListenerTest {

    // ── resolveNavReselectedAction ────────────────────────────────────────────

    @Test
    fun `reselecting nav_home triggers NavigateToWaterHome`() {
        val action = resolveNavReselectedAction(NAV_HOME, NAV_HOME, suppress = false)
        assertEquals(NavAction.NavigateToWaterHome, action)
    }

    @Test
    fun `reselecting nav_stats returns NoOp`() {
        val action = resolveNavReselectedAction(NAV_STATS, NAV_HOME, suppress = false)
        assertEquals(NavAction.NoOp, action)
    }

    @Test
    fun `suppressed reselect of nav_home returns Suppressed`() {
        val action = resolveNavReselectedAction(NAV_HOME, NAV_HOME, suppress = true)
        assertEquals(NavAction.Suppressed, action)
    }

    @Test
    fun `suppressed reselect of nav_stats returns Suppressed`() {
        val action = resolveNavReselectedAction(NAV_STATS, NAV_HOME, suppress = true)
        assertEquals(NavAction.Suppressed, action)
    }
}

class HomeActivityButtonRoutingTest {

    // ── btn_add_blood_pressure ────────────────────────────────────────────────

    @Test
    fun `Add Blood Pressure button routes to BloodPressureAddActivity`() {
        val dest = resolveButtonDestination("btn_add_blood_pressure")
        assertEquals("BloodPressureAddActivity", dest.activityClass)
    }

    @Test
    fun `Add Blood Pressure button carries no EXTRA_TYPE`() {
        val dest = resolveButtonDestination("btn_add_blood_pressure")
        assertNull(dest.extraType)
    }

    // ── btn_bp_statistics ─────────────────────────────────────────────────────

    @Test
    fun `BP Statistics button routes to BloodPressureStatsActivity`() {
        val dest = resolveButtonDestination("btn_bp_statistics")
        assertEquals("BloodPressureStatsActivity", dest.activityClass)
    }

    @Test
    fun `BP Statistics button passes TYPE_BLOOD_PRESSURE extra`() {
        val dest = resolveButtonDestination("btn_bp_statistics")
        assertEquals(BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE, dest.extraType)
    }

    // ── btn_add_blood_sugar ───────────────────────────────────────────────────

    @Test
    fun `Add Blood Sugar button routes to BloodSugarAddActivity`() {
        val dest = resolveButtonDestination("btn_add_blood_sugar")
        assertEquals("BloodSugarAddActivity", dest.activityClass)
    }

    @Test
    fun `Add Blood Sugar button carries no EXTRA_TYPE`() {
        val dest = resolveButtonDestination("btn_add_blood_sugar")
        assertNull(dest.extraType)
    }

    // ── btn_bs_statistics ─────────────────────────────────────────────────────

    @Test
    fun `BS Statistics button routes to BloodPressureStatsActivity`() {
        val dest = resolveButtonDestination("btn_bs_statistics")
        assertEquals("BloodPressureStatsActivity", dest.activityClass)
    }

    @Test
    fun `BS Statistics button passes TYPE_BLOOD_SUGAR extra`() {
        val dest = resolveButtonDestination("btn_bs_statistics")
        assertEquals(BloodPressureStatsActivity.TYPE_BLOOD_SUGAR, dest.extraType)
    }

    // ── BP and BS stats buttons both go to same activity with different extras ─

    @Test
    fun `BP and BS stats buttons target same activity class`() {
        val bp = resolveButtonDestination("btn_bp_statistics")
        val bs = resolveButtonDestination("btn_bs_statistics")
        assertEquals(bp.activityClass, bs.activityClass)
    }

    @Test
    fun `BP and BS stats buttons carry different EXTRA_TYPE values`() {
        val bp = resolveButtonDestination("btn_bp_statistics")
        val bs = resolveButtonDestination("btn_bs_statistics")
        assertNotEquals(bp.extraType, bs.extraType)
    }
}
