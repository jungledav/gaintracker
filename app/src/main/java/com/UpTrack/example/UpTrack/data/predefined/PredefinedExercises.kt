package com.UpTrack.example.UpTrack.data.predefined

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable


object PredefinedExercises {
    @Serializable
    data class ExerciseInfo(val name: String, val equipment: String)
    data class MuscleGroup(val groupName: String, val exercises: List<ExerciseInfo>)
    private val customExercises = java.util.concurrent.ConcurrentHashMap<String, MutableList<ExerciseInfo>>()

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
        MuscleGroup("Forearms", listOf(
            ExerciseInfo("Wrist Curls", "Barbell"),
            ExerciseInfo("Reverse Wrist Curls", "Barbell"),
            ExerciseInfo("Hammer Curls", "Dumbbell"),
            ExerciseInfo("Zottman Curls", "Dumbbell"),
            ExerciseInfo("Behind-the-Back Wrist Curl", "Barbell"),
            ExerciseInfo("Plate Pinches", "Weight Plate")
        )),
        MuscleGroup("Abs", listOf(
            ExerciseInfo("Crunches", "Bodyweight"),
            ExerciseInfo("Plank", "Bodyweight"),
            ExerciseInfo("Hanging Leg Raise", "Bodyweight"),
            ExerciseInfo("Russian Twists", "Dumbbell"),
            ExerciseInfo("Sit-Ups", "Bodyweight"),
            ExerciseInfo("Leg Raises", "Bodyweight"),
            ExerciseInfo("Bicycle Crunches", "Bodyweight"),
            ExerciseInfo("Cable Crunch", "Machine")
        )),
        MuscleGroup("Gluteus", listOf(
            ExerciseInfo("Hip Thrust", "Barbell"),
            ExerciseInfo("Glute Bridge", "Barbell"),
            ExerciseInfo("Donkey Kicks", "Bodyweight"),
            ExerciseInfo("Cable Kickbacks", "Machine"),
            ExerciseInfo("Step Ups", "Dumbbell"),
            ExerciseInfo("Bulgarian Split Squats", "Dumbbell"),
            ExerciseInfo("Box Squats", "Barbell"),
            ExerciseInfo("Romanian Deadlift", "Barbell")
        )),
        MuscleGroup("Legs", listOf(
            ExerciseInfo("Squats", "Barbell"),
            ExerciseInfo("Front Squats", "Barbell"),
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
        return (predefinedExerciseNames + customExerciseNames).sorted() + "+ Add your own exercise"
    }

    fun getExerciseInfo(name: String): ExerciseInfo? {
        return predefinedExercises
            .flatMap { it.exercises }
            .find { it.name == name } ?: customExercises.flatMap { it.value }.find { it.name == name }
    }
    fun setCustomExercises(customExercisesMap: MutableMap<String, MutableList<ExerciseInfo>>) {
        synchronized(customExercises) {
            customExercises.clear()
            customExercises.putAll(customExercisesMap)
        }
        Log.d("PredefinedExercises", "Custom exercises set at: ${System.currentTimeMillis()}")

    }

    private fun saveCustomExercises(context: Context) {
        val sharedPreferences = context.getSharedPreferences("custom_exercises", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Json.encodeToString(customExercises)
        editor.putString("custom_exercises_data", json)
        editor.apply()
    }
    fun addCustomExercise(context: Context, muscleGroupName: String, exerciseName: String, equipmentType: String) {
        synchronized(customExercises) {
            if (customExercises[muscleGroupName] == null) {
                customExercises[muscleGroupName] = mutableListOf()
            }
            customExercises[muscleGroupName]!!.add(ExerciseInfo(name = exerciseName, equipment = equipmentType))
        }
        saveCustomExercises(context) // Assuming this is also thread-safe
    }
    fun findMuscleGroupByExerciseName(exerciseName: String): String? {
        // Check in predefined exercises
        Log.d("PredefinedExercises", "Finding muscle group for exercise $exerciseName at: ${System.currentTimeMillis()}")

        for (muscleGroup in predefinedExercises) {
            if (muscleGroup.exercises.any { it.name == exerciseName }) {
                return muscleGroup.groupName
            }

        }

        // Check in custom exercises
        for ((muscleGroupName, exercises) in customExercises) {
            if (exercises.any { it.name == exerciseName }) {
                return muscleGroupName
            }
        }

        return null // return null if exercise name doesn't match any predefined or custom exercise

    }



}