package com.example.gaintracker.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val exerciseGroupDao: ExerciseGroupDao
) {
    lateinit var viewModelScope: CoroutineScope

    val allExercises: LiveData<List<Exercise>> = exerciseDao.getAllExercises()

    val allExerciseSets: LiveData<List<ExerciseSet>> = exerciseSetDao.getAllExerciseSets()
    val allExerciseNames: LiveData<List<String>> =
        exerciseGroupDao.getAllExerciseGroupNames().map { exerciseGroups ->
            exerciseGroups.map { it.name }
        }

    suspend fun getExerciseGroupByName(name: String): ExerciseGroup? {
        return exerciseGroupDao.getExerciseGroupByName(name)
    }
    suspend fun insertExerciseGroup(exerciseGroup: ExerciseGroup): Long {
        return exerciseGroupDao.insertExerciseGroup(exerciseGroup)
    }
    suspend fun insertExercise(name: String): Long {
        // Check if an exercise group with the same name exists
        val existingExerciseGroup = exerciseGroupDao.getExerciseGroupByName(name)
        Log.d("MainRepository", "Existing Exercise Group: $existingExerciseGroup")

        // If it exists, use its ID; otherwise, create a new exercise group and use its ID
        val exerciseGroupId = existingExerciseGroup?.id ?: exerciseGroupDao.insertExerciseGroup(
            ExerciseGroup(name = name)
        )

        // Create a new exercise using the exercise group ID and insert it into the database
        val exercise = Exercise(exerciseGroupId = exerciseGroupId)
        return exerciseDao.insert(exercise)
    }
    suspend fun insertExerciseWithDetails(exercise: Exercise): Long {
        return exerciseDao.insert(exercise)
    }

    fun getExerciseGroupId(exerciseId: Long): LiveData<Long> {
        val liveData = exerciseDao.getExerciseGroupId(exerciseId)
        return if (liveData != null) {
            liveData.map { it ?: 0L }
        } else {
            MutableLiveData(0L)
        }
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

    suspend fun getLatestExercise(): Exercise? {
        return exerciseDao.getLatestExercise()
    }

    suspend fun countTotalWorkouts() = exerciseDao.countTotalWorkouts()

    suspend fun countTotalExercises() = exerciseDao.countTotalExercises()

    suspend fun countTotalSets() = exerciseDao.countTotalSets()

    suspend fun countTotalReps() = exerciseDao.countTotalReps()

    suspend fun countTotalWeight() = exerciseDao.countTotalWeight()

    fun getMaxRepForExercise(exerciseId: Long): LiveData<Int> {
        return exerciseDao.getMaxRepForExercise(exerciseId)
    }

    fun getTotalRepsForExercise(exerciseId: Long): LiveData<Int> {
        return exerciseDao.getTotalRepsForExercise(exerciseId)
    }

    fun getExerciseVolumeForExercise(exerciseId: Long): LiveData<Double> {
        return exerciseDao.getExerciseVolumeForExercise(exerciseId)
    }

    fun getMaxSetVolumeForExercise(exerciseId: Long): LiveData<Double> {
        return exerciseDao.getMaxSetVolumeForExercise(exerciseId)
    }


    fun getMaxWeightDateForExercise(exerciseId: Long): LiveData<Long?> {
        return exerciseDao.getMaxWeightDateForExercise(exerciseId)
    }


    fun getWorkoutMaxWeightForExercise(exerciseId: Long): LiveData<Double> {
        return exerciseDao.getTodayMaxWeightForExercise(exerciseId)
    }

    fun getMaxRepsForExercise(exerciseId: Long): LiveData<Int> {
        return exerciseDao.getMaxRepsForExercise(exerciseId)
    }

    fun getMaxRepsDateForExercise(exerciseId: Long): LiveData<Long> {
        return exerciseDao.getMaxRepsDateForExercise(exerciseId)
    }

    fun getMaxRepsForExerciseGroup(exerciseId: Long): LiveData<Int> {
        return exerciseDao.getMaxRepsForExerciseGroup(exerciseId)
    }

    fun getMaxRepsDateForExerciseGroup(exerciseId: Long): LiveData<Long?> {
        return exerciseDao.getMaxRepsDateForExerciseGroup(exerciseId)
    }

    fun getMaxRepsExercise(exerciseId: Long): LiveData<ExerciseMaxReps?> {
        return exerciseDao.getMaxRepsExercise(exerciseId)
    }
    fun getMaxSetVolumeForGroup(exerciseId: Long): LiveData<ExerciseSetVolume?> {
        return exerciseDao.getMaxSetVolumeForGroup(exerciseId)
    }
    fun doesExerciseSetExist(exerciseGroupId: Long): LiveData<Boolean>{
        return exerciseSetDao.doesExerciseSetExist(exerciseGroupId)
    }

    suspend fun getExerciseByDateAndGroup(date: Long, groupId: Long): Exercise? {
        return exerciseDao.getExerciseByDateAndGroup(date, groupId)
    }

    suspend fun calculateOneRepMax(exerciseId: Long): Double? {
        return exerciseDao.calculateOneRepMax(exerciseId)
    }
    suspend fun calculateGroupMaxOneRep(exerciseId: Long): MaxOneRepForExercise? {
        return exerciseDao.calculateGroupMaxOneRep(exerciseId)
    }

    fun getMaxExerciseVolumeForGroup(exerciseId: Long): LiveData<ExerciseSetVolume?> {
        return exerciseDao.getMaxExerciseVolumeForGroup(exerciseId)
    }
}
