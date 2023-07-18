package com.UpTrack.example.UpTrack.data.database

import androidx.room.TypeConverter
import com.UpTrack.example.UpTrack.data.models.ExerciseSet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromString(value: String): List<ExerciseSet> {
        val listType = object : TypeToken<List<ExerciseSet>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<ExerciseSet>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}