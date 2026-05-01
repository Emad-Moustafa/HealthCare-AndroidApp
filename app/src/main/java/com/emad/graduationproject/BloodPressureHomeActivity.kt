package com.emad.graduationproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class BloodPressureHomeActivity : AppCompatActivity() {

    private lateinit var btnAddBloodPressure: MaterialButton
    private lateinit var btnBpStatistics: MaterialButton
    private lateinit var btnAddBloodSugar: MaterialButton
    private lateinit var btnBsStatistics: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView

    // Guard flag: when true, the bottom-nav listener ignores the next
    // selection event (used to silently correct the highlighted item).
    private var suppressNavEvent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blood_pressure_home)
        bindViews()
        setupClickListeners()
        // Set selectedItemId BEFORE attaching any listener so the initial
        // highlight is correct without triggering navigation.
        bottomNavigation.selectedItemId = R.id.nav_home
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        // When returning from StatsActivity the nav bar may still highlight
        // the statistics item. Only correct it if it is actually wrong, and
        // suppress both the selected AND reselected listeners while doing so
        // to avoid triggering any unintended navigation.
        if (bottomNavigation.selectedItemId != R.id.nav_home) {
            suppressNavEvent = true
            bottomNavigation.selectedItemId = R.id.nav_home
            suppressNavEvent = false
        }
    }

    private fun bindViews() {
        findViewById<android.widget.ImageView>(R.id.btn_back)
            .setOnClickListener { finish() }

        btnAddBloodPressure = findViewById(R.id.btn_add_blood_pressure)
        btnBpStatistics     = findViewById(R.id.btn_bp_statistics)
        btnAddBloodSugar    = findViewById(R.id.btn_add_blood_sugar)
        btnBsStatistics     = findViewById(R.id.btn_bs_statistics)
        bottomNavigation    = findViewById(R.id.bottom_navigation)
    }

    private fun setupClickListeners() {
        btnAddBloodPressure.setOnClickListener {
            startActivity(Intent(this, BloodPressureAddActivity::class.java))
        }

        btnBpStatistics.setOnClickListener {
            startActivity(Intent(this, BloodPressureStatsActivity::class.java).apply {
                putExtra(BloodPressureStatsActivity.EXTRA_TYPE,
                    BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE)
            })
        }

        btnAddBloodSugar.setOnClickListener {
            startActivity(Intent(this, BloodSugarAddActivity::class.java))
        }

        btnBsStatistics.setOnClickListener {
            startActivity(Intent(this, BloodPressureStatsActivity::class.java).apply {
                putExtra(BloodPressureStatsActivity.EXTRA_TYPE,
                    BloodPressureStatsActivity.TYPE_BLOOD_SUGAR)
            })
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            // Silently ignore events that are only correcting the visual highlight.
            if (suppressNavEvent) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, WaterHomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    finish()
                    true
                }
                R.id.nav_statistics -> {
                    startActivity(Intent(this, BloodPressureStatsActivity::class.java).apply {
                        putExtra(BloodPressureStatsActivity.EXTRA_TYPE,
                            BloodPressureStatsActivity.TYPE_BLOOD_PRESSURE)
                    })
                    true
                }
                else -> true
            }
        }

        bottomNavigation.setOnItemReselectedListener { item ->
            if (suppressNavEvent) return@setOnItemReselectedListener
            if (item.itemId == R.id.nav_home) {
                startActivity(Intent(this, WaterHomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
                finish()
            }
        }
    }
}