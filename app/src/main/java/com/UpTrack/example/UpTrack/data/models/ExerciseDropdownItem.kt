package com.UpTrack.example.UpTrack.data.models

sealed class ExerciseDropdownItem {
    data class Exercise(val name: String, val lastTrained: String?): ExerciseDropdownItem()
    data class SubHeader(val title: String): ExerciseDropdownItem()
}
