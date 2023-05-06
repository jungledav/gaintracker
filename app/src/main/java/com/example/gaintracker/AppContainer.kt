package com.example.gaintracker

import android.content.Context
import com.example.gaintracker.data.dao.ExerciseDao
import com.example.gaintracker.data.dao.ExerciseGroupDao
import com.example.gaintracker.data.dao.ExerciseSetDao
import com.example.gaintracker.data.dao.ExerciseHistoryDao
import com.example.gaintracker.data.database.GainTrackerDatabase
import com.example.gaintracker.repositories.MainRepository
import com.example.gaintracker.viewmodels.MainViewModelFactory

class AppContainer(context: Context) {
    private val gainTrackerDatabase = GainTrackerDatabase.getDatabase(context)
    private val exerciseDao: ExerciseDao = gainTrackerDatabase.exerciseDao()
    private val exerciseSetDao: ExerciseSetDao = gainTrackerDatabase.exerciseSetDao()
    private val exerciseHistoryDao: ExerciseHistoryDao = gainTrackerDatabase.exerciseHistoryDao()
    private val exerciseGroupDao: ExerciseGroupDao = gainTrackerDatabase.exerciseGroupDao()
    val mainRepository = MainRepository(exerciseDao, exerciseSetDao, exerciseHistoryDao, exerciseGroupDao)

    val mainViewModelFactory = MainViewModelFactory(mainRepository)
}
