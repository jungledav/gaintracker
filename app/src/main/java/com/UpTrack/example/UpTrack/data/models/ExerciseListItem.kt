package com.UpTrack.example.UpTrack.data.models

import java.util.Date

sealed class ExerciseListItem {
    data class ExerciseItem(
        val exerciseId: Long,
        val exerciseGroupId: Long,
        val exerciseDate: Long,
        val exerciseGroupName: String,
        val totalSets: Int
    ) : ExerciseListItem()

    data class DividerItem(val date: Date) : ExerciseListItem()
    object NoExercisesTodayItem : ExerciseListItem()
    object AddAnotherExerciseItem : ExerciseListItem()  // Add this line
}




