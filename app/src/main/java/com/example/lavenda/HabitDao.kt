package com.example.lavenda

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// A helper helper to hold the data together
data class HabitWithStatus(
    val id: Int,
    val name: String,
    val color: String,
    val isCompleted: Boolean // This will be calculated by SQL
)

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: Habit)

    @Insert
    suspend fun insertEntry(entry: HabitEntry)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId AND date = :date")
    suspend fun deleteEntry(habitId: Int, date: Long)
    @Query("UPDATE habits SET name = :newName WHERE id = :id")
    suspend fun updateHabitName(id: Int, newName: String)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Int)

    // THE MAGIC QUERY: Fetches all habits + checks if done on specific date
    @Query("""
    SELECT h.id, 
           h.name, 
           h.color,
           CASE WHEN e.isCompleted IS NULL THEN 0 ELSE e.isCompleted END as isCompleted
    FROM habits h
    LEFT JOIN habit_entries e ON h.id = e.habitId AND e.date = :date
    WHERE h.createdAt <= :date
""")
    fun getHabitsForDate(date: Long): Flow<List<HabitWithStatus>>
}