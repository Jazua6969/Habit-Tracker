package com.example.lavenda

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_entries")
data class HabitEntry(
    @PrimaryKey(autoGenerate = true) val entryId: Int = 0,
    val habitId: Int,
    val date: Long, // We will store date as a simple number like 20231027
    val isCompleted: Boolean = true
)