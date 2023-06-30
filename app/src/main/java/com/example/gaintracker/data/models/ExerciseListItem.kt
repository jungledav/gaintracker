package com.example.gaintracker.data.models

import java.util.Date

sealed class ExerciseListItem {
    data class ExerciseItem(val exercise: Exercise, val exerciseGroupName: String) : ExerciseListItem()
    data class DividerItem(val date: Date) : ExerciseListItem()
    object NoExercisesTodayItem : ExerciseListItem()
    object AddAnotherExerciseItem : ExerciseListItem()  // Add this line
}




