package com.example.lavenda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // 🚨 ADD THIS: Load the theme BEFORE the screen is created
        val prefs = getSharedPreferences("LavendaPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("DARK_MODE", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState) // <-- super.onCreate stays here

        val isFirstRun = prefs.getBoolean("IS_FIRST_RUN", true)

        if (isFirstRun) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        } else {
            setContentView(R.layout.activity_splash)

            val userName = prefs.getString("USER_NAME", "Bestie")
            val tvGreeting = findViewById<TextView>(R.id.tvSplashGreeting)

            tvGreeting.text = "Heyyy $userName! 🌸"

            tvGreeting.translationY = 50f
            tvGreeting.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .start()

            lifecycleScope.launch {
                delay(2000)
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
    }
}