package com.example.lavenda

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: HabitAdapter

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 🌙 LOAD THEME PREFERENCE ---
        val prefs = getSharedPreferences("LavendaPrefs", Context.MODE_PRIVATE)
        // 🎯 We store this in a variable right away so we can use it for the buttons below!
        val isDarkMode = prefs.getBoolean("DARK_MODE", true)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        // --- ✨ THE GREETING ANIMATION ---
        val userName = prefs.getString("USER_NAME", "Bestie")
        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        tvTitle.text = "Heyyy $userName! :)"

        tvTitle.alpha = 0f
        tvTitle.translationY = 50f

        tvTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .start()
        // ---------------------------------

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "lavenda-db")
            .fallbackToDestructiveMigration()
            .build()

        val recyclerView = findViewById<RecyclerView>(R.id.rvHabits)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HabitAdapter(
            emptyList(),
            { habit, isChecked ->
                handleCheck(habit, isChecked)
                if (isChecked) triggerConfetti()
                triggerHapticFeedback(recyclerView)
            },
            { habit ->
                triggerHapticFeedback(recyclerView)
                showEditDeleteDialog(habit)
            }
        )
        recyclerView.adapter = adapter

        loadHabitsForToday()
        updateStreak()

        // --- 🔘 BUTTON INITIALIZATION & THEME LOGIC ---
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        val fabCalendar = findViewById<FloatingActionButton>(R.id.fabCalendar)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        // 🎯 FIX: Using the saved preference instead of the lagging system configuration
        if (isDarkMode) {
            fabCalendar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#424242"))
            fabCalendar.imageTintList = ColorStateList.valueOf(Color.WHITE)

            fabAdd.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#616161"))
            fabAdd.imageTintList = ColorStateList.valueOf(Color.WHITE)

            btnSettings.imageTintList = ColorStateList.valueOf(Color.WHITE)
        } else {
            fabCalendar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E1BEE7"))
            fabCalendar.imageTintList = ColorStateList.valueOf(Color.parseColor("#4A148C"))

            fabAdd.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CE93D8"))
            fabAdd.imageTintList = ColorStateList.valueOf(Color.BLACK)

            btnSettings.imageTintList = ColorStateList.valueOf(Color.parseColor("#CE93D8"))
        }

        // 🎯 NEW: Call the custom dialog and pass 'null' because it's a new habit
        fabAdd.setOnClickListener {
            showCustomHabitDialog(null)
        }

        fabCalendar.setOnClickListener {
            val intent = Intent(this, GridActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStreak()
        val prefs = getSharedPreferences("LavendaPrefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("USER_NAME", "Bestie")
        findViewById<TextView>(R.id.tvTitle).text = "Heyyy $userName! :)"
    }

    private fun updateStreak() {
        lifecycleScope.launch(Dispatchers.IO) {
            var streak = 0
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

            val todayCode = sdf.format(cal.time).toLong()
            val todayHabits = db.habitDao().getHabitsForDate(todayCode).first()
            val isTodayPerfect = todayHabits.isNotEmpty() && todayHabits.all { it.isCompleted }

            if (isTodayPerfect) {
                streak = 1
                withContext(Dispatchers.Main) {
                    val recyclerView = findViewById<RecyclerView>(R.id.rvHabits)
                    recyclerView.performHapticFeedback(
                        android.view.HapticFeedbackConstants.LONG_PRESS,
                        android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                }
            }

            var checking = true
            val walkCal = Calendar.getInstance()

            while (checking) {
                walkCal.add(Calendar.DAY_OF_YEAR, -1)
                val dateCode = sdf.format(walkCal.time).toLong()
                val habitsForDay = db.habitDao().getHabitsForDate(dateCode).first()

                if (habitsForDay.isNotEmpty() && habitsForDay.all { it.isCompleted }) {
                    streak++
                } else {
                    checking = false
                }
            }

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.tvStreakCount).text = streak.toString()
            }
        }
    }

    private fun handleCheck(habit: HabitWithStatus, isChecked: Boolean) {
        lifecycleScope.launch {
            if (isChecked) {
                val entry = HabitEntry(habitId = habit.id, date = getTodayDateCode())
                db.habitDao().insertEntry(entry)
            } else {
                db.habitDao().deleteEntry(habit.id, getTodayDateCode())
            }
            updateStreak()
        }
    }

    private fun triggerConfetti() {
        val viewKonfetti = findViewById<KonfettiView>(R.id.konfettiView)
        val party = Party(
            speed = 0f, maxSpeed = 30f, damping = 0.9f, spread = 360,
            colors = listOf(0xce93d8, 0xba68c8, 0xfce18a, 0xff726d),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        viewKonfetti.start(party)
    }

    private fun getTodayDateCode(): Long {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date()).toLong()
    }

    private fun loadHabitsForToday() {
        lifecycleScope.launch {
            db.habitDao().getHabitsForDate(getTodayDateCode()).collect { list ->
                adapter.updateData(list)
            }
        }
    }

    private fun showEditDeleteDialog(habit: HabitWithStatus) {
        val options = arrayOf("Rename Task", "Delete Task")

        AlertDialog.Builder(this)
            .setTitle(habit.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showCustomHabitDialog(habit) // 🎯 Calls our new dialog!
                    1 -> confirmDelete(habit)
                }
            }
            .show()
    }

    // ✨ THE NEW CUSTOM DIALOG LOGIC ✨
    private fun showCustomHabitDialog(existingHabit: HabitWithStatus?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_habit)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.dialogContainer)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val etName = dialog.findViewById<EditText>(R.id.etHabitName)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)

        val prefs = getSharedPreferences("LavendaPrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("DARK_MODE", false)

        if (isDarkMode) {
            container.background = createRoundedBg(Color.parseColor("#303030"))
            tvTitle.setTextColor(Color.WHITE)
            etName.setTextColor(Color.WHITE)

            btnCancel.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#616161"))
            btnCancel.setTextColor(Color.WHITE)

            btnSave.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
            btnSave.setTextColor(Color.BLACK)
        } else {
            container.background = createRoundedBg(Color.parseColor("#FFFFFF"))
            tvTitle.setTextColor(Color.parseColor("#4A148C"))
            etName.setTextColor(Color.BLACK)

            btnCancel.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
            btnCancel.setTextColor(Color.BLACK)

            btnSave.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CE93D8"))
            btnSave.setTextColor(Color.BLACK)
        }

        if (existingHabit != null) {
            tvTitle.text = "Rename Habit ✍️"
            etName.setText(existingHabit.name)
            etName.setSelection(etName.text.length)
            btnSave.text = "Update"
        } else {
            tvTitle.text = "New Habit ✨"
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                lifecycleScope.launch {
                    if (existingHabit != null) {
                        db.habitDao().updateHabitName(existingHabit.id, name)
                    } else {
                        db.habitDao().insertHabit(Habit(name = name, createdAt = getTodayDateCode()))
                        updateStreak()
                    }
                    dialog.dismiss()
                }
            } else {
                etName.error = "Name cannot be empty!"
            }
        }

        dialog.show()
    }

    private fun createRoundedBg(color: Int): GradientDrawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadius = 60f
        shape.setColor(color)
        return shape
    }

    private fun confirmDelete(habit: HabitWithStatus) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.deleteDialogContainer)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvDeleteTitle)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelDelete)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmDelete)

        // 🌙 Theme Logic
        val prefs = getSharedPreferences("LavendaPrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("DARK_MODE", false)

        if (isDarkMode) {
            container.background = createRoundedBg(Color.parseColor("#303030")) // Dark Grey
            tvTitle.setTextColor(Color.WHITE)

            btnCancel.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#616161"))
            btnCancel.setTextColor(Color.WHITE)
        } else {
            container.background = createRoundedBg(Color.parseColor("#FFFFFF")) // White
            tvTitle.setTextColor(Color.parseColor("#4A148C")) // Lavenda Purple

            btnCancel.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
            btnCancel.setTextColor(Color.BLACK)
        }
        // Note: The Delete button stays Red (#FF5252) in both modes to clearly show it's destructive!

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            lifecycleScope.launch {
                db.habitDao().deleteHabit(habit.id)
                updateStreak()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun triggerHapticFeedback(view: View, isStrong: Boolean = false) {
        view.isHapticFeedbackEnabled = true

        val constant = if (isStrong) {
            android.view.HapticFeedbackConstants.LONG_PRESS
        } else {
            android.view.HapticFeedbackConstants.CONFIRM
        }

        view.performHapticFeedback(
            constant,
            android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}