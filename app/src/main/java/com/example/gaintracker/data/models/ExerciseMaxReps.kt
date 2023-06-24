package com.example.gaintracker.data.models

import androidx.room.ColumnInfo

data class ExerciseMaxReps(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "total_reps") val totalReps: Int,
    @ColumnInfo(name = "exerciseGroupId") val exerciseGroupId: Long
)
