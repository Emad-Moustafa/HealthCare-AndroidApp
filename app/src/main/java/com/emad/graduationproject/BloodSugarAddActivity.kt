package com.emad.graduationproject

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * BloodSugarAddActivity
 *
 * Allows the user to log a new blood sugar (glucose) reading.
 * Layout: blood_sugar_add.xml
 *
 * Fields:
 *  - Glucose (mg/dL)  — et_glucose   inside til_glucose
 *  - Meal Type        — et_meal_type inside til_meal_type (ExposedDropdown)
 *  - Notes (optional) — et_notes     inside til_notes
 *
 * On save: validates → persists via BloodPressureTrackerRepository (API) → finishes.
 */
class BloodSugarAddActivity : AppCompatActivity() {

    // ── Input layouts ─────────────────────────────────────────────────────────
    private lateinit var tilGlucose: TextInputLayout
    private lateinit var tilMealType: TextInputLayout

    // ── Edit texts ────────────────────────────────────────────────────────────
    private lateinit var etGlucose: TextInputEditText
    private lateinit var etMealType: AutoCompleteTextView
    private lateinit var etNotes: TextInputEditText

    // ── Action button ─────────────────────────────────────────────────────────
    private lateinit var btnSave: MaterialButton

    // ── Bottom Navigation ─────────────────────────────────────────────────────
    private lateinit var bottomNavigation: BottomNavigationView

    // ── Meal type options ─────────────────────────────────────────────────────
    private val mealTypes = listOf("Fasting", "Before Meal", "After Meal", "Bedtime")

    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blood_sugar_add)

        bindViews()
        setupMealTypeDropdown()
        setupClickListeners()
        setupBottomNavigation()
    }

    // ── View Binding ──────────────────────────────────────────────────────────

    private fun bindViews() {
        findViewById<android.widget.ImageView>(R.id.btn_back)
            .setOnClickListener { finish() }

        tilGlucose  = findViewById(R.id.til_glucose)
        tilMealType = findViewById(R.id.til_meal_type)

        etGlucose  = findViewById(R.id.et_glucose)
        etMealType = findViewById(R.id.et_meal_type)
        etNotes    = findViewById(R.id.et_notes)

        btnSave = findViewById(R.id.btn_save)

        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    // ── Bottom Navigation ─────────────────────────────────────────────────────

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, BloodPressureHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_statistics -> {
                    val intent = Intent(this, BloodPressureStatsActivity::class.java)
                    intent.putExtra(
                        BloodPressureStatsActivity.EXTRA_TYPE,
                        BloodPressureStatsActivity.TYPE_BLOOD_SUGAR
                    )
                    startActivity(intent)
                    true
                }
                else -> true
            }
        }
        bottomNavigation.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.nav_home) {
                val intent = Intent(this, BloodPressureHomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    // ── Meal Type Dropdown ────────────────────────────────────────────────────

    private fun setupMealTypeDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mealTypes)
        etMealType.setAdapter(adapter)
        etMealType.setText(mealTypes[0], false)
    }

    // ── Click Listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            hideKeyboard()
            if (validate()) {
                saveReading()
            }
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validate(): Boolean {
        var valid = true

        val glucoseText  = etGlucose.text?.toString()?.trim() ?: ""
        val mealTypeText = etMealType.text?.toString()?.trim() ?: ""

        tilGlucose.error = null
        if (glucoseText.isEmpty()) {
            tilGlucose.error = "Please enter your glucose level"
            valid = false
        } else {
            val v = glucoseText.toIntOrNull()
            if (v == null || v !in 20..600) {
                tilGlucose.error = "Enter a value between 20 and 600 mg/dL"
                valid = false
            }
        }

        tilMealType.error = null
        if (mealTypeText.isEmpty() || mealTypeText !in mealTypes) {
            tilMealType.error = "Please select a meal type"
            valid = false
        }

        return valid
    }

    // ── Save via API ──────────────────────────────────────────────────────────

    private fun saveReading() {
        val glucose  = etGlucose.text.toString().trim().toInt()
        val mealType = etMealType.text.toString().trim()
        val notes    = etNotes.text?.toString()?.trim() ?: ""

        btnSave.isEnabled = false

        lifecycleScope.launch {
            val saved = BloodPressureTrackerRepository.addBsReading(glucose, mealType, notes)
            if (saved != null) {
                Toast.makeText(
                    this@BloodSugarAddActivity,
                    "Saved! Status: ${saved.categoryLabel} (${saved.glucose} mg/dL — ${saved.mealType})",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this@BloodSugarAddActivity,
                    "Failed to save. Check your connection and try again.",
                    Toast.LENGTH_LONG
                ).show()
                btnSave.isEnabled = true
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun hideKeyboard() {
        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}