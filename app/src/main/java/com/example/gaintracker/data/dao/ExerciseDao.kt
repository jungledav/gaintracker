package com.example.gaintracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

import com.example.gaintracker.data.models.Exercise
import com.example.gaintracker.data.models.ExerciseSet

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(exercise: Exercise): Long

    @Update
    suspend fun update(exercise: Exercise)

    @Query("SELECT * FROM exercises ORDER BY date DESC")
    fun getAllExercises(): LiveData<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Long): Exercise

    @Query("SELECT * FROM exercises")
    fun getAllExerciseNames(): LiveData<List<Exercise>>

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("""
    SELECT * FROM exercise_sets 
    WHERE exercise_id IN (
        SELECT id FROM exercises WHERE exerciseGroupId = :exerciseGroupId
    )
""")
    fun getSetsForExerciseGroup(exerciseGroupId: Int): LiveData<List<ExerciseSet>>

    @Query("SELECT exerciseGroupId FROM exercises WHERE id = :exerciseId")
    fun getExerciseGroupId(exerciseId: Long): LiveData<Long>

}
