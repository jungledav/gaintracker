package com.example.gaintracker.viewmodels

import androidx.lifecycle.*
import com.example.gaintracker.data.models.Exercise
import com.example.gaintracker.data.models.ExerciseMaxReps
import com.example.gaintracker.data.models.ExerciseSet
import com.example.gaintracker.data.models.ExerciseSetVolume
import com.example.gaintracker.data.models.ExerciseWithGroupName
import com.example.gaintracker.repositories.MainRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainViewModel(private val repository: MainRepository) : ViewModel() {

    init {
        repository.viewModelScope = viewModelScope
    }


    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    suspend fun insertExercise(name: String): Long {
        return repository.insertExercise(name)
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

    val allExercisesWithGroupNames: LiveData<List<ExerciseWithGroupName>> =
        repository.getAllExercisesWithGroupNames()

    fun getSetsForExercise(exerciseId: Long): LiveData<List<ExerciseSet>> {
        return liveData {
            val sets = repository.getSetsForExercise(exerciseId)
            emit(sets)
        }.distinctUntilChanged()
    }

    fun getMaxWeightForExercise(exerciseId: Long): LiveData<Float> {
        return repository.getMaxWeightForExercise(exerciseId)
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

    fun getMaxRepForExercise(exerciseId: Long): LiveData<Int> {
        return repository.getMaxRepForExercise(exerciseId)
    }

    fun getTotalRepsForExercise(exerciseId: Long): LiveData<Int> {
        return repository.getTotalRepsForExercise(exerciseId)
    }

    fun getExerciseVolumeForExercise(exerciseId: Long): LiveData<Double> {
        return repository.getExerciseVolumeForExercise(exerciseId)
    }

    fun getMaxSetVolumeForExercise(exerciseId: Long): LiveData<Double> {
        return repository.getMaxSetVolumeForExercise(exerciseId)
    }


    fun getMaxWeightDateForExercise(exerciseId: Long): LiveData<String> {
        val timestampLiveData = repository.getMaxWeightDateForExercise(exerciseId)
        return timestampLiveData.map { timestamp ->
            val date = Date(timestamp)
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

            val day = dayFormat.format(date).toInt()
            val daySuffix = getDayOfMonthSuffix(day)
            val month = monthFormat.format(date)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

            "$month $day$daySuffix, $year"
        }
    }

    fun getMaxRepsDateForExerciseGroup(exerciseId: Long): LiveData<String> {
        val timestampLiveData = repository.getMaxRepsDateForExerciseGroup(exerciseId)
        return timestampLiveData.map { timestamp ->
            val date = Date(timestamp)
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

            val day = dayFormat.format(date).toInt()
            val daySuffix = getDayOfMonthSuffix(day)
            val month = monthFormat.format(date)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

            "$month $day$daySuffix, $year"
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
        return maxRepsLiveData.map { maxRepsExercise ->
            val timestamp = maxRepsExercise.date.toLong() // Convert string to Long
            val date = Date(timestamp) // Create a Date from the timestamp

            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

            val day = dayFormat.format(date).toInt()
            val daySuffix = getDayOfMonthSuffix(day)
            val month = monthFormat.format(date)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

            val formattedDate = "$month $day$daySuffix, $year"
            ExerciseMaxReps(formattedDate, maxRepsExercise.totalReps, maxRepsExercise.exerciseGroupId)
        }
    }




    fun getMaxSetVolumeForGroup(exerciseId: Long): LiveData<ExerciseSetVolume> {
        val setVolumeLiveData = repository.getMaxSetVolumeForGroup(exerciseId)
        return setVolumeLiveData.map { setVolume ->
            val date = Date(setVolume.date.toLong()) // Corrected line
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

            val day = dayFormat.format(date).toInt()
            val daySuffix = getDayOfMonthSuffix(day)
            val month = monthFormat.format(date)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

            val formattedDate = "$month $day$daySuffix, $year"
            ExerciseSetVolume(formattedDate, setVolume.max_volume)
        }
    }





}
