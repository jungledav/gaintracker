package com.UpTrack.example.UpTrack.data.models

import android.util.Log


data class MaxRepsForExercise(
    val exerciseDate: Long,
    val maxReps: Float
) {
    init {
        Log.d("DEBUG", "MaxRepsForExercise created with date: $exerciseDate and reps: $maxReps")
    }
}
