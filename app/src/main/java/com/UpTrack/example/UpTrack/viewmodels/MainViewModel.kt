package com.UpTrack.example.UpTrack.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.UpTrack.example.UpTrack.data.models.Exercise
import com.UpTrack.example.UpTrack.data.models.ExerciseData
import com.UpTrack.example.UpTrack.data.models.ExerciseGroup
import com.UpTrack.example.UpTrack.data.models.ExerciseMaxReps
import com.UpTrack.example.UpTrack.data.models.ExerciseSet
import com.UpTrack.example.UpTrack.data.models.ExerciseSetVolume
import com.UpTrack.example.UpTrack.data.models.ExerciseWithLastTraining
import com.UpTrack.example.UpTrack.repositories.MainRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class MainViewModel(private val repository: MainRepository) : ViewModel() {
    val isLoading = MutableLiveData(true)
    val lastTrainedDays = MutableLiveData<Int?>()
    val exercisesWithLastTraining = MutableLiveData<List<ExerciseWithLastTraining>>()

    init {
        repository.viewModelScope = viewModelScope
    }


    val allExerciseData: LiveData<List<ExerciseData>> = repository.getAllExerciseData().asLiveData()

    fun getSetsForExerciseFlow(exerciseId: Long): Flow<List<ExerciseSet>> {
        Log.d("MainViewModel", "getSetsForExerciseFlow called with ID: $exerciseId")

        return repository.getSetsForExercise(exerciseId).map { it.reversed() }
            .onEach { sets ->
                Log.d("MainViewModel", "Fetched ${sets.size} sets for exercise: $exerciseId")
            }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    suspend fun insertExercise(name: String): Long {
        return repository.insertExercise(name)
    }

    suspend fun insertExerciseWithDetails(exercise: Exercise): Long {
        val insertedId = repository.insertExerciseWithDetails(exercise)
        return insertedId
    }


    suspend fun insertExerciseSet(exerciseSet: ExerciseSet) {
        repository.insertExerciseSet(exerciseSet)
    }

    fun deleteExerciseSet(exerciseSet: ExerciseSet) {
        viewModelScope.launch {
            repository.deleteExerciseSet(exerciseSet)
        }
    }

    fun updateExerciseSet(exerciseSet: ExerciseSet) {
        viewModelScope.launch {
            repository.updateExerciseSet(exerciseSet)
        }
    }


    fun getExerciseGroupId(exerciseId: Long): LiveData<Long> {
        return repository.getExerciseGroupId(exerciseId)
    }

    fun getExerciseGroupName(exerciseGroupId: Int): LiveData<String> {
        return repository.getExerciseGroupName(exerciseGroupId)
    }

    suspend fun getExerciseGroupNameById(exerciseGroupId: Int): String {
        return repository.getExerciseGroupNameById(exerciseGroupId)
    }

    fun getMaxWeightForExerciseType(exerciseTypeId: Long): LiveData<Float> {
        return repository.getMaxWeightForExerciseType(exerciseTypeId)
    }


    suspend fun getLatestExercise(): Exercise? {
        return repository.getLatestExercise()
    }

    suspend fun countTotalWorkouts() = repository.countTotalWorkouts()

    suspend fun countTotalExercises() = repository.countTotalExercises()

    suspend fun countTotalSets() = repository.countTotalSets()

    suspend fun countTotalReps() = repository.countTotalReps()

    suspend fun countTotalWeight() = repository.countTotalWeight()


    fun getExerciseVolumeForExercise(exerciseId: Long): LiveData<Double> {
        return repository.getExerciseVolumeForExercise(exerciseId)
    }

    fun getMaxSetVolumeForExercise(exerciseId: Long): LiveData<Double> {
        return repository.getMaxSetVolumeForExercise(exerciseId)
    }

    private fun formatTimestamp(timestamp: Long?): String {
        if (timestamp != null) {
            val date = Date(timestamp)
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

            val day = dayFormat.format(date).toInt()
            val daySuffix = getDayOfMonthSuffix(day)
            val month = monthFormat.format(date)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

            return "$month $day$daySuffix, $year"
        } else {
            // Handle null timestamp here.
            val date = Date()
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

            val day = dayFormat.format(date).toInt()
            val daySuffix = getDayOfMonthSuffix(day)
            val month = monthFormat.format(date)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

            return "$month $day$daySuffix, $year"
        }
    }

    fun getMaxWeightDateForExercise(exerciseId: Long): LiveData<String> {
        val timestampLiveData: LiveData<Long?> = repository.getMaxWeightDateForExercise(exerciseId)
        return timestampLiveData.map { timestamp: Long? ->
            formatTimestamp(timestamp)
        }
    }

    suspend fun getExerciseGroupByName(name: String): ExerciseGroup? {
        return repository.getExerciseGroupByName(name)
    }

    suspend fun insertExerciseGroup(exerciseGroup: ExerciseGroup): Long {
        return repository.insertExerciseGroup(exerciseGroup)
    }

    fun getMaxRepsDateForExerciseGroup(exerciseId: Long): LiveData<String> {
        val timestampLiveData = repository.getMaxRepsDateForExerciseGroup(exerciseId)
        return timestampLiveData.map { timestamp ->
            formatTimestamp(timestamp)
        }
    }


    fun getDayOfMonthSuffix(n: Int): String {
        if (n in 11..13) {
            return "th"
        }
        return when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }


    fun getMaxRepsForExercise(exerciseId: Long): LiveData<Int> {
        return repository.getMaxRepsForExercise(exerciseId)
    }

    fun getMaxRepsForExerciseGroup(exerciseId: Long): LiveData<Int> {
        return repository.getMaxRepsForExerciseGroup(exerciseId)
    }


    fun getTodayMaxWeightForExercise(exerciseId: Long): LiveData<Double> {
        return repository.getWorkoutMaxWeightForExercise(exerciseId)
    }


    fun getTodayTotalRepsForExercise(exerciseId: Long): LiveData<Int> {
        return repository.getTotalRepsForExercise(exerciseId)
    }


    fun getMaxTotalRepsForExerciseGroup(exerciseId: Long): LiveData<ExerciseMaxReps> {
        val maxRepsLiveData = repository.getMaxRepsExercise(exerciseId)
            .map { it?.let { it } ?: ExerciseMaxReps("No History yet", 0, 0) }
        return maxRepsLiveData.map { maxRepsExercise ->
            // maxRepsExercise is guaranteed to be non-null at this point

            // Check if date is not empty before trying to convert it to a Long
            val formattedDate = if (maxRepsExercise.date != "No History yet") {
                val timestamp = maxRepsExercise.date.toLong()
                val date = Date(timestamp) // Create a Date from the timestamp

                val dayFormat = SimpleDateFormat("d", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

                val day = dayFormat.format(date).toInt()
                val daySuffix = getDayOfMonthSuffix(day)
                val month = monthFormat.format(date)
                val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

                "$month $day$daySuffix, $year"
            } else {
                "No History yet"
            }

            ExerciseMaxReps(
                formattedDate,
                maxRepsExercise.totalReps,
                maxRepsExercise.exerciseGroupId
            )
        }
    }

    fun doesExerciseSetExist(exerciseGroupId: Long): LiveData<Boolean> {
        return repository.doesExerciseSetExist(exerciseGroupId)
    }

    fun getMaxSetVolumeForGroup(exerciseId: Long): LiveData<ExerciseSetVolume> {
        val setVolumeLiveData = repository.getMaxSetVolumeForGroup(exerciseId)
        return setVolumeLiveData.map { setVolume ->
            if (setVolume != null && setVolume.date != null && setVolume.max_volume != null) {
                val date = Date(setVolume.date.toLong())
                val dayFormat = SimpleDateFormat("d", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

                val day = dayFormat.format(date).toInt()
                val daySuffix = getDayOfMonthSuffix(day)
                val month = monthFormat.format(date)
                val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

                val formattedDate = "$month $day$daySuffix, $year"
                ExerciseSetVolume(formattedDate, setVolume.max_volume)
            } else {
                // Handle null setVolume here
                // You could, for example, return a default ExerciseSetVolume
                ExerciseSetVolume("No History yet", 0.0)
            }
        }
    }

    fun getMaxExerciseVolumeForGroup(exerciseId: Long): LiveData<ExerciseSetVolume> {
        val setGroupexerciseVolumeLiveData = repository.getMaxExerciseVolumeForGroup(exerciseId)
        return setGroupexerciseVolumeLiveData.map { exerciseVolume ->
            if (exerciseVolume != null) {
                val date = Date(exerciseVolume.date.toLong())
                val dayFormat = SimpleDateFormat("d", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

                val day = dayFormat.format(date).toInt()
                val daySuffix = getDayOfMonthSuffix(day)
                val month = monthFormat.format(date)
                val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

                val formattedDate = "$month $day$daySuffix, $year"
                ExerciseSetVolume(formattedDate, exerciseVolume.max_volume)
            } else {
                // Handle null setVolume here
                // You could, for example, return a default ExerciseSetVolume
                ExerciseSetVolume("No History yet", 0.0)
            }
        }
    }

    suspend fun getExerciseByDateAndGroup(date: Long, groupId: Long): Exercise? {
        return repository.getExerciseByDateAndGroup(date, groupId)
    }

    private val _oneRepMax = MutableLiveData<Double?>()
    val oneRepMax: LiveData<Double?>
        get() = _oneRepMax

    fun calculateOneRepMax(exerciseId: Long) {
        viewModelScope.launch {
            _oneRepMax.value = repository.calculateOneRepMax(exerciseId)
        }
    }

    private val _groupMaxOneRep = MutableLiveData<Double?>()
    val groupMaxOneRep: LiveData<Double?>
        get() = _groupMaxOneRep

    private val _groupMaxOneRepDate = MutableLiveData<String?>()
    val groupMaxOneRepDate: LiveData<String?>
        get() = _groupMaxOneRepDate

    fun calculateGroupMaxOneRep(exerciseId: Long) {
        viewModelScope.launch {
            val maxOneRepForExercise = repository.calculateGroupMaxOneRep(exerciseId)
            maxOneRepForExercise?.let {
                _groupMaxOneRep.value = it.oneRepMax

                val date = Date(it.exerciseDate)
                val dayFormat = SimpleDateFormat("d", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

                val day = dayFormat.format(date).toInt()
                val daySuffix = getDayOfMonthSuffix(day)
                val month = monthFormat.format(date)
                val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

                _groupMaxOneRepDate.value = "$month $day$daySuffix, $year"
            } ?: run {
                _groupMaxOneRep.value = null
                _groupMaxOneRepDate.value = null
            }
        }
    }

    fun getSavedUnit(): String {
        return repository.getSavedUnit()
    }

    suspend fun getExerciseGroupIdByName(name: String): Long? {
        return repository.getExerciseGroupIdByName(name)
    }

    fun longToDate(timestamp: Long): Date {
        return Date(timestamp)
    }

    fun daysBetween(startDate: Date, endDate: Date): Int {
        val diffInMillis = endDate.time + TimeUnit.HOURS.toMillis(12) - startDate.time
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()
    }

    suspend fun getDaysSinceLastTrained(exerciseGroupId: Long): Int? {

        val lastTrainedTimestamp = repository.getLastTrainedDate(exerciseGroupId)
        return if (lastTrainedTimestamp != null) {
            daysBetween(longToDate(lastTrainedTimestamp), Date())
        } else {
            null
        }

    }

    fun loadDaysSinceLastTrained(exerciseNames: List<String>) {
        isLoading.value = true  // Indicate that data loading has started
        viewModelScope.launch {
            try {
                val exercisesWithLastTraining = exerciseNames.map { exerciseName ->
                    val exerciseGroupId = getExerciseGroupIdByName(exerciseName)
                    Log.d("MainviewModel", "groupID: $exerciseGroupId and exercisename:$exerciseName" )

                    val daysAgo = if (exerciseGroupId != null) {
                        getDaysSinceLastTrained(exerciseGroupId)
                    } else {
                        null
                    }
                    ExerciseWithLastTraining(exerciseName, daysAgo)
                }
                this@MainViewModel.exercisesWithLastTraining.value = exercisesWithLastTraining
            } catch (e: Exception) {
                // Handle exception...
            } finally {
                isLoading.value = false  // Indicate that data loading has ended
            }
        }
    }


}