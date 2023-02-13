package com.example.worktracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var date: String,
    val shiftSpan: String,
    val breakTotal: String,
    val shiftTotal: String,
)