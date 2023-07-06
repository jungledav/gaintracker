package com.UpTrack.example.UpTrack.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.UpTrack.example.UpTrack.data.models.ExerciseSet

@Dao
interface ExerciseHistoryDao {

    @Query("SELECT exercise_sets.* FROM exercise_sets INNER JOIN exercises ON exercise_sets.exercise_id = exercises.id WHERE exercises.exerciseGroupId = :exerciseGroupId ORDER BY exercise_sets.date DESC")
    fun getExerciseHistory(exerciseGroupId: Int): LiveData<List<ExerciseSet>>
}
