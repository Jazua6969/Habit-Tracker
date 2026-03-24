package com.example.lavenda

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private var habits: List<HabitWithStatus>,
    private val onCheckClick: (HabitWithStatus, Boolean) -> Unit,
    private val onLongClick: (HabitWithStatus) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.habitContainer)
        val tvIcon: TextView = view.findViewById(R.id.tvHabitIcon) // 🎯 NEW: Grab the emoji view
        val tvName: TextView = view.findViewById(R.id.tvHabitName)
        val cbDone: CheckBox = view.findViewById(R.id.cbHabitDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.tvName.text = habit.name

        val context = holder.itemView.context
        val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // 🎯 NEW: Apply Theme Colors AND Emojis
        if (isDarkMode) {
            holder.container.setBackgroundColor(Color.parseColor("#424242"))
            holder.tvName.setTextColor(Color.WHITE)
            holder.tvIcon.text = "✨" // Dark mode emoji (feel free to change this to 🌙 or 🌌!)
            holder.cbDone.buttonTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
        } else {
            holder.container.setBackgroundColor(Color.parseColor("#F3E5F5"))
            holder.tvName.setTextColor(Color.parseColor("#4A148C"))
            holder.tvIcon.text = "🌸" // Light mode emoji
            holder.cbDone.buttonTintList = ColorStateList.valueOf(Color.parseColor("#7B1FA2"))
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(habit)
            true
        }

        if (habit.isCompleted) {
            holder.tvName.paintFlags = holder.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvName.alpha = 0.5f
        } else {
            holder.tvName.paintFlags = holder.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvName.alpha = 1.0f
        }

        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = habit.isCompleted
        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            onCheckClick(habit, isChecked)
        }
    }

    override fun getItemCount() = habits.size

    fun updateData(newHabits: List<HabitWithStatus>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}