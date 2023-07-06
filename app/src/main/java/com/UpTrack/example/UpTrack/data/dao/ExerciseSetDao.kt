package com.UpTrack.example.UpTrack.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.UpTrack.example.UpTrack.data.models.ExerciseSet

@Dao
interface ExerciseSetDao {
    @Query("SELECT * FROM exercise_sets")
    fun getAllExerciseSets(): LiveData<List<ExerciseSet>>

    @Insert
    suspend fun insert(exerciseSet: ExerciseSet): Long

    @Update
    suspend fun update(exerciseSet: ExerciseSet)

    @Query("SELECT * FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getExerciseSetsByExerciseId(exerciseId: Long): LiveData<List<ExerciseSet>>

    @Query("SELECT * FROM exercise_sets WHERE id = :exerciseSetId")
    suspend fun getExerciseSetById(exerciseSetId: Long): ExerciseSet

    @Query("SELECT * FROM exercise_sets WHERE exercise_id = :exerciseId")
    suspend fun getSetsForExercise(exerciseId: Long): List<ExerciseSet>

    @Delete
    suspend fun deleteExerciseSet(exerciseSet: ExerciseSet)
    @Query("SELECT EXISTS(SELECT * FROM exercise_sets WHERE exercise_id IN (SELECT id FROM exercises WHERE exerciseGroupId = :exerciseGroupId))")
    fun doesExerciseSetExist(exerciseGroupId: Long): LiveData<Boolean>
}
