package com.UpTrack.example.UpTrack.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.UpTrack.example.UpTrack.data.dao.*
import com.UpTrack.example.UpTrack.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date


class MainRepository(
    private val exerciseDao: ExerciseDao,
    private val exerciseSetDao: ExerciseSetDao,
    private val exerciseGroupDao: ExerciseGroupDao,
    private val context: Context

) {
    lateinit var viewModelScope: CoroutineScope


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



    suspend fun insertExerciseSet(exerciseSet: ExerciseSet) {
        exerciseSetDao.insert(exerciseSet)
    }

    suspend fun updateExerciseSet(exerciseSet: ExerciseSet) {
        exerciseSetDao.update(exerciseSet)
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise)
    }

    fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>> {
        return exerciseSetDao.getSetsForExercise(exerciseId)
    }

    suspend fun deleteExerciseSet(exerciseSet: ExerciseSet) {
        exerciseSetDao.deleteExerciseSet(exerciseSet)
    }


    fun getExerciseGroupName(exerciseGroupId: Int): LiveData<String> {
        return exerciseGroupDao.getExerciseGroupName(exerciseGroupId)
    }

    suspend fun getExerciseGroupNameById(id: Int): String {
        return exerciseGroupDao.getExerciseGroupNameById(id)
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
    suspend fun getMaxOneRepForLastThreeMonths(exerciseId: Long): List<MaxOneRepForExercise> {
        val endDate = Date().time  // Now it's a long timestamp
        val calendar = Calendar.getInstance().apply {
            timeInMillis = endDate
            add(Calendar.MONTH, -3)
        }
        val startDate = calendar.timeInMillis
        return exerciseDao.calculateMaxOneRepForLastThreeMonths(exerciseId, startDate, endDate)
    }

    fun getMaxWeightForLastThreeMonths(exerciseId: Long): LiveData<List<MaxWeightForExercise>> {
        val endDate = Date().time // current timestamp
        val calendar = Calendar.getInstance().apply {
            timeInMillis = endDate
            add(Calendar.MONTH, -3)
        }
        val startDate = calendar.timeInMillis
        return liveData {
            emit(exerciseDao.getMaxWeightForLast30Days(exerciseId, startDate, endDate))
        }
    }
    suspend fun getMaxRepsOneSetForLastThreeMonths(exerciseId: Long): List<MaxRepsForExercise>? {
        val endDate = Date().time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = endDate
            add(Calendar.MONTH, -3)
        }
        val startDate = calendar.timeInMillis

        return exerciseDao.getMaxRepsOneSetForLastThreeMonths(exerciseId, startDate, endDate)
    }

    suspend fun getTotalRepsForLastThreeMonths(exerciseId: Long): List<TotalRepsForExercise>? {
        val endDate = Date().time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = endDate
            add(Calendar.MONTH, -3)
        }
        val startDate = calendar.timeInMillis

        return exerciseDao.getTotalRepsForLastThreeMonths(exerciseId, startDate, endDate)
    }




    fun getMaxExerciseVolumeForGroup(exerciseId: Long): LiveData<ExerciseSetVolume?> {
        return exerciseDao.getMaxExerciseVolumeForGroup(exerciseId)
    }
    fun getAllExerciseData(): Flow<List<ExerciseData>> {
        return exerciseDao.getAllExerciseData()
    }
    fun getSavedUnit(): String {
        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val defaultUnit = "kg"
        return sharedPref.getString("unit_key", defaultUnit) ?: defaultUnit
    }
    suspend fun getExerciseGroupIdByName(name: String): Long? {
        Log.d("Mainrepository", "exercisename: $name")
        return exerciseGroupDao.getExerciseGroupIdByName(name)
    }
    suspend fun getLastTrainedDate(exerciseGroupId: Long): Long? {
        val lastTraining = exerciseDao.getLastTraining(exerciseGroupId)
        return lastTraining?.date
    }

    suspend fun getAllExercisesForBackup(): List<Exercise> {
        return exerciseDao.getAllExercisesForBackup()
    }

    suspend fun insertAllExercisesFromBackup(exercises: List<Exercise>) {
        return exerciseDao.restoreAllExercisesFromBackup(exercises)
    }
    suspend fun getAllExerciseSetsForBackup(): List<ExerciseSet> {
        return exerciseSetDao.getAllExerciseSetsForBackup()
    }

    suspend fun insertAllExerciseSetsFromBackup(exerciseSets: List<ExerciseSet>) {
        return exerciseSetDao.restoreAllExerciseSetsFromBackup(exerciseSets)
    }

    suspend fun getAllExerciseGroupsForBackup(): List<ExerciseGroup> {
        return exerciseGroupDao.getAllExerciseGroupsForBackup()
    }

    suspend fun insertAllExerciseGroupsFromBackup(exerciseGroups: List<ExerciseGroup>) {
        return exerciseGroupDao.restoreAllExerciseGroupsFromBackup(exerciseGroups)
    }
    suspend fun getMaxSetVolumeForLastThreeMonths(exerciseId: Long): List<MaxSetVolumeForExercise>? {
        val endDate = Date().time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = endDate
            add(Calendar.MONTH, -3)
        }
        val startDate = calendar.timeInMillis

        return exerciseDao.getMaxSetVolumeForLastThreeMonths(exerciseId, startDate, endDate)
    }
    suspend fun getTotalWorkoutVolumeForLastThreeMonths(exerciseId: Long): List<TotalWorkoutVolumeForExercise>? {
        val endDate = Date().time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = endDate
            add(Calendar.MONTH, -3)
        }
        val startDate = calendar.timeInMillis

        return exerciseDao.getTotalWorkoutVolumeForLastThreeMonths(exerciseId, startDate, endDate)
    }

}
