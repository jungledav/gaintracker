package com.UpTrack.example.UpTrack.data.models

import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Serializable
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseGroup::class,
            parentColumns = ["id"],
            childColumns = ["exerciseGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exerciseGroupId: Long,
    val date: Long = System.currentTimeMillis()
) {
    fun toCsvStringForBackup(): String {
        return "$id,$exerciseGroupId,$date"
    }

    companion object {
        fun fromCsvStringFromBackup(csvString: String): Exercise {
            val parts = csvString.split(",")
            return Exercise(
                id = parts[0].toInt(),
                exerciseGroupId = parts[1].toLong(),
                date = parts[2].toLong()
            )
        }
    }
}
