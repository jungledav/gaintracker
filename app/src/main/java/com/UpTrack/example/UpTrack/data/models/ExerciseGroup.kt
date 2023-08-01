package com.UpTrack.example.UpTrack.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_groups")
data class ExerciseGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
){
    fun toCsvStringForBackup(): String {
        return "$id,$name"
    }

    companion object {
        fun fromCsvStringFromBackup(csvString: String): ExerciseGroup {
            val parts = csvString.split(",")
            return ExerciseGroup(
                id = parts[0].toLong(),
                name = parts[1]
            )
        }
    }

}
