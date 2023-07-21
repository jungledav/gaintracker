package com.UpTrack.example.UpTrack

import android.content.Context
import com.UpTrack.example.UpTrack.data.dao.ExerciseDao
import com.UpTrack.example.UpTrack.data.dao.ExerciseGroupDao
import com.UpTrack.example.UpTrack.data.dao.ExerciseSetDao
import com.UpTrack.example.UpTrack.data.database.GainTrackerDatabase
import com.UpTrack.example.UpTrack.repositories.MainRepository
import com.UpTrack.example.UpTrack.viewmodels.MainViewModelFactory

class AppContainer(private val context: Context) {
    private val gainTrackerDatabase = GainTrackerDatabase.getDatabase(context)
    private val exerciseDao: ExerciseDao = gainTrackerDatabase.exerciseDao()
    private val exerciseSetDao: ExerciseSetDao = gainTrackerDatabase.exerciseSetDao()
    private val exerciseGroupDao: ExerciseGroupDao = gainTrackerDatabase.exerciseGroupDao()
    val mainRepository = MainRepository(exerciseDao, exerciseSetDao, exerciseGroupDao, context)

    val mainViewModelFactory = MainViewModelFactory(mainRepository)
}
