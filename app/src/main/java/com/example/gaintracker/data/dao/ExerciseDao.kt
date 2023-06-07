package com.example.gaintracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

import com.example.gaintracker.data.models.Exercise
import com.example.gaintracker.data.models.ExerciseSet
import com.example.gaintracker.fragments.ExerciseHistoryFragment

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

    @Query("""
    SELECT exercise_sets.*, exercises.date AS exerciseDate
    FROM exercise_sets
    INNER JOIN exercises ON exercise_sets.exercise_id = exercises.id
    WHERE exercises.exerciseGroupId = :exerciseGroupId
    ORDER BY exercise_sets.date DESC
""")
    fun getSetsForExerciseGroupWithExerciseDate(exerciseGroupId: Long): List<ExerciseHistoryFragment.ExerciseSetWithExerciseDate>

    @Query("SELECT MAX(weight) FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getMaxWeightForExercise(exerciseId: Int): LiveData<Float>
    @Query("""
    SELECT MAX(exercise_sets.weight) 
    FROM exercise_sets
    INNER JOIN exercises ON exercise_sets.exercise_id = exercises.id
    WHERE exercises.exerciseGroupId = (SELECT exerciseGroupId FROM exercises WHERE id = :exerciseId)
    """)
    fun getMaxWeightForExerciseType(exerciseId: Long): LiveData<Float>

    @Query("SELECT * FROM exercises ORDER BY date DESC LIMIT 1")
    suspend fun getLatestExercise(): Exercise?

    // Counts the total number of workouts
    @Query("SELECT COUNT(DISTINCT DATE(date / 1000, 'unixepoch')) FROM exercises")
    suspend fun countTotalWorkouts(): Int


    // Counts the total number of exercises
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countTotalExercises(): Int

    // Counts the total number of sets
    @Query("SELECT COUNT(*) FROM exercise_sets")
    suspend fun countTotalSets(): Int

    // Counts the total number of reps
    @Query("SELECT SUM(reps) FROM exercise_sets")
    suspend fun countTotalReps(): Int

    // Counts the total weight lifted
    @Query("SELECT SUM(weight) FROM exercise_sets")
    suspend fun countTotalWeight(): Float
    @Query("SELECT MAX(reps) FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getMaxRepForExercise(exerciseId: Long): LiveData<Int>

    @Query("SELECT SUM(reps) FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getTotalRepsForExercise(exerciseId: Long): LiveData<Int>

    @Query("SELECT SUM(reps * weight) FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getExerciseVolumeForExercise(exerciseId: Long): LiveData<Double>

    @Query("SELECT MAX(reps * weight) FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getMaxSetVolumeForExercise(exerciseId: Long): LiveData<Double>
}
