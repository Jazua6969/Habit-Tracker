package com.example.lavenda

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Habit::class, HabitEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}