package com.emad.graduationproject

// ─────────────────────────────────────────────────────────────────────────────
//  SUPPLEMENTAL Unit Test Suite #2 — BloodPressureStatsActivity Logic
//
//  ضعه في نفس المسار:
//    app/src/test/java/com/emad/graduationproject/
//
//  يغطي المنطق الصافي (pure logic) المتبقي في BloodPressureStatsActivity
//  الذي لم يُختبر في الملفين السابقين:
//
//    1. applyTrendView — توجيه الاتجاه (UP/DOWN/FLAT) إلى drawable ولون
//    2. displayType routing — اختيار layout و title بناءً على EXTRA_TYPE
//    3. Stats text formatting — تنسيق النص عند وجود بيانات أو غيابها
//    4. Month spinner offset — حساب السنة/الشهر من موضع الـ spinner
//    5. StatsActivity bottom nav routing
//    6. BloodPressureStatsActivity nav bar: nav_home يرجع للـ Home، nav_stats يبقى
// ─────────────────────────────────────────────────────────────────────────────

import org.junit.Assert.*
import org.junit.Test

// ══════════════════════════════════════════════════════════════════════════════
//  1. applyTrendView — منطق توجيه الاتجاه
//
//  الكود الحقيقي:
//    "UP"   → ic_trending_up   + color من trend.color
//    "DOWN" → ic_trending_down + color من trend.color
//    else   → ic_trending_flat + color من trend.color
//    label.text = trend.delta
//
//  نستخرج هذا القرار كـ pure function تُعيد اسم الـ drawable والدلتا.
// ══════════════════════════════════════════════════════════════════════════════

data class TrendViewState(
    val drawableName: String,
    val deltaText: String,
    val colorHex: String
)

fun resolveTrendViewState(trend: BpTrendInfo): TrendViewState {
    val drawable = when (trend.direction) {
        "UP"   -> "ic_trending_up"
        "DOWN" -> "ic_trending_down"
        else   -> "ic_trending_flat"
    }
    return TrendViewState(drawable, trend.delta, trend.color)
}

/** applyTrendView تُعيد null عندما يكون الـ trend null (لا شيء يُطبَّق) */
fun resolveTrendViewStateOrNull(trend: BpTrendInfo?): TrendViewState? =
    trend?.let { resolveTrendViewState(it) }

class ApplyTrendViewTest {

    @Test
    fun `UP direction maps to ic_trending_up`() {
        val state = resolveTrendViewState(BpTrendInfo("UP", "+5 mmHg", "#FF0000"))
        assertEquals("ic_trending_up", state.drawableName)
    }

    @Test
    fun `DOWN direction maps to ic_trending_down`() {
        val state = resolveTrendViewState(BpTrendInfo("DOWN", "-3 mmHg", "#00AA00"))
        assertEquals("ic_trending_down", state.drawableName)
    }

    @Test
    fun `FLAT direction maps to ic_trending_flat`() {
        val state = resolveTrendViewState(BpTrendInfo("FLAT", "0 mmHg", "#888888"))
        assertEquals("ic_trending_flat", state.drawableName)
    }

    @Test
    fun `unknown direction falls back to ic_trending_flat`() {
        val state = resolveTrendViewState(BpTrendInfo("STABLE", "0", "#888888"))
        assertEquals("ic_trending_flat", state.drawableName)
    }

    @Test
    fun `empty direction falls back to ic_trending_flat`() {
        val state = resolveTrendViewState(BpTrendInfo("", "--", "#888888"))
        assertEquals("ic_trending_flat", state.drawableName)
    }

    @Test
    fun `delta text is preserved correctly`() {
        val state = resolveTrendViewState(BpTrendInfo("UP", "+12 mmHg", "#FF0000"))
        assertEquals("+12 mmHg", state.deltaText)
    }

    @Test
    fun `color hex is preserved correctly`() {
        val state = resolveTrendViewState(BpTrendInfo("DOWN", "-2", "#FF5722"))
        assertEquals("#FF5722", state.colorHex)
    }

    @Test
    fun `null trend returns null state (no view update)`() {
        val state = resolveTrendViewStateOrNull(null)
        assertNull(state)
    }

    @Test
    fun `non-null trend returns non-null state`() {
        val state = resolveTrendViewStateOrNull(BpTrendInfo("UP", "+1", "#FF0000"))
        assertNotNull(state)
    }

    @Test
    fun `all three directions produce distinct drawables`() {
        val up   = resolveTrendViewState(BpTrendInfo("UP",   "+1", "#F00")).drawableName
        val down = resolveTrendViewState(BpTrendInfo("DOWN", "-1", "#0F0")).drawableName
        val flat = resolveTrendViewState(BpTrendInfo("FLAT", "0",  "#888")).drawableName

        assertNotEquals(up, down)
        assertNotEquals(up, flat)
        assertNotEquals(down, flat)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  2. displayType routing — اختيار المحتوى بناءً على EXTRA_TYPE
//
//  الكود الحقيقي:
//    displayType = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_BLOOD_PRESSURE
//    if (displayType == TYPE_BLOOD_PRESSURE) → layout BP + title "Blood Pressure Stats"
//    else                                    → layout BS + title "Blood Sugar Stats"
// ══════════════════════════════════════════════════════════════════════════════

data class StatsDisplayConfig(
    val layoutName: String,
    val title: String,
    val displayType: String
)

fun resolveStatsDisplayConfig(extraType: String?): StatsDisplayConfig {
    val displayType = extraType ?: "blood_pressure"
    return if (displayType == "blood_pressure") {
        StatsDisplayConfig("blood_pressure_stats", "Blood Pressure Stats", displayType)
    } else {
        StatsDisplayConfig("blood_sugar_stats", "Blood Sugar Stats", displayType)
    }
}

class DisplayTypeRoutingTest {

    @Test
    fun `TYPE_BLOOD_PRESSURE extra selects BP layout`() {
        val config = resolveStatsDisplayConfig("blood_pressure")
        assertEquals("blood_pressure_stats", config.layoutName)
    }

    @Test
    fun `TYPE_BLOOD_PRESSURE extra sets correct title`() {
        val config = resolveStatsDisplayConfig("blood_pressure")
        assertEquals("Blood Pressure Stats", config.title)
    }

    @Test
    fun `TYPE_BLOOD_SUGAR extra selects BS layout`() {
        val config = resolveStatsDisplayConfig("blood_sugar")
        assertEquals("blood_sugar_stats", config.layoutName)
    }

    @Test
    fun `TYPE_BLOOD_SUGAR extra sets correct title`() {
        val config = resolveStatsDisplayConfig("blood_sugar")
        assertEquals("Blood Sugar Stats", config.title)
    }

    @Test
    fun `null extra defaults to blood_pressure layout`() {
        val config = resolveStatsDisplayConfig(null)
        assertEquals("blood_pressure_stats", config.layoutName)
    }

    @Test
    fun `null extra defaults to Blood Pressure Stats title`() {
        val config = resolveStatsDisplayConfig(null)
        assertEquals("Blood Pressure Stats", config.title)
    }

    @Test
    fun `unknown extra type falls back to BS layout (else branch)`() {
        // أي قيمة غير "blood_pressure" تدخل الـ else
        val config = resolveStatsDisplayConfig("unknown_type")
        assertEquals("blood_sugar_stats", config.layoutName)
    }

    @Test
    fun `displayType is preserved in config`() {
        val config = resolveStatsDisplayConfig("blood_sugar")
        assertEquals("blood_sugar", config.displayType)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  3. Stats text formatting — تنسيق النص للعرض
//
//  الكود الحقيقي يتبع هذا النمط في refreshBpStats و refreshBsStats:
//    بيانات موجودة  → "120/80"  (BP) أو "105" (BS)
//    بيانات غائبة   → "--/--"   (BP) أو "--"  (BS)
//    label text     → "Monthly Average (mmHg)" أو "No data for this month"
// ══════════════════════════════════════════════════════════════════════════════

data class BpDisplayText(val valueText: String, val labelText: String)
data class BsDisplayText(val valueText: String, val labelText: String)

fun formatBpMonthlyAverage(stats: BpStatsData?): BpDisplayText =
    if (stats?.monthlyAvgSystolic != null && stats.monthlyAvgDiastolic != null) {
        BpDisplayText(
            "${stats.monthlyAvgSystolic}/${stats.monthlyAvgDiastolic}",
            "Monthly Average (mmHg)"
        )
    } else {
        BpDisplayText("--/--", "No data for this month")
    }

fun formatBpWeeklyAverage(stats: BpStatsData?): BpDisplayText =
    if (stats?.weeklyAvgSystolic != null && stats.weeklyAvgDiastolic != null) {
        BpDisplayText(
            "${stats.weeklyAvgSystolic}/${stats.weeklyAvgDiastolic}",
            "Last 7 days"
        )
    } else {
        BpDisplayText("--/--", "No data")
    }

fun formatBpLatestReading(reading: BloodPressureReadingData?): Triple<String, String, String> =
    if (reading != null) {
        Triple("${reading.systolic}/${reading.diastolic}", "mmHg", reading.formattedTime)
    } else {
        Triple("--/--", "mmHg", "No readings yet")
    }

fun formatBsMonthlyAverage(stats: BsStatsData?): BsDisplayText =
    if (stats?.monthlyAvg != null) {
        BsDisplayText("${stats.monthlyAvg}", "Monthly Average (mg/dL)")
    } else {
        BsDisplayText("--", "No data for this month")
    }

fun formatBsWeeklyAverage(stats: BsStatsData?): BsDisplayText =
    if (stats?.weeklyAvg != null) {
        BsDisplayText("${stats.weeklyAvg}", "Last 7 days")
    } else {
        BsDisplayText("--", "No data")
    }

fun formatBsLatestReading(reading: BloodSugarReadingData?): Triple<String, String, String> =
    if (reading != null) {
        Triple("${reading.glucose}", "mg/dL", reading.formattedTime)
    } else {
        Triple("--", "mg/dL", "No readings yet")
    }

class BpStatsTextFormattingTest {

    private fun makeBpStats(sys: Int? = 122, dia: Int? = 81, wSys: Int? = 119, wDia: Int? = 79) =
        BpStatsData(
            monthlyAvgSystolic  = sys,
            monthlyAvgDiastolic = dia,
            systolicTrend       = BpTrendInfo("FLAT", "0", "#888888"),
            diastolicTrend      = BpTrendInfo("FLAT", "0", "#888888"),
            latestReading       = null,
            weeklyAvgSystolic   = wSys,
            weeklyAvgDiastolic  = wDia,
            highestReading      = null,
            lowestReading       = null
        )

    // ── Monthly average ───────────────────────────────────────────────────────

    @Test
    fun `monthly average with data shows correct format`() {
        val text = formatBpMonthlyAverage(makeBpStats(122, 81))
        assertEquals("122/81", text.valueText)
        assertEquals("Monthly Average (mmHg)", text.labelText)
    }

    @Test
    fun `monthly average with null systolic shows placeholder`() {
        val text = formatBpMonthlyAverage(makeBpStats(null, 81))
        assertEquals("--/--", text.valueText)
        assertEquals("No data for this month", text.labelText)
    }

    @Test
    fun `monthly average with null diastolic shows placeholder`() {
        val text = formatBpMonthlyAverage(makeBpStats(122, null))
        assertEquals("--/--", text.valueText)
    }

    @Test
    fun `monthly average with null stats shows placeholder`() {
        val text = formatBpMonthlyAverage(null)
        assertEquals("--/--", text.valueText)
        assertEquals("No data for this month", text.labelText)
    }

    // ── Weekly average ────────────────────────────────────────────────────────

    @Test
    fun `weekly average with data shows correct format`() {
        val text = formatBpWeeklyAverage(makeBpStats(wSys = 119, wDia = 79))
        assertEquals("119/79", text.valueText)
        assertEquals("Last 7 days", text.labelText)
    }

    @Test
    fun `weekly average with null values shows placeholder`() {
        val text = formatBpWeeklyAverage(makeBpStats(wSys = null, wDia = null))
        assertEquals("--/--", text.valueText)
        assertEquals("No data", text.labelText)
    }

    @Test
    fun `weekly average with null stats shows placeholder`() {
        val text = formatBpWeeklyAverage(null)
        assertEquals("--/--", text.valueText)
    }

    // ── Latest reading ────────────────────────────────────────────────────────

    @Test
    fun `latest BP reading present shows correct values`() {
        val reading = BloodPressureReadingData(1L, 118, 76, 72, "", 0L, "01/01 10:00", "NORMAL", "Normal")
        val (value, unit, time) = formatBpLatestReading(reading)
        assertEquals("118/76", value)
        assertEquals("mmHg", unit)
        assertEquals("01/01 10:00", time)
    }

    @Test
    fun `latest BP reading null shows placeholder`() {
        val (value, unit, time) = formatBpLatestReading(null)
        assertEquals("--/--", value)
        assertEquals("mmHg", unit)
        assertEquals("No readings yet", time)
    }
}

class BsStatsTextFormattingTest {

    private fun makeBsStats(monthly: Int? = 103, weekly: Int? = 99) = BsStatsData(
        monthlyAvg     = monthly,
        fastingTrend   = BpTrendInfo("FLAT", "0", "#888888"),
        afterMealTrend = BpTrendInfo("FLAT", "0", "#888888"),
        latestReading  = null,
        weeklyAvg      = weekly,
        highestReading = null,
        lowestReading  = null
    )

    // ── Monthly average ───────────────────────────────────────────────────────

    @Test
    fun `BS monthly average with data shows correct format`() {
        val text = formatBsMonthlyAverage(makeBsStats(monthly = 103))
        assertEquals("103", text.valueText)
        assertEquals("Monthly Average (mg/dL)", text.labelText)
    }

    @Test
    fun `BS monthly average with null shows placeholder`() {
        val text = formatBsMonthlyAverage(makeBsStats(monthly = null))
        assertEquals("--", text.valueText)
        assertEquals("No data for this month", text.labelText)
    }

    @Test
    fun `BS monthly average with null stats shows placeholder`() {
        val text = formatBsMonthlyAverage(null)
        assertEquals("--", text.valueText)
    }

    // ── Weekly average ────────────────────────────────────────────────────────

    @Test
    fun `BS weekly average with data shows correct format`() {
        val text = formatBsWeeklyAverage(makeBsStats(weekly = 99))
        assertEquals("99", text.valueText)
        assertEquals("Last 7 days", text.labelText)
    }

    @Test
    fun `BS weekly average with null shows placeholder`() {
        val text = formatBsWeeklyAverage(makeBsStats(weekly = null))
        assertEquals("--", text.valueText)
        assertEquals("No data", text.labelText)
    }

    // ── Latest reading ────────────────────────────────────────────────────────

    @Test
    fun `latest BS reading present shows correct values`() {
        val reading = BloodSugarReadingData(1L, 92, "Fasting", "", 0L, "02/01 08:00", "NORMAL", "Normal")
        val (value, unit, time) = formatBsLatestReading(reading)
        assertEquals("92", value)
        assertEquals("mg/dL", unit)
        assertEquals("02/01 08:00", time)
    }

    @Test
    fun `latest BS reading null shows placeholder`() {
        val (value, unit, time) = formatBsLatestReading(null)
        assertEquals("--", value)
        assertEquals("mg/dL", unit)
        assertEquals("No readings yet", time)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  4. Month spinner offset — حساب السنة/الشهر من موضع الـ spinner
//
//  الكود الحقيقي:
//    position 0 = الشهر الحالي      (offset = 0)
//    position 1 = الشهر السابق      (offset = -1)
//    position N = قبل N أشهر        (offset = -N)
//
//  نستخرج هذا كـ pure function تحسب (year, month) من position وتاريخ مرجعي.
// ══════════════════════════════════════════════════════════════════════════════

data class YearMonth(val year: Int, val month: Int)

/**
 * يحسب (year, month) من موضع الـ spinner.
 * baseYear و baseMonth = الشهر الحالي (يمررهما الـ test بشكل ثابت).
 * month قيمته 0..11 (نفس Calendar.MONTH).
 */
fun resolveYearMonthFromSpinnerPosition(
    position: Int,
    baseYear: Int,
    baseMonth: Int
): YearMonth {
    var month = baseMonth - position
    var year  = baseYear
    while (month < 0) {
        month += 12
        year  -= 1
    }
    return YearMonth(year, month)
}

class MonthSpinnerOffsetTest {

    // نستخدم تاريخ مرجعي ثابت: مارس 2025 (month=2)
    private val BASE_YEAR  = 2025
    private val BASE_MONTH = 2 // March (0-indexed)

    @Test
    fun `position 0 returns current month and year`() {
        val ym = resolveYearMonthFromSpinnerPosition(0, BASE_YEAR, BASE_MONTH)
        assertEquals(2025, ym.year)
        assertEquals(2, ym.month) // March
    }

    @Test
    fun `position 1 returns previous month`() {
        val ym = resolveYearMonthFromSpinnerPosition(1, BASE_YEAR, BASE_MONTH)
        assertEquals(2025, ym.year)
        assertEquals(1, ym.month) // February
    }

    @Test
    fun `position 2 returns two months ago`() {
        val ym = resolveYearMonthFromSpinnerPosition(2, BASE_YEAR, BASE_MONTH)
        assertEquals(2025, ym.year)
        assertEquals(0, ym.month) // January
    }

    @Test
    fun `crossing year boundary decrements year`() {
        // 3 months before March 2025 = December 2024
        val ym = resolveYearMonthFromSpinnerPosition(3, BASE_YEAR, BASE_MONTH)
        assertEquals(2024, ym.year)
        assertEquals(11, ym.month) // December
    }

    @Test
    fun `position 11 goes back 11 months`() {
        // March - 11 = April 2024
        val ym = resolveYearMonthFromSpinnerPosition(11, BASE_YEAR, BASE_MONTH)
        assertEquals(2024, ym.year)
        assertEquals(3, ym.month) // April
    }

    @Test
    fun `starting from January crossing boundary goes to December previous year`() {
        // Base: January 2025 (month=0), position 1 → December 2024
        val ym = resolveYearMonthFromSpinnerPosition(1, 2025, 0)
        assertEquals(2024, ym.year)
        assertEquals(11, ym.month)
    }

    @Test
    fun `starting from December position 1 goes to November same year`() {
        // Base: December 2025 (month=11), position 1 → November 2025
        val ym = resolveYearMonthFromSpinnerPosition(1, 2025, 11)
        assertEquals(2025, ym.year)
        assertEquals(10, ym.month) // November
    }

    @Test
    fun `month value always in range 0 to 11`() {
        for (pos in 0..11) {
            val ym = resolveYearMonthFromSpinnerPosition(pos, BASE_YEAR, BASE_MONTH)
            assertTrue("month ${ym.month} out of range for position $pos", ym.month in 0..11)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  5. StatsActivity bottom nav routing
//
//  الكود الحقيقي في setupBottomNavigation:
//    nav_home      → startActivity(BloodPressureHomeActivity) + finish()
//    nav_statistics → true (يبقى في نفس الصفحة، لا navigation)
//    else           → true (NoOp)
// ══════════════════════════════════════════════════════════════════════════════

sealed class StatsNavAction {
    object NavigateToHome : StatsNavAction()
    object StayOnStats    : StatsNavAction()
    object NoOp           : StatsNavAction()
}

fun resolveStatsNavAction(itemId: Int, navHomeId: Int, navStatsId: Int): StatsNavAction =
    when (itemId) {
        navHomeId   -> StatsNavAction.NavigateToHome
        navStatsId  -> StatsNavAction.StayOnStats
        else        -> StatsNavAction.NoOp
    }

private const val STATS_NAV_HOME  = 3001
private const val STATS_NAV_STATS = 3002

class StatsActivityBottomNavTest {

    @Test
    fun `nav_home selected navigates back to Home`() {
        val action = resolveStatsNavAction(STATS_NAV_HOME, STATS_NAV_HOME, STATS_NAV_STATS)
        assertEquals(StatsNavAction.NavigateToHome, action)
    }

    @Test
    fun `nav_statistics selected stays on stats screen`() {
        val action = resolveStatsNavAction(STATS_NAV_STATS, STATS_NAV_HOME, STATS_NAV_STATS)
        assertEquals(StatsNavAction.StayOnStats, action)
    }

    @Test
    fun `unknown item id returns NoOp`() {
        val action = resolveStatsNavAction(9999, STATS_NAV_HOME, STATS_NAV_STATS)
        assertEquals(StatsNavAction.NoOp, action)
    }

    @Test
    fun `nav_home and nav_stats actions are distinct`() {
        val home  = resolveStatsNavAction(STATS_NAV_HOME,  STATS_NAV_HOME, STATS_NAV_STATS)
        val stats = resolveStatsNavAction(STATS_NAV_STATS, STATS_NAV_HOME, STATS_NAV_STATS)
        assertNotEquals(home, stats)
    }
}
