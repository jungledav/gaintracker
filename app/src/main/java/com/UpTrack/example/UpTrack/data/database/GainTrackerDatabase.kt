package com.UpTrack.example.UpTrack.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.UpTrack.example.UpTrack.data.dao.ExerciseDao
import com.UpTrack.example.UpTrack.data.dao.ExerciseGroupDao
import com.UpTrack.example.UpTrack.data.dao.ExerciseSetDao
import com.UpTrack.example.UpTrack.data.dao.ExerciseHistoryDao
import com.UpTrack.example.UpTrack.data.models.Exercise
import com.UpTrack.example.UpTrack.data.models.ExerciseGroup
import com.UpTrack.example.UpTrack.data.models.ExerciseSet
import com.UpTrack.example.UpTrack.data.models.ExerciseHistory

@Database(entities = [Exercise::class, ExerciseSet::class, ExerciseHistory::class, ExerciseGroup::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
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
