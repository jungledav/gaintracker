package com.UpTrack.example.UpTrack.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.UpTrack.example.UpTrack.data.models.ExerciseGroup
import kotlinx.coroutines.flow.Flow





@Dao
interface ExerciseGroupDao {

    @Insert
    suspend fun insertExerciseGroup(exerciseGroup: ExerciseGroup): Long

    @Query("SELECT * FROM exercise_groups WHERE name = :name LIMIT 1")
    suspend fun getExerciseGroupByName(name: String): ExerciseGroup?

    @Query("SELECT * FROM exercise_groups")
    fun getAllExerciseGroupNames(): LiveData<List<ExerciseGroup>>

    @Query("SELECT name FROM exercise_groups WHERE id = :exerciseGroupId")
    fun getExerciseGroupName(exerciseGroupId: Int): LiveData<String>

    @Query("SELECT name FROM exercise_groups WHERE id = :id")
    suspend fun getExerciseGroupNameById(id: Int): String

    @Query("SELECT id FROM exercise_groups WHERE name = :name LIMIT 1")
    suspend fun getExerciseGroupIdByName(name: String): Long?
    @Query("SELECT * FROM exercise_groups")
    fun getAllExerciseGroupsForBackup(): List<ExerciseGroup>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreAllExerciseGroupsFromBackup(exerciseGroups: List<ExerciseGroup>)
}
