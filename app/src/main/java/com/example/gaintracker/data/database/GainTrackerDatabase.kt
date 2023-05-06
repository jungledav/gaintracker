package com.example.gaintracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gaintracker.data.dao.ExerciseDao
import com.example.gaintracker.data.dao.ExerciseGroupDao
import com.example.gaintracker.data.dao.ExerciseSetDao
import com.example.gaintracker.data.dao.ExerciseHistoryDao
import com.example.gaintracker.data.models.Exercise
import com.example.gaintracker.data.models.ExerciseGroup
import com.example.gaintracker.data.models.ExerciseSet
import com.example.gaintracker.data.models.ExerciseHistory

@Database(entities = [Exercise::class, ExerciseSet::class, ExerciseHistory::class, ExerciseGroup::class], version = 3, exportSchema = false)
abstract class GainTrackerDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun exerciseHistoryDao(): ExerciseHistoryDao
    abstract fun exerciseGroupDao(): ExerciseGroupDao
    companion object {
        @Volatile
        private var INSTANCE: GainTrackerDatabase? = null

        fun getDatabase(context: Context): GainTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GainTrackerDatabase::class.java,
                    "gain_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
