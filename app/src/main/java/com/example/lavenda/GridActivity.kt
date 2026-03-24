package com.example.lavenda

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GridActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    // 🎯 NEW: Variable to track which month the user is currently looking at
    private var currentDisplayedMonth = Calendar.getInstance()

    private val thoughts = listOf(
        "Consistency is the secret to lasting growth.",
        "Each day offers a fresh chance to grow and thrive.",
        "A bright future is built one intentional day at a time. ✨",
        "Small, steady steps lead to incredible transformations.",
        "Focus on the present step, not the entire journey ahead.",
        "Growth has no deadline; bloom at your own natural pace.",
        "Strength is found in the quiet moments of perseverance.",
        "Believe in the progress you are making today.",
        "You have the power to achieve exactly what you set your mind to.",
        "You are capable of amazing things.",
        "Keep going—you have everything it takes to succeed."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "lavenda-db")
            .fallbackToDestructiveMigration()
            .build()

        val gridView = findViewById<GridView>(R.id.calendarGrid)
        val btnPrev = findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = findViewById<ImageButton>(R.id.btnNextMonth)

        // 🎯 NEW: Listeners for the Time Travel buttons
        btnPrev.setOnClickListener {
            currentDisplayedMonth.add(Calendar.MONTH, -1)
            setupCalendar(gridView)
        }

        btnNext.setOnClickListener {
            currentDisplayedMonth.add(Calendar.MONTH, 1)
            setupCalendar(gridView)
        }

        // Load the initial calendar (current month)
        setupCalendar(gridView)
        displayDailyThought()
    }

    private fun displayDailyThought() {
        val tvThought = findViewById<TextView>(R.id.tvThoughtOfDay)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val dateSeed = todayStr.toLong()
        val randomIndex = (dateSeed % thoughts.size).toInt()
        tvThought.text = thoughts[randomIndex]
    }

    private fun startPulse(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.08f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.08f, 1f)
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE
        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY)
        set.duration = 2000
        set.start()

        view.tag = set
    }

    private fun setupCalendar(gridView: GridView) {
        // 🎯 NEW: Update the Month/Year header text
        val tvMonthYear = findViewById<TextView>(R.id.tvMonthYear)
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = monthYearFormat.format(currentDisplayedMonth.time)

        val days = mutableListOf<Date?>()

        // Use a clone of the currently displayed month to calculate the days
        val cal = currentDisplayedMonth.clone() as Calendar

        val todayCal = Calendar.getInstance() // Always represents the actual real-world "Today"
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
        val todayMonth = todayCal.get(Calendar.MONTH)
        val todayYear = todayCal.get(Calendar.YEAR)

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val monthMaxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1

        for (i in 0 until firstDayOfWeek) {
            days.add(null)
        }

        for (i in 0 until monthMaxDays) {
            days.add(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        gridView.adapter = object : BaseAdapter() {
            override fun getCount(): Int = days.size
            override fun getItem(p0: Int): Any? = days[p0]
            override fun getItemId(p0: Int): Long = p0.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: LayoutInflater.from(parent?.context)
                    .inflate(R.layout.item_calendar_day, parent, false)

                val date = days[position]

                if (date == null) {
                    view.visibility = View.INVISIBLE
                    return view
                } else {
                    view.visibility = View.VISIBLE
                }

                val calDate = Calendar.getInstance().apply { time = date }
                val dayOfMonth = calDate.get(Calendar.DAY_OF_MONTH)

                val tvDay = view.findViewById<TextView>(R.id.tvDayNumber)
                val progress = view.findViewById<ProgressBar>(R.id.dayProgressCircle)

                (view.tag as? AnimatorSet)?.cancel()
                view.scaleX = 1f
                view.scaleY = 1f

                val isDarkMode = (parent?.context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES

                val normalTextColor = if (isDarkMode) Color.WHITE else Color.BLACK
                val todayTextColor = if (isDarkMode) Color.WHITE else Color.parseColor("#4A148C")

                val ringNormal = if (isDarkMode) R.drawable.ring_progress_dark else R.drawable.ring_progress
                val ringToday = if (isDarkMode) R.drawable.ring_progress_today_dark else R.drawable.ring_progress_today

                // Check if the specific day we are drawing is the real-world "Today"
                val isToday = dayOfMonth == todayDay &&
                        calDate.get(Calendar.MONTH) == todayMonth &&
                        calDate.get(Calendar.YEAR) == todayYear

                if (isToday) {
                    progress.progressDrawable = ContextCompat.getDrawable(this@GridActivity, ringToday)
                    tvDay.setTypeface(null, Typeface.BOLD)
                    tvDay.setTextColor(todayTextColor)
                    startPulse(view)
                } else {
                    progress.progressDrawable = ContextCompat.getDrawable(this@GridActivity, ringNormal)
                    tvDay.setTypeface(null, Typeface.NORMAL)
                    tvDay.setTextColor(normalTextColor)
                }

                progress.progress = 0
                tvDay.text = dayOfMonth.toString()

                val dateCode = String.format("%04d%02d%02d", calDate.get(Calendar.YEAR), calDate.get(Calendar.MONTH) + 1, calDate.get(Calendar.DAY_OF_MONTH)).toLong()

                view.setOnClickListener {
                    lifecycleScope.launch {
                        val habits = db.habitDao().getHabitsForDate(dateCode).first()
                        val total = habits.size
                        val completed = habits.count { it.isCompleted }
                        if (total > 0) {
                            Toast.makeText(this@GridActivity, "$dayOfMonth Bloom: $completed/$total habits done! ✨", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@GridActivity, "No habits tracked on this day.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                lifecycleScope.launch {
                    val habits = db.habitDao().getHabitsForDate(dateCode).first()
                    if (habits.isNotEmpty()) {
                        val completed = habits.count { it.isCompleted }
                        val percentage = (completed.toFloat() / habits.size.toFloat() * 100).toInt()
                        progress.isIndeterminate = false
                        progress.progress = percentage
                    } else {
                        progress.progress = 0
                    }
                }

                return view
            }
        }
    }
}