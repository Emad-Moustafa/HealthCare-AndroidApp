package com.emad.graduationproject

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MoodHomeActivity : AppCompatActivity() {

    private lateinit var tvDayStreak:  TextView
    private lateinit var tvTotalLogs:  TextView
    private lateinit var btnAddMood:   MaterialButton
    private lateinit var btnViewStats: MaterialButton
    private lateinit var bottomNav:    BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_home)
        MoodRepository.init(applicationContext)
        bindViews()
        applyNavColors()
        bottomNav.selectedItemId = R.id.nav_home
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        refreshStats()
        applyNavColors()
        bottomNav.setOnItemSelectedListener(null)
        bottomNav.setOnItemReselectedListener(null)
        bottomNav.selectedItemId = R.id.nav_home
        setupListeners()
    }

    private fun bindViews() {
        tvDayStreak  = findViewById(R.id.tv_day_streak)
        tvTotalLogs  = findViewById(R.id.tv_total_logs)
        btnAddMood   = findViewById(R.id.btn_add_mood)
        btnViewStats = findViewById(R.id.btn_view_statistics)
        bottomNav    = findViewById(R.id.bottom_navigation)

        findViewById<android.widget.ImageView>(R.id.btn_back)
            .setOnClickListener { finish() }
    }

    private fun refreshStats() {
        lifecycleScope.launch {
            tvDayStreak.text = MoodRepository.getDayStreak().toString()
            tvTotalLogs.text = MoodRepository.getTotalLogs().toString()
        }
    }

    private fun applyNavColors() {
        val green = Color.parseColor("#C3E66E")
        val grey  = Color.parseColor("#737373")
        val tintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(green, grey)
        )
        bottomNav.itemIconTintList = tintList
        bottomNav.itemTextColor    = tintList
    }

    private fun setupListeners() {
        btnAddMood.setOnClickListener {
            startActivity(Intent(this, MoodFeelingSelectionActivity::class.java))
        }

        btnViewStats.setOnClickListener {
            startActivity(Intent(this, MoodStatsActivity::class.java))
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, WaterHomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    finish()
                    true
                }
                R.id.nav_statistics -> {
                    startActivity(Intent(this, MoodStatsActivity::class.java))
                    true
                }
                else -> true
            }
        }

        bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.nav_home) {
                startActivity(Intent(this, WaterHomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
                finish()
            }
        }
    }
}