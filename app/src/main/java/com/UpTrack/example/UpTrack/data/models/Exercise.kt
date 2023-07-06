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
    val date: Long = System.currentTimeMillis(),
)
