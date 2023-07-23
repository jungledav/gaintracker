package com.UpTrack.example.UpTrack.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.UpTrack.example.UpTrack.data.models.Exercise
import com.UpTrack.example.UpTrack.data.models.ExerciseData
import com.UpTrack.example.UpTrack.data.models.ExerciseMaxReps
import com.UpTrack.example.UpTrack.data.models.ExerciseSet
import com.UpTrack.example.UpTrack.data.models.ExerciseSetVolume
import com.UpTrack.example.UpTrack.data.models.MaxOneRepForExercise
import com.UpTrack.example.UpTrack.fragments.ExerciseHistoryFragment
import kotlinx.coroutines.flow.Flow



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

    @Query(
        """
    SELECT * FROM exercise_sets 
    WHERE exercise_id IN (
        SELECT id FROM exercises WHERE exerciseGroupId = :exerciseGroupId
    )
"""
    )
    fun getSetsForExerciseGroup(exerciseGroupId: Int): LiveData<List<ExerciseSet>>

    @Query("SELECT exerciseGroupId FROM exercises WHERE id = :exerciseId")
    fun getExerciseGroupId(exerciseId: Long): LiveData<Long?>?


    @Query(
        """
    SELECT exercise_sets.*, exercises.date AS exerciseDate
    FROM exercise_sets
    INNER JOIN exercises ON exercise_sets.exercise_id = exercises.id
    WHERE exercises.exerciseGroupId = :exerciseGroupId
    ORDER BY exercise_sets.date DESC
"""
    )
    fun getSetsForExerciseGroupWithExerciseDate(exerciseGroupId: Long): List<ExerciseHistoryFragment.ExerciseSetWithExerciseDate>

    @Query("SELECT MAX(weight) FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getMaxWeightForExercise(exerciseId: Int): LiveData<Float>

    @Query(
        """
    SELECT MAX(exercise_sets.weight) 
    FROM exercise_sets
    INNER JOIN exercises ON exercise_sets.exercise_id = exercises.id
    WHERE exercises.exerciseGroupId = (SELECT exerciseGroupId FROM exercises WHERE id = :exerciseId)
    """
    )
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


    @Query(
        """
    SELECT exercises.date
    FROM exercises
    WHERE exercises.id = (
        SELECT exercise_sets.exercise_id
        FROM exercise_sets
        WHERE exercise_sets.weight = (
            SELECT MAX(exercise_sets.weight)
            FROM exercise_sets
            WHERE exercise_sets.exercise_id IN (
                SELECT exercises.id
                FROM exercises
                WHERE exercises.exerciseGroupId = (
                    SELECT exercises.exerciseGroupId
                    FROM exercises
                    WHERE exercises.id = :exerciseId
                )
            )
        )
        LIMIT 1
    )
"""
    )
    fun getMaxWeightDateForExercise(exerciseId: Long): LiveData<Long?>


    @Query("SELECT MAX(weight) FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getTodayMaxWeightForExercise(exerciseId: Long): LiveData<Double>

    @Query("SELECT MAX(reps) FROM exercise_sets WHERE exercise_id = :exerciseId")
    fun getMaxRepsForExercise(exerciseId: Long): LiveData<Int>

    @Query("SELECT MAX(reps) FROM exercise_sets WHERE exercise_id IN (SELECT id FROM exercises WHERE exerciseGroupId = (SELECT exerciseGroupId FROM exercises WHERE id = :exerciseId))")
    fun getMaxRepsForExerciseGroup(exerciseId: Long): LiveData<Int>

    @Query("SELECT date FROM exercise_sets WHERE reps = (SELECT MAX(reps) FROM exercise_sets WHERE exercise_id = :exerciseId)")
    fun getMaxRepsDateForExercise(exerciseId: Long): LiveData<Long>

    @Query(
        """
    SELECT exercises.date
    FROM exercises
    INNER JOIN exercise_sets ON exercise_sets.exercise_id = exercises.id
    WHERE exercises.exerciseGroupId = (
        SELECT exerciseGroupId FROM exercises WHERE id = :exerciseId
    ) AND exercise_sets.reps = (
        SELECT MAX(reps) 
        FROM exercise_sets 
        INNER JOIN exercises ON exercise_sets.exercise_id = exercises.id
        WHERE exercises.exerciseGroupId = (
            SELECT exerciseGroupId FROM exercises WHERE id = :exerciseId
        )
    )
    LIMIT 1
"""
    )
    fun getMaxRepsDateForExerciseGroup(exerciseId: Long): LiveData<Long?>

    @Query(
        """
    SELECT MAX(total_reps)
    FROM (
        SELECT SUM(exercise_sets.reps) AS total_reps
        FROM exercise_sets
        INNER JOIN exercises ON exercise_sets.exercise_id = exercises.id
        WHERE exercises.exerciseGroupId = :exerciseGroupId
        GROUP BY exercise_sets.exercise_id
    )
"""
    )
    suspend fun getMaxTotalRepsForExerciseGroup(exerciseGroupId: Long): Int


    @Query(
        """
    SELECT exercises.date, sets_sum.total_reps, exercises.exerciseGroupId
    FROM exercises
    INNER JOIN (
        SELECT exercise_id, SUM(reps) as total_reps
        FROM exercise_sets
        GROUP BY exercise_id
    ) AS sets_sum
    ON exercises.id = sets_sum.exercise_id
    WHERE exercises.exerciseGroupId = (
        SELECT exerciseGroupId
        FROM exercises
        WHERE id = :exerciseId
    )
    ORDER BY sets_sum.total_reps DESC
    LIMIT 1
"""
    )
    fun getMaxRepsExercise(exerciseId: Long): LiveData<ExerciseMaxReps?>


    @Query(
        """
    SELECT exs.date, MAX(sets.weight * sets.reps) as max_volume
    FROM exercise_sets as sets 
    INNER JOIN exercises as exs ON sets.exercise_id = exs.id
    WHERE exs.exerciseGroupId = (
        SELECT exerciseGroupId 
        FROM exercises 
        WHERE id = :exerciseId
    )
    GROUP BY exs.date
    ORDER BY max_volume DESC
    LIMIT 1
"""
    )
    fun getMaxSetVolumeForGroup(exerciseId: Long): LiveData<ExerciseSetVolume?>

    @Query("SELECT * FROM exercises WHERE date = :date AND exerciseGroupId = :groupId LIMIT 1")
    suspend fun getExerciseByDateAndGroup(date: Long, groupId: Long): Exercise?


    @Query(
        """
    SELECT weight * (1 + 0.0333 * reps) as one_rep_max 
    FROM exercise_sets
    WHERE exercise_id = :exerciseId
    ORDER BY one_rep_max DESC
    LIMIT 1
    """
    )
    suspend fun calculateOneRepMax(exerciseId: Long): Double?


    @Query("""
    SELECT 
        E.date AS exerciseDate,
         MAX(ES.weight * (1 + 0.0333 * ES.reps)) AS oneRepMax
    FROM 
        exercise_sets AS ES
    JOIN 
        exercises AS E ON ES.exercise_id = E.id
    WHERE 
        E.exerciseGroupId = (
            SELECT exerciseGroupId FROM exercises WHERE id = :exerciseId
        )
""")
    suspend fun calculateGroupMaxOneRep(exerciseId: Long): MaxOneRepForExercise?

    @Query(
        """
    SELECT exs.date, SUM(sets.weight * sets.reps) as max_volume
    FROM exercise_sets as sets 
    INNER JOIN exercises as exs ON sets.exercise_id = exs.id
    WHERE exs.exerciseGroupId = (
        SELECT exerciseGroupId 
        FROM exercises 
        WHERE id = :exerciseId
    )
    GROUP BY exs.date
    ORDER BY max_volume DESC
    LIMIT 1
"""
    )
    fun getMaxExerciseVolumeForGroup(exerciseId: Long): LiveData<ExerciseSetVolume?>
    @Transaction
    @Query("""
SELECT 
    exercises.id AS exerciseId, 
    exercises.exerciseGroupId, 
    exercises.date AS exerciseDate, 
    exercise_groups.name AS groupName,
    COUNT(exercise_sets.id) AS setsCount
FROM 
    exercises 
JOIN 
    exercise_groups ON exercise_groups.id = exercises.exerciseGroupId 
LEFT JOIN 
    exercise_sets ON exercises.id = exercise_sets.exercise_id
GROUP BY 
    exercises.id
    ORDER BY 
    exercises.date DESC
""")
    fun getAllExerciseData(): Flow<List<ExerciseData>>
    @Query("SELECT * FROM exercises WHERE exerciseGroupId = :exerciseGroupId ORDER BY date DESC LIMIT 1")
    suspend fun getLastTraining(exerciseGroupId: Long): Exercise?


}


