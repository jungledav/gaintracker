package com.UpTrack.example.UpTrack.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.UpTrack.example.UpTrack.R
import com.UpTrack.example.UpTrack.data.models.Exercise
import com.UpTrack.example.UpTrack.data.models.ExerciseGroup
import com.UpTrack.example.UpTrack.data.models.ExerciseSet
import com.UpTrack.example.UpTrack.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class SettingsDialogFragment : DialogFragment() {
    private lateinit var viewModel: MainViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException("Activity cannot be null")
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_settings, null)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group)
        val radioKg = view.findViewById<RadioButton>(R.id.radio_kg)
        val radioLbs = view.findViewById<RadioButton>(R.id.radio_lbs)
        val backupButton =
            view.findViewById<Button>(R.id.backupButton)  // Replace with your actual ID
        val restoreButton =
            view.findViewById<Button>(R.id.restoreButton)  // Replace with your actual ID

        val sharedPref =
            activity.applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val defaultUnit = "kg"
        val savedUnit = sharedPref.getString("unit_key", defaultUnit)

        if (savedUnit == "kg") {
            radioKg.isChecked = true
        } else {
            radioLbs.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val unit = if (checkedId == R.id.radio_kg) "kg" else "lbs"
            with(sharedPref.edit()) {
                putString("unit_key", unit)
                apply()
            }
        }
        backupButton.setOnClickListener {
            // Backup data to CSV
            CoroutineScope(Dispatchers.IO).launch {
                backupData()
            }
        }

        restoreButton.setOnClickListener {
            // Restore data from CSV
            CoroutineScope(Dispatchers.IO).launch {
                restoreData()
            }
        }
        builder.setView(view)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id ->
                    // Dismiss the dialog
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })

        return builder.create()
    }

    private suspend fun backupData() = withContext(Dispatchers.IO) {
        val folder = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        if (folder != null) {
            withContext(Dispatchers.IO) {
                try {
                    if (!folder.exists()) {
                        folder.mkdir()
                    }

                    val exercisesCsvFile = File(folder, "exercises_backup.csv")
                    val exerciseSetsCsvFile = File(folder, "exercise_sets_backup.csv")
                    val exerciseGroupsCsvFile = File(folder, "exercise_groups_backup.csv")

                    val exerciseFileWriter = FileWriter(exercisesCsvFile)
                    val exerciseSetFileWriter = FileWriter(exerciseSetsCsvFile)
                    val exerciseGroupFileWriter = FileWriter(exerciseGroupsCsvFile)

                    val exercises = viewModel.getAllExercisesForBackup()
                    val exerciseSets = viewModel.getAllExerciseSetsForBackup()
                    val exerciseGroups = viewModel.getAllExerciseGroupsForBackup()

                    exercises.forEach { exercise ->
                        exerciseFileWriter.append(exercise.toCsvStringForBackup())
                        exerciseFileWriter.append('\n')
                    }

                    exerciseSets.forEach { exerciseSet ->
                        exerciseSetFileWriter.append(exerciseSet.toCsvStringForBackup())
                        exerciseSetFileWriter.append('\n')
                    }

                    exerciseGroups.forEach { exerciseGroup ->
                        exerciseGroupFileWriter.append(exerciseGroup.toCsvStringForBackup())
                        exerciseGroupFileWriter.append('\n')
                    }

                    exerciseFileWriter.flush()
                    exerciseFileWriter.close()

                    exerciseSetFileWriter.flush()
                    exerciseSetFileWriter.close()

                    exerciseGroupFileWriter.flush()
                    exerciseGroupFileWriter.close()
                } catch (e: Exception) {
                    // Handle the exception
                    // Log any exceptions that are thrown
                    Log.e("Backup", "Exception during backup", e)
                }
            }
        }
    }

    private suspend fun restoreData() = withContext(Dispatchers.IO) {
        val folder = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        if (folder != null) {
            try {
                val exercisesCsvFile = File(folder, "exercises_backup.csv")
                val exerciseSetsCsvFile = File(folder, "exercise_sets_backup.csv")
                val exerciseGroupsCsvFile = File(folder, "exercise_groups_backup.csv")

                if (exerciseGroupsCsvFile.exists()) {
                    val exerciseGroups = mutableListOf<ExerciseGroup>()

                    exerciseGroupsCsvFile.bufferedReader().useLines { lines ->
                        lines.forEach { line ->
                            val record = ExerciseGroup.fromCsvStringFromBackup(line)
                            exerciseGroups.add(record)
                        }
                    }
                    Log.d("Restore", "ExerciseGroups: $exerciseGroups")

                    viewModel.insertAllExerciseGroupsFromBackup(exerciseGroups)
                }

                if (exercisesCsvFile.exists()) {
                    val exercises = mutableListOf<Exercise>()

                    exercisesCsvFile.bufferedReader().useLines { lines ->
                        lines.forEach { line ->
                            val record = Exercise.fromCsvStringFromBackup(line)
                            exercises.add(record)
                        }
                    }
                    Log.d("Restore", "Exercises: $exercises")

                    viewModel.insertAllExercisesFromBackup(exercises)
                }

                if (exerciseSetsCsvFile.exists()) {
                    val exerciseSets = mutableListOf<ExerciseSet>()

                    exerciseSetsCsvFile.bufferedReader().useLines { lines ->
                        lines.forEach { line ->
                            val record = ExerciseSet.fromCsvStringFromBackup(line)
                            exerciseSets.add(record)
                        }
                    }
                    Log.d("Restore", "ExerciseSets: $exerciseSets")

                    viewModel.insertAllExerciseSetsFromBackup(exerciseSets)
                }
            } catch (e: Exception) {
                // Handle the exception
                Log.e("Restore", "Exception during restore", e)
            }
        }
    }


}
