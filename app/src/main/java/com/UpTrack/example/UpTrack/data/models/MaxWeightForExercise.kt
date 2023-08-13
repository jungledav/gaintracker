package com.UpTrack.example.UpTrack.data.models

import android.util.Log


data class MaxWeightForExercise(
    val exerciseDate: Long,
    val maxWeight: Float
){
    init {
        Log.d("DEBUG", "MaxWeightForExercise created with date: $exerciseDate and reps: $maxWeight")
    }
}
