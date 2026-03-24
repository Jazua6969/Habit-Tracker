package com.example.lavenda

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("LavendaPrefs", Context.MODE_PRIVATE)

        val etEditName = findViewById<EditText>(R.id.etEditName)
        val btnSaveName = findViewById<Button>(R.id.btnSaveName)
        val switchTheme = findViewById<SwitchCompat>(R.id.switchTheme)

        // 1. Load Current Name
        // Using "Bestie" as the default to match your MainActivity fallback
        val currentName = prefs.getString("USER_NAME", "Bestie")
        etEditName.setText(currentName)

        // 2. Save New Name
        btnSaveName.setOnClickListener {
            val newName = etEditName.text.toString().trim()
            if (newName.isNotEmpty()) {
                prefs.edit().putString("USER_NAME", newName).apply()
                Toast.makeText(this, "Name updated!", Toast.LENGTH_SHORT).show()
            } else {
                etEditName.error = "Name cannot be empty!"
            }
        }

        // 3. Load & Handle Theme
        // 🎯 FIX: Changed default value to 'true' so it starts in Dark Mode on first run
        val isDarkMode = prefs.getBoolean("DARK_MODE", true)
        switchTheme.isChecked = isDarkMode

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            // Save the user's manual choice to preferences
            prefs.edit().putBoolean("DARK_MODE", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}