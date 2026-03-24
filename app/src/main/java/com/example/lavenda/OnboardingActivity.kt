package com.example.lavenda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val etUserName = findViewById<EditText>(R.id.etUserName)
        val btnStart = findViewById<Button>(R.id.btnStartBlooming)

        btnStart.setOnClickListener {
            val name = etUserName.text.toString().trim()

            if (name.isNotEmpty()) {
                // 1. Open the "SharedPreferences" vault
                val prefs = getSharedPreferences("LavendaPrefs", Context.MODE_PRIVATE)

                // 2. Save the name and mark First Run as False
                prefs.edit()
                    .putString("USER_NAME", name)
                    .putBoolean("IS_FIRST_RUN", false)
                    .apply()

                // 3. Go to MainActivity and destroy this screen so they can't hit "Back" to it
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}