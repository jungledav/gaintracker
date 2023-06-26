package com.example.gaintracker.data.predefined

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable


object PredefinedExercises {
    @Serializable
    data class ExerciseInfo(val name: String, val equipment: String)
    data class MuscleGroup(val groupName: String, val exercises: List<ExerciseInfo>)
    private val customExercises = mutableMapOf<String, MutableList<ExerciseInfo>>()

    val predefinedExercises: List<MuscleGroup> = listOf(
        MuscleGroup("Chest", listOf(
            ExerciseInfo("Bench Press", "Barbell"),
            ExerciseInfo("Cable Crossover", "Machine"),
            ExerciseInfo("Chest Press Machine", "Machine"),
            ExerciseInfo("Decline Bench Press", "Barbell"),
            ExerciseInfo("Dumbbell Flyes", "Dumbbell"),
            ExerciseInfo("Dumbbell Pullovers", "Dumbbell"),
            ExerciseInfo("Incline Bench Press", "Barbell"),
            ExerciseInfo("Push-ups", "Bodyweight")
        )),
        MuscleGroup("Back", listOf(
            ExerciseInfo("Bent-over Row", "Barbell"),
            ExerciseInfo("Chin-ups", "Bodyweight"),
            ExerciseInfo("Deadlift", "Barbell"),
            ExerciseInfo("Dumbbell Row", "Dumbbell"),
            ExerciseInfo("Lat Pulldown", "Machine"),
            ExerciseInfo("Pull-ups", "Bodyweight"),
            ExerciseInfo("Seated Cable Row", "Machine"),
          ExerciseInfo("T-Bar Row", "Barbell")
        )),
        MuscleGroup("Shoulders", listOf(
            ExerciseInfo("Shoulder Press", "Barbell"),
            ExerciseInfo("Dumbbell Shoulder Press", "Dumbbell"),
            ExerciseInfo("Lateral Raise", "Dumbbell"),
            ExerciseInfo("Front Raise", "Dumbbell"),
            ExerciseInfo("Rear Delt Machine Fly", "Machine"),
            ExerciseInfo("Cable Cross Pull", "Machine"),
            ExerciseInfo("Upright Row", "Barbell"),
            ExerciseInfo("Face Pull", "Machine"),
            ExerciseInfo("Machine Shoulder Press", "Machine")
        )),
        MuscleGroup("Biceps", listOf(
            ExerciseInfo("Barbell Curl", "Barbell"),
            ExerciseInfo("Dumbbell Curl", "Dumbbell"),
            ExerciseInfo("Hammer Curl", "Dumbbell"),
            ExerciseInfo("Preacher Curl", "Barbell"),
            ExerciseInfo("Concentration Curl", "Dumbbell"),
            ExerciseInfo("Cable Curl", "Machine"),
            ExerciseInfo("Seated Machine Curl", "Machine")
        )),
        MuscleGroup("Triceps", listOf(
            ExerciseInfo("Triceps Dips", "Bodyweight"),
            ExerciseInfo("Close Grip Bench Press", "Barbell"),
            ExerciseInfo("Skull Crushers", "Barbell"),
            ExerciseInfo("Triceps Push down", "Machine"),
            ExerciseInfo("Dumbbell Overhead Triceps Extension", "Dumbbell"),
            ExerciseInfo("Kickbacks", "Dumbbell"),
            ExerciseInfo("Machine Triceps Extension", "Machine")
        )),
        MuscleGroup("Legs", listOf(
            ExerciseInfo("Squats", "Barbell"),
            ExerciseInfo("Seated Leg Press", "Machine"),
            ExerciseInfo("Lunges", "Dumbbell"),
            ExerciseInfo("Dead lift", "Barbell"),
            ExerciseInfo("Leg Extension Machine", "Machine"),
            ExerciseInfo("Leg Curl", "Machine"),
            ExerciseInfo("Calf Raises", "Dumbbell"),
            ExerciseInfo("Seated Calf Raises", "Machine")
        )),

    )
    fun getMuscleGroupNames(): List<String> {
        return predefinedExercises.map { it.groupName }.distinct()
    }
    fun getExerciseNamesForMuscleGroup(muscleGroup: String): List<String> {
        val predefinedExerciseNames = predefinedExercises
            .filter { it.groupName == muscleGroup }
            .flatMap { it.exercises.map { exerciseInfo -> exerciseInfo.name } }
        val customExerciseNames = customExercises[muscleGroup]?.map { it.name } ?: emptyList()
        return (predefinedExerciseNames + customExerciseNames).sorted() + "Add your own exercise"
    }

    fun getExerciseInfo(name: String): ExerciseInfo? {
        return predefinedExercises
            .flatMap { it.exercises }
            .find { it.name == name } ?: customExercises.flatMap { it.value }.find { it.name == name }
    }
    fun setCustomExercises(customExercisesMap: MutableMap<String, MutableList<ExerciseInfo>>) {
        customExercises.clear()
        customExercises.putAll(customExercisesMap)
    }

    private fun saveCustomExercises(context: Context) {
        val sharedPreferences = context.getSharedPreferences("custom_exercises", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Json.encodeToString(customExercises)
        editor.putString("custom_exercises_data", json)
        editor.apply()
    }
    fun addCustomExercise(context: Context, muscleGroupName: String, exerciseName: String, equipmentType: String) {
        if (customExercises[muscleGroupName] == null) {
            customExercises[muscleGroupName] = mutableListOf()
        }
        customExercises[muscleGroupName]!!.add(ExerciseInfo(name = exerciseName, equipment = equipmentType))

        // Pass the context when calling saveCustomExercises.
        saveCustomExercises(context)
    }


}