package com.example.gaintracker.viewmodels

import androidx.lifecycle.*
import com.example.gaintracker.data.models.Exercise
import com.example.gaintracker.data.models.ExerciseSet
import com.example.gaintracker.data.models.ExerciseWithGroupName
import com.example.gaintracker.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    init {
        repository.viewModelScope = viewModelScope
    }

    val allExercises: LiveData<List<Exercise>> = repository.allExercises
    val allExerciseNames: LiveData<List<String>> = repository.allExerciseNames

    private val _setsForCurrentExercise = MutableLiveData<List<ExerciseSet>>(listOf())
    val setsForCurrentExercise: LiveData<List<ExerciseSet>>
        get() = _setsForCurrentExercise

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



    fun getExerciseHistory(exerciseGroupId: Int): LiveData<List<ExerciseSet>> {
        return repository.getExerciseHistory(exerciseGroupId)
    }

    fun getExerciseGroupId(exerciseId: Long): LiveData<Long> {
        return repository.getExerciseGroupId(exerciseId)
    }

    fun getExerciseGroupName(exerciseGroupId: Int): LiveData<String> {
        return repository.getExerciseGroupName(exerciseGroupId)
    }

    suspend fun getExerciseGroupNameById(id: Int): String {
        return repository.getExerciseGroupNameById(id)
    }

    val allExercisesWithGroupNames: LiveData<List<ExerciseWithGroupName>> =
        repository.getAllExercisesWithGroupNames()
    fun getSetsForExercise(exerciseId: Long): LiveData<List<ExerciseSet>> {
        return liveData {
            val sets = repository.getSetsForExercise(exerciseId)
            emit(sets)
        }.distinctUntilChanged()
    }
    fun getSetsForExerciseGroup(exerciseGroupId: Int): LiveData<List<ExerciseSet>> {
        return repository.getSetsForExerciseGroup(exerciseGroupId)
    }
    fun getMaxWeightForExercise(exerciseId: Long): LiveData<Float> {
        return repository.getMaxWeightForExercise(exerciseId)
    }
    fun getMaxWeightForExerciseType(exerciseTypeId: Long): LiveData<Float> {
        return repository.getMaxWeightForExerciseType(exerciseTypeId)
    }
}
