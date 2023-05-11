package com.example.gaintracker.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.gaintracker.data.dao.*
import com.example.gaintracker.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.switchMap


class MainRepository(
    private val exerciseDao: ExerciseDao,
    private val exerciseSetDao: ExerciseSetDao,
    private val exerciseHistoryDao: ExerciseHistoryDao,
    private val exerciseGroupDao: ExerciseGroupDao
) {
    lateinit var viewModelScope: CoroutineScope

    val allExercises: LiveData<List<Exercise>> = exerciseDao.getAllExercises()

    val allExerciseSets: LiveData<List<ExerciseSet>> = exerciseSetDao.getAllExerciseSets()
    val allExerciseNames: LiveData<List<String>> = exerciseGroupDao.getAllExerciseGroupNames().map { exerciseGroups ->
        exerciseGroups.map { it.name }
    }


    suspend fun insertExercise(name: String): Long {
        // Check if an exercise group with the same name exists
        val existingExerciseGroup = exerciseGroupDao.getExerciseGroupByName(name)
        Log.d("MainRepository", "Existing Exercise Group: $existingExerciseGroup")

        // If it exists, use its ID; otherwise, create a new exercise group and use its ID
        val exerciseGroupId = existingExerciseGroup?.id ?: exerciseGroupDao.insertExerciseGroup(ExerciseGroup(name = name))

        // Create a new exercise using the exercise group ID and insert it into the database
        val exercise = Exercise(exerciseGroupId = exerciseGroupId)
        return exerciseDao.insert(exercise)
    }

    fun getExerciseGroupId(exerciseId: Long): LiveData<Long> {
        return exerciseDao.getExerciseGroupId(exerciseId)
    }

    suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.update(exercise)
    }

    suspend fun insertExerciseSet(exerciseSet: ExerciseSet) {
        exerciseSetDao.insert(exerciseSet)
    }

    suspend fun updateExerciseSet(exerciseSet: ExerciseSet) {
        exerciseSetDao.update(exerciseSet)
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise)
    }

    suspend fun getSetsForExercise(exerciseId: Long): List<ExerciseSet> {
        return exerciseSetDao.getSetsForExercise(exerciseId)
    }

    suspend fun deleteExerciseSet(exerciseSet: ExerciseSet) {
        exerciseSetDao.deleteExerciseSet(exerciseSet)
    }

    // Update this function to use the exerciseGroupId
    fun getExerciseHistory(exerciseGroupId: Int): LiveData<List<ExerciseSet>> {
        return exerciseDao.getSetsForExerciseGroup(exerciseGroupId)
    }

    suspend fun insertOrGetExerciseGroup(name: String): ExerciseGroup {
        var exerciseGroup = exerciseGroupDao.getExerciseGroupByName(name)
        if (exerciseGroup == null) {
            val exerciseGroupId = exerciseGroupDao.insertExerciseGroup(ExerciseGroup(name = name))
            exerciseGroup = ExerciseGroup(id = exerciseGroupId, name = name)
        }
        return exerciseGroup
    }
    suspend fun getAllExerciseGroups(): List<ExerciseGroup> {
        return exerciseGroupDao.getAllExerciseGroupNames().value ?: emptyList()
    }
    fun getExerciseGroupName(exerciseGroupId: Int): LiveData<String> {
        return exerciseGroupDao.getExerciseGroupName(exerciseGroupId)
    }
    suspend fun getExerciseGroupNameById(id: Int): String {
        return exerciseGroupDao.getExerciseGroupNameById(id)
    }
    fun getAllExercisesWithGroupNames(): LiveData<List<ExerciseWithGroupName>> {
        val exercisesLiveData = exerciseDao.getAllExercises()
        return exercisesLiveData.switchMap { exercises ->
            liveData(Dispatchers.IO) {
                val exerciseWithGroupNames = exercises.map { exercise ->
                    val groupName = getExerciseGroupNameById(exercise.exerciseGroupId.toInt())
                    ExerciseWithGroupName(exercise, groupName)
                }
                emit(exerciseWithGroupNames)
            }
        }
    }
    fun getSetsForExerciseGroup(exerciseGroupId: Int): LiveData<List<ExerciseSet>> {
        return exerciseDao.getSetsForExerciseGroup(exerciseGroupId)
    }
    fun getMaxWeightForExercise(exerciseId: Long): LiveData<Float> {
        return exerciseDao.getMaxWeightForExercise(exerciseId.toInt())
    }

    fun getMaxWeightForExerciseType(exerciseTypeId: Long): LiveData<Float> {
        return exerciseDao.getMaxWeightForExerciseType(exerciseTypeId)
    }

}
