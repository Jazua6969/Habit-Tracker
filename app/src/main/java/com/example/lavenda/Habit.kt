package com.example.lavenda

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: String = "#E6E6FA", // Default Lavender hex
    val createdAt: Long // This tracks the date (e.g., 20260131) so history stays accurate
)