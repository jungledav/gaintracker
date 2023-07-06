package com.UpTrack.example.UpTrack.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_history")
data class ExerciseHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val exerciseId: Long,
    val reps: Int,
    val weight: Double,
    val date: Long
)
