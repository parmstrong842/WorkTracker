package com.example.worktracker.data

import androidx.room.*

@Dao
interface ShiftDao {

    @Query("SELECT * from shifts ORDER BY date ASC")
    suspend fun getAllItems(): List<Shift>

    @Query("SELECT * from shifts WHERE id = :id")
    suspend fun getItem(id: Int): Shift

    // Specify the conflict strategy as IGNORE, when the user tries to add an
    // existing Item into the database Room ignores the conflict.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Shift)

    @Update
    suspend fun update(item: Shift)

    @Delete
    suspend fun delete(item: Shift)
}