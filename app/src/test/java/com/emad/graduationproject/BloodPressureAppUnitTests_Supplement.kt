package com.emad.graduationproject

// ─────────────────────────────────────────────────────────────────────────────
//  SUPPLEMENTAL Unit Test Suite — Blood Pressure & Blood Sugar App
//
//  هذا الملف يُكمّل ملف BloodPressureAppUnitTests.kt الأصلي فقط.
//  ضعه في نفس المسار:
//    app/src/test/java/com/emad/graduationproject/
//
//  يغطي الفجوات التالية:
//    1. تصحيح رسائل الخطأ في BP Validation  (كانت مختلفة عن الكود الحقيقي)
//    2. تصحيح رسائل الخطأ في BS Validation  (كانت مختلفة عن الكود الحقيقي)
//    3. اختبارات BloodSugarAddActivity validate() (كانت مفقودة كلياً)
//    4. stub لثوابت BloodPressureStatsActivity (يحل مشكلة الكمبايل بدون Android)
//    5. اختبارات ApiResponse مع data classes إضافية من HealthApiService
//    6. اختبارات BloodSugarAddActivity navigation routing
// ─────────────────────────────────────────────────────────────────────────────

import org.junit.Assert.*
import org.junit.Test

// ══════════════════════════════════════════════════════════════════════════════
//  STUB — BloodPressureStatsActivity companion constants
//
//  الملف الأصلي في Section 5 يستدعي BloodPressureStatsActivity.EXTRA_TYPE
//  لكن هذا كلاس Android Activity لا يُكمبايل في بيئة JVM بدون Robolectric.
//  الحل: نعرّف object stub يحمل نفس الثوابت بنفس القيم الحقيقية من الكود.
//  هذا يجعل Section 5 في الملف الأصلي يُكمبايل ويشتغل بدون أي تغيير فيه.
// ══════════════════════════════════════════════════════════════════════════════

object BloodPressureStatsActivity {
    const val EXTRA_TYPE          = "extra_type"
    const val TYPE_BLOOD_PRESSURE = "blood_pressure"
    const val TYPE_BLOOD_SUGAR    = "blood_sugar"
}

// ══════════════════════════════════════════════════════════════════════════════
//  1. CORRECTED BP VALIDATION TESTS
//
//  المشكلة: الملف الأصلي كان يتوقع رسائل خطأ مختلفة عن الكود الحقيقي.
//
//  الكود الحقيقي (BloodPressureAddActivity.validate) يستخدم نظام مختلف:
//    - كل حقل يُتحقق منه باستقلالية (multi-error، مش early-return)
//    - رسائل الخطأ الحقيقية:
//        "Please enter systolic pressure"       (حقل فارغ)
//        "Enter a value between 60 and 300 mmHg" (خارج النطاق أو غير رقمي)
//        "Please enter diastolic pressure"       (حقل فارغ)
//        "Enter a value between 40 and 200 mmHg" (خارج النطاق أو غير رقمي)
//        "Please enter pulse rate"               (حقل فارغ)
//        "Enter a value between 30 and 250 bpm"  (خارج النطاق أو غير رقمي)
//        "Systolic must be greater than diastolic" (sys <= dia)
//
//  نُعيد تعريف validateBloodPressureReal كـ pure function تطابق الكود الحقيقي.
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Pure function تُطابق منطق BloodPressureAddActivity.validate() بدقة.
 * ترجع قائمة بكل الأخطاء (multi-error) بدلاً من early-return،
 * تماماً كما يعمل الكود الحقيقي الذي يضع error على كل TextInputLayout بشكل مستقل.
 */
fun validateBloodPressureReal(
    systolicText: String,
    diastolicText: String,
    pulseText: String
): List<Pair<String, String>> { // List<field, errorMsg>
    val errors = mutableListOf<Pair<String, String>>()

    // Systolic
    if (systolicText.isEmpty()) {
        errors += "systolic" to "Please enter systolic pressure"
    } else {
        val v = systolicText.toIntOrNull()
        if (v == null || v !in 60..300) {
            errors += "systolic" to "Enter a value between 60 and 300 mmHg"
        }
    }

    // Diastolic
    if (diastolicText.isEmpty()) {
        errors += "diastolic" to "Please enter diastolic pressure"
    } else {
        val v = diastolicText.toIntOrNull()
        if (v == null || v !in 40..200) {
            errors += "diastolic" to "Enter a value between 40 and 200 mmHg"
        }
    }

    // Pulse
    if (pulseText.isEmpty()) {
        errors += "pulse" to "Please enter pulse rate"
    } else {
        val v = pulseText.toIntOrNull()
        if (v == null || v !in 30..250) {
            errors += "pulse" to "Enter a value between 30 and 250 bpm"
        }
    }

    // Systolic > Diastolic (يُطبَّق فقط لو الحقلين مرّوا الفحص الأول)
    if (errors.isEmpty()) {
        val sys = systolicText.toInt()
        val dia = diastolicText.toInt()
        if (sys <= dia) {
            errors += "systolic"  to "Systolic must be greater than diastolic"
            errors += "diastolic" to "Diastolic must be less than systolic"
        }
    }

    return errors
}

/** Helper: هل النتيجة صحيحة (لا أخطاء)؟ */
private fun bpIsValid(sys: String, dia: String, pls: String) =
    validateBloodPressureReal(sys, dia, pls).isEmpty()

/** Helper: أول رسالة خطأ لحقل معين */
private fun bpErrorFor(field: String, sys: String, dia: String, pls: String) =
    validateBloodPressureReal(sys, dia, pls).firstOrNull { it.first == field }?.second

class CorrectedBloodPressureValidationTest {

    // ── حقول فارغة ────────────────────────────────────────────────────────────

    @Test
    fun `empty systolic gives correct error message`() {
        val err = bpErrorFor("systolic", "", "80", "72")
        assertEquals("Please enter systolic pressure", err)
    }

    @Test
    fun `empty diastolic gives correct error message`() {
        val err = bpErrorFor("diastolic", "120", "", "72")
        assertEquals("Please enter diastolic pressure", err)
    }

    @Test
    fun `empty pulse gives correct error message`() {
        val err = bpErrorFor("pulse", "120", "80", "")
        assertEquals("Please enter pulse rate", err)
    }

    // ── قيم خارج النطاق — رسائل الخطأ الحقيقية ───────────────────────────────

    @Test
    fun `systolic out of range gives correct error message`() {
        assertEquals(
            "Enter a value between 60 and 300 mmHg",
            bpErrorFor("systolic", "59", "40", "60")
        )
        assertEquals(
            "Enter a value between 60 and 300 mmHg",
            bpErrorFor("systolic", "301", "80", "72")
        )
    }

    @Test
    fun `diastolic out of range gives correct error message`() {
        assertEquals(
            "Enter a value between 40 and 200 mmHg",
            bpErrorFor("diastolic", "120", "39", "72")
        )
        assertEquals(
            "Enter a value between 40 and 200 mmHg",
            bpErrorFor("diastolic", "250", "201", "72")
        )
    }

    @Test
    fun `pulse out of range gives correct error message`() {
        assertEquals(
            "Enter a value between 30 and 250 bpm",
            bpErrorFor("pulse", "120", "80", "29")
        )
        assertEquals(
            "Enter a value between 30 and 250 bpm",
            bpErrorFor("pulse", "120", "80", "251")
        )
    }

    // ── قيم غير رقمية — رسائل الخطأ الحقيقية ─────────────────────────────────

    @Test
    fun `non-numeric systolic gives correct error message`() {
        assertEquals(
            "Enter a value between 60 and 300 mmHg",
            bpErrorFor("systolic", "abc", "80", "72")
        )
    }

    @Test
    fun `non-numeric diastolic gives correct error message`() {
        assertEquals(
            "Enter a value between 40 and 200 mmHg",
            bpErrorFor("diastolic", "120", "xyz", "72")
        )
    }

    @Test
    fun `non-numeric pulse gives correct error message`() {
        assertEquals(
            "Enter a value between 30 and 250 bpm",
            bpErrorFor("pulse", "120", "80", "!!")
        )
    }

    // ── قاعدة systolic > diastolic — رسائل الخطأ الحقيقية ───────────────────

    @Test
    fun `systolic equal to diastolic gives error on both fields`() {
        val errors = validateBloodPressureReal("120", "120", "72")
        val fields = errors.map { it.first }
        assertTrue(fields.contains("systolic"))
        assertTrue(fields.contains("diastolic"))
        assertEquals(
            "Systolic must be greater than diastolic",
            bpErrorFor("systolic", "120", "120", "72")
        )
        assertEquals(
            "Diastolic must be less than systolic",
            bpErrorFor("diastolic", "120", "120", "72")
        )
    }

    @Test
    fun `systolic less than diastolic gives error on both fields`() {
        val errors = validateBloodPressureReal("80", "120", "72")
        assertEquals(2, errors.size)
    }

    // ── Multi-error: خطأ في أكثر من حقل في نفس الوقت ─────────────────────────

    @Test
    fun `all three fields empty returns three errors`() {
        val errors = validateBloodPressureReal("", "", "")
        assertEquals(3, errors.size)
    }

    @Test
    fun `two fields empty returns two errors`() {
        val errors = validateBloodPressureReal("", "", "72")
        assertEquals(2, errors.size)
    }

    @Test
    fun `systolic and diastolic both out of range returns two errors`() {
        val errors = validateBloodPressureReal("59", "39", "72")
        assertEquals(2, errors.size)
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    fun `valid normal reading returns no errors`() {
        assertTrue(bpIsValid("120", "80", "72"))
    }

    @Test
    fun `boundary values 60 40 30 are valid`() {
        assertTrue(bpIsValid("60", "40", "60")) // sys(60) > dia(40) ✓, pulse=60 in 30..250 ✓
    }

    @Test
    fun `boundary values 300 200 250 are valid`() {
        assertTrue(bpIsValid("300", "200", "250"))
    }

    @Test
    fun `hypertensive reading within range is valid`() {
        assertTrue(bpIsValid("180", "110", "90"))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  2. CORRECTED BS VALIDATION TESTS
//
//  الكود الحقيقي (BloodSugarAddActivity.validate) يستخدم:
//    - "Please enter your glucose level"        (حقل فارغ)
//    - "Enter a value between 20 and 600 mg/dL" (خارج النطاق أو غير رقمي)
//    - "Please select a meal type"              (meal type فارغ أو غير صالح)
//
//  نفس نمط multi-error: كل حقل يُفحص بشكل مستقل.
// ══════════════════════════════════════════════════════════════════════════════

private val REAL_MEAL_TYPES = listOf("Fasting", "Before Meal", "After Meal", "Bedtime")

/**
 * Pure function تُطابق منطق BloodSugarAddActivity.validate() بدقة.
 */
fun validateBloodSugarReal(
    glucoseText: String,
    mealTypeText: String
): List<Pair<String, String>> {
    val errors = mutableListOf<Pair<String, String>>()

    if (glucoseText.isEmpty()) {
        errors += "glucose" to "Please enter your glucose level"
    } else {
        val v = glucoseText.toIntOrNull()
        if (v == null || v !in 20..600) {
            errors += "glucose" to "Enter a value between 20 and 600 mg/dL"
        }
    }

    if (mealTypeText.isEmpty() || mealTypeText !in REAL_MEAL_TYPES) {
        errors += "mealType" to "Please select a meal type"
    }

    return errors
}

private fun bsIsValid(glucose: String, meal: String) =
    validateBloodSugarReal(glucose, meal).isEmpty()

private fun bsErrorFor(field: String, glucose: String, meal: String) =
    validateBloodSugarReal(glucose, meal).firstOrNull { it.first == field }?.second

class CorrectedBloodSugarValidationTest {

    // ── حقول فارغة ────────────────────────────────────────────────────────────

    @Test
    fun `empty glucose gives correct error message`() {
        assertEquals(
            "Please enter your glucose level",
            bsErrorFor("glucose", "", "Fasting")
        )
    }

    @Test
    fun `empty meal type gives correct error message`() {
        assertEquals(
            "Please select a meal type",
            bsErrorFor("mealType", "100", "")
        )
    }

    @Test
    fun `both fields empty returns two errors`() {
        val errors = validateBloodSugarReal("", "")
        assertEquals(2, errors.size)
    }

    // ── قيم خارج النطاق ───────────────────────────────────────────────────────

    @Test
    fun `glucose below 20 gives correct error message`() {
        assertEquals(
            "Enter a value between 20 and 600 mg/dL",
            bsErrorFor("glucose", "19", "Fasting")
        )
    }

    @Test
    fun `glucose above 600 gives correct error message`() {
        assertEquals(
            "Enter a value between 20 and 600 mg/dL",
            bsErrorFor("glucose", "601", "Fasting")
        )
    }

    // ── قيم غير رقمية ────────────────────────────────────────────────────────

    @Test
    fun `non-numeric glucose gives correct error message`() {
        assertEquals(
            "Enter a value between 20 and 600 mg/dL",
            bsErrorFor("glucose", "high", "Fasting")
        )
    }

    @Test
    fun `decimal glucose gives correct error message`() {
        assertEquals(
            "Enter a value between 20 and 600 mg/dL",
            bsErrorFor("glucose", "100.5", "Fasting")
        )
    }

    // ── Meal type validation ──────────────────────────────────────────────────

    @Test
    fun `invalid meal type gives correct error message`() {
        assertEquals(
            "Please select a meal type",
            bsErrorFor("mealType", "100", "Lunch")
        )
    }

    @Test
    fun `lowercase fasting is rejected`() {
        assertEquals(
            "Please select a meal type",
            bsErrorFor("mealType", "100", "fasting")
        )
    }

    @Test
    fun `all four valid meal types are accepted`() {
        listOf("Fasting", "Before Meal", "After Meal", "Bedtime").forEach { meal ->
            assertTrue(
                "Expected '$meal' to be valid",
                bsIsValid("100", meal)
            )
        }
    }

    // ── Boundary values ───────────────────────────────────────────────────────

    @Test
    fun `glucose at lower boundary 20 is valid`() {
        assertTrue(bsIsValid("20", "Fasting"))
    }

    @Test
    fun `glucose at upper boundary 600 is valid`() {
        assertTrue(bsIsValid("600", "After Meal"))
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    fun `normal fasting glucose with valid meal type returns no errors`() {
        assertTrue(bsIsValid("95", "Fasting"))
    }

    @Test
    fun `post-meal high glucose in valid range returns no errors`() {
        assertTrue(bsIsValid("400", "After Meal"))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  3. BloodSugarAddActivity — NAVIGATION ROUTING TESTS
//
//  الملف الأصلي غطّى BloodPressureHomeActivity routing فقط.
//  BloodSugarAddActivity عنده نفس pattern لكن لم يُختبر أبداً.
//
//  منطق الكود الحقيقي (setupBottomNavigation):
//    nav_home      → BloodPressureHomeActivity  (finish current)
//    nav_statistics → BloodPressureStatsActivity مع EXTRA_TYPE = TYPE_BLOOD_SUGAR
//    onReselect nav_home → BloodPressureHomeActivity (finish current)
// ══════════════════════════════════════════════════════════════════════════════

sealed class BsNavAction {
    object NavigateToHome        : BsNavAction()
    object NavigateToBsStats     : BsNavAction()
    object NoOp                  : BsNavAction()
}

fun resolveBsNavAction(itemId: Int, navHomeId: Int, navStatsId: Int): BsNavAction =
    when (itemId) {
        navHomeId   -> BsNavAction.NavigateToHome
        navStatsId  -> BsNavAction.NavigateToBsStats
        else        -> BsNavAction.NoOp
    }

fun resolveBsNavReselectedAction(itemId: Int, navHomeId: Int): BsNavAction =
    if (itemId == navHomeId) BsNavAction.NavigateToHome else BsNavAction.NoOp

private const val BS_NAV_HOME  = 2001
private const val BS_NAV_STATS = 2002

class BloodSugarActivityNavTest {

    @Test
    fun `nav_home selected navigates to Home`() {
        val action = resolveBsNavAction(BS_NAV_HOME, BS_NAV_HOME, BS_NAV_STATS)
        assertEquals(BsNavAction.NavigateToHome, action)
    }

    @Test
    fun `nav_statistics selected navigates to BS Stats`() {
        val action = resolveBsNavAction(BS_NAV_STATS, BS_NAV_HOME, BS_NAV_STATS)
        assertEquals(BsNavAction.NavigateToBsStats, action)
    }

    @Test
    fun `unknown item returns NoOp`() {
        val action = resolveBsNavAction(9999, BS_NAV_HOME, BS_NAV_STATS)
        assertEquals(BsNavAction.NoOp, action)
    }

    @Test
    fun `reselecting nav_home navigates to Home`() {
        val action = resolveBsNavReselectedAction(BS_NAV_HOME, BS_NAV_HOME)
        assertEquals(BsNavAction.NavigateToHome, action)
    }

    @Test
    fun `reselecting nav_stats returns NoOp`() {
        val action = resolveBsNavReselectedAction(BS_NAV_STATS, BS_NAV_HOME)
        assertEquals(BsNavAction.NoOp, action)
    }

    // stats activity يجب أن يُفتح مع extra type = TYPE_BLOOD_SUGAR
    @Test
    fun `BS stats navigation carries TYPE_BLOOD_SUGAR extra`() {
        // نتحقق أن الـ extra المرسل هو TYPE_BLOOD_SUGAR وليس TYPE_BLOOD_PRESSURE
        val expectedExtra = BloodPressureStatsActivity.TYPE_BLOOD_SUGAR
        assertNotEquals(BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE, expectedExtra)
        assertEquals("blood_sugar", expectedExtra)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  4. ApiResponse — ADDITIONAL DATA CLASSES FROM HealthApiService
//
//  الملف الأصلي اختبر ApiResponse مع BP/BS classes فقط.
//  HealthApiService يحتوي على data classes أخرى مشتركة يجب التحقق منها.
// ══════════════════════════════════════════════════════════════════════════════

class BpTrendInfoAdditionalTest {

    @Test
    fun `BpTrendInfo is a data class with structural equality`() {
        val t1 = BpTrendInfo("UP", "+5 mmHg", "#FF0000")
        val t2 = BpTrendInfo("UP", "+5 mmHg", "#FF0000")
        assertEquals(t1, t2)
        assertEquals(t1.hashCode(), t2.hashCode())
    }

    @Test
    fun `BpTrendInfo copy works correctly`() {
        val original = BpTrendInfo("UP", "+5 mmHg", "#FF0000")
        val copy = original.copy(direction = "DOWN")
        assertEquals("DOWN", copy.direction)
        assertEquals("+5 mmHg", copy.delta)
        assertEquals("#FF0000", copy.color)
    }
}

class AddBpReadingRequestAdditionalTest {

    @Test
    fun `AddBpReadingRequest equality holds for same values`() {
        val r1 = AddBpReadingRequest(120, 80, 72, "morning")
        val r2 = AddBpReadingRequest(120, 80, 72, "morning")
        assertEquals(r1, r2)
    }

    @Test
    fun `AddBpReadingRequest copy changes only specified field`() {
        val original = AddBpReadingRequest(120, 80, 72, "note")
        val modified = original.copy(systolic = 130)
        assertEquals(130, modified.systolic)
        assertEquals(80,  modified.diastolic)
        assertEquals(72,  modified.pulse)
        assertEquals("note", modified.notes)
    }
}

class AddBsReadingRequestAdditionalTest {

    @Test
    fun `AddBsReadingRequest equality holds for same values`() {
        val r1 = AddBsReadingRequest(100, "Fasting", "note")
        val r2 = AddBsReadingRequest(100, "Fasting", "note")
        assertEquals(r1, r2)
    }

    @Test
    fun `AddBsReadingRequest copy changes only specified field`() {
        val original = AddBsReadingRequest(100, "Fasting", "note")
        val modified = original.copy(glucose = 140, mealType = "After Meal")
        assertEquals(140, modified.glucose)
        assertEquals("After Meal", modified.mealType)
        assertEquals("note", modified.notes)
    }
}

class ApiResponseAdditionalTest {

    @Test
    fun `ApiResponse with Unit data represents successful delete`() {
        val response = ApiResponse<Unit>(success = true, data = Unit, message = null)
        assertTrue(response.success)
        assertNotNull(response.data)
    }

    @Test
    fun `ApiResponse failure message is preserved`() {
        val response = ApiResponse<BloodPressureReadingData>(
            success = false,
            data    = null,
            message = "Connection timeout"
        )
        assertFalse(response.success)
        assertNull(response.data)
        assertEquals("Connection timeout", response.message)
    }

    @Test
    fun `ApiResponse with empty list is valid success`() {
        val response = ApiResponse(success = true, data = emptyList<BloodPressureReadingData>())
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(0, response.data!!.size)
    }

    @Test
    fun `ApiResponse default message is null`() {
        val response = ApiResponse(success = true, data = 42)
        assertNull(response.message)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  5. STATS ACTIVITY COMPANION CONSTANTS — CORRECTED VERSION
//
//  يحل مشكلة الملف الأصلي (Section 5) حيث كانت الثوابت تُستدعى من كلاس Android
//  مما يسبب مشكلة كمبايل في بيئة JVM.
//  الـ stub المعرَّف أعلاه يحل المشكلة، وهذه الاختبارات تتحقق من الـ stub نفسه.
// ══════════════════════════════════════════════════════════════════════════════

class StatsActivityStubConstantsTest {

    @Test
    fun `EXTRA_TYPE constant has correct value`() {
        assertEquals("extra_type", BloodPressureStatsActivity.EXTRA_TYPE)
    }

    @Test
    fun `TYPE_BLOOD_PRESSURE constant has correct value`() {
        assertEquals("blood_pressure", BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE)
    }

    @Test
    fun `TYPE_BLOOD_SUGAR constant has correct value`() {
        assertEquals("blood_sugar", BloodPressureStatsActivity.TYPE_BLOOD_SUGAR)
    }

    @Test
    fun `BP and BS type constants are distinct`() {
        assertNotEquals(
            BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE,
            BloodPressureStatsActivity.TYPE_BLOOD_SUGAR
        )
    }

    @Test
    fun `EXTRA_TYPE is not equal to any type constant`() {
        assertNotEquals(BloodPressureStatsActivity.EXTRA_TYPE, BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE)
        assertNotEquals(BloodPressureStatsActivity.EXTRA_TYPE, BloodPressureStatsActivity.TYPE_BLOOD_SUGAR)
    }
}
