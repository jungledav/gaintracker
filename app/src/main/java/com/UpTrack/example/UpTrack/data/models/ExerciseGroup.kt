package com.UpTrack.example.UpTrack.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_groups")
data class ExerciseGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
