package com.UpTrack.example.UpTrack.data.models

data class ExerciseData(
    val exerciseId: Long,
    val exerciseGroupId: Long,
    val exerciseDate: Long,
    val groupName: String,
    val setsCount: Int
)