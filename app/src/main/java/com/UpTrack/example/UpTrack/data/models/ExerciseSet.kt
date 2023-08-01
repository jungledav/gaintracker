package com.UpTrack.example.UpTrack.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exercise_id: Long,
    val date: Long = System.currentTimeMillis(),
    val reps: Int,
    val weight: Double
) {
    fun toCsvStringForBackup(): String {
        return "$id,$exercise_id,$date,$reps,$weight"
    }

    companion object {
        fun fromCsvStringFromBackup(csvString: String): ExerciseSet {
            val parts = csvString.split(",")
            return ExerciseSet(
                id = parts[0].toLong(),
                exercise_id = parts[1].toLong(),
                date = parts[2].toLong(),
                reps = parts[3].toInt(),
                weight = parts[4].toDouble()
            )
        }
    }
}
