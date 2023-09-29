package com.UpTrack.example.UpTrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.UpTrack.example.UpTrack.data.predefined.PredefinedExercises
import com.UpTrack.example.UpTrack.viewmodels.MainViewModel
import com.UpTrack.example.UpTrack.viewmodels.MainViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.appcompat.widget.Toolbar
import com.UpTrack.example.UpTrack.viewmodels.ExerciseDetailsActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import androidx.lifecycle.Observer


class AddExerciseActivity : AppCompatActivity() {

    private val appContainer by lazy {
        (application as GainTrackerApplication).appContainer
    }
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(appContainer.mainRepository) }

    private lateinit var muscleGroupSpinner: Spinner
    private lateinit var exerciseSpinner: Spinner
    private lateinit var buttonAddExercise: Button

    companion object {
        const val EXTRA_FROM_MAIN_ACTIVITY = "com.UpTrack.example.UpTrack.EXTRA_FROM_MAIN_ACTIVITY"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_add_exercise)
        loadCustomExercises()
        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar_add_exercise)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Exercise"
        muscleGroupSpinner = findViewById(R.id.muscleGroupSpinner)
        exerciseSpinner = findViewById(R.id.exerciseNameSpinner)
        buttonAddExercise = findViewById(R.id.buttonAddExercise)
        // Observe the isLoading LiveData

        // Initially deactivate the buttonAddExercise
        buttonAddExercise.isEnabled = false
        setupSpinners()
        viewModel.isLoading.observe(this, Observer { isLoading ->
            // Enable or disable UI components based on `isLoading`

            exerciseSpinner.isEnabled = !isLoading
            buttonAddExercise.isEnabled = !isLoading
        })
        buttonAddExercise.setOnClickListener {
            val exerciseName = exerciseSpinner.selectedItem.toString()

            if (exerciseName.isNotBlank() && exerciseName != "+ Add your own exercise") {
                lifecycleScope.launch {
                    Log.d("AddExerciseActivity", "Inserting exercise: $exerciseName")
                    val fullExerciseName = exerciseSpinner.selectedItem.toString()
                   val exerciseName = fullExerciseName.substringBefore(" (").trim()
                    val exerciseId = viewModel.insertExercise(exerciseName)
                    Log.d("AddExerciseActivity", "Exercise inserted with ID: $exerciseId")

                    if (exerciseId != 0L) {
                        Log.d("AddExerciseActivity", "Navigating to ExerciseDetailsActivity with ID: $exerciseId and Name: $exerciseName")

                        navigateToExerciseDetailsActivity(exerciseId, exerciseName)
                    } else {
                        Log.d("AddExerciseActivity", "Failed to insert exercise.")
                    }
                }
            } else {
                Toast.makeText(this, "Please select an exercise", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun loadCustomExercises() {
        val sharedPreferences = getSharedPreferences("custom_exercises", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("custom_exercises_data", null)

        if (json != null) {
            PredefinedExercises.setCustomExercises(Json.decodeFromString(json))
        }
    }

    private fun setupSpinners() {
        val muscleGroupList = PredefinedExercises.getMuscleGroupNames().toMutableList()
        muscleGroupList.add(0, "Please select...")
        val muscleGroupAdapter = ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, muscleGroupList
        )
        muscleGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        muscleGroupSpinner.adapter = muscleGroupAdapter

        // Initially deactivate the exerciseSpinner
        exerciseSpinner.isEnabled = false

        muscleGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val muscleGroupName = muscleGroupSpinner.selectedItem.toString()

                if(muscleGroupName == "Please select...") {
                    // If 'Please select' is chosen, deactivate the exerciseSpinner
                    exerciseSpinner.isEnabled = false
                    buttonAddExercise.isEnabled = false // Deactivate the buttonAddExercise

                } else {
                    // If a muscle group is selected, activate the exerciseSpinner and load associated exercises
                    exerciseSpinner.isEnabled = true
                    val muscleGroupName = muscleGroupSpinner.selectedItem.toString()
                    val exerciseNames = PredefinedExercises.getExerciseNamesForMuscleGroup(muscleGroupName).filter { it != "Please select..." }
                    Log.d("AddExerciseActivity", "exercisename:$exerciseNames" )

                    viewModel.loadDaysSinceLastTrained(exerciseNames)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                buttonAddExercise.isEnabled = false // Deactivate the buttonAddExercise
            }
        }

        viewModel.exercisesWithLastTraining.observe(this, { exercises ->
            val exerciseListWithDates = mutableListOf("Please select...")
            val exercisedListWithDates = mutableListOf<String>()
            val nonExercisedListWithDates = mutableListOf<String>()

            for (exercise in exercises) {
                val daysAgoLong = exercise.daysAgo?.toLong()
                val daysAgoString = when (daysAgoLong) {
                    null -> ""
                    0L -> "Today"
                    1L -> "Yesterday"
                    else -> "${exercise.daysAgo} days ago"
                }

                val exerciseItem = if (exercise.daysAgo != null) {
                    "${exercise.exerciseName} ($daysAgoString)"
                } else {
                    exercise.exerciseName
                }

                if (exercise.daysAgo != null) {
                    exercisedListWithDates.add(exerciseItem)
                } else {
                    nonExercisedListWithDates.add(exerciseItem)
                }
            }

            exerciseListWithDates.addAll(exercisedListWithDates)
            exerciseListWithDates.addAll(nonExercisedListWithDates)

            val exerciseAdapter = ArrayAdapter<String>(
                this@AddExerciseActivity, android.R.layout.simple_spinner_item, exerciseListWithDates
            )
            exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            exerciseSpinner.adapter = exerciseAdapter
        })

        exerciseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (exerciseSpinner.selectedItem == "+ Add your own exercise") {
                    showAddCustomExerciseDialog()
                }
                // If an exercise is selected and it is not 'Please select...', activate the buttonAddExercise
                if (exerciseSpinner.selectedItem != "Please select...") {
                    buttonAddExercise.isEnabled = true
                } else {
                    buttonAddExercise.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                buttonAddExercise.isEnabled = false // Deactivate the buttonAddExercise
            }
        }
    }

    private var customExerciseDialog: AlertDialog? = null

    private fun showAddCustomExerciseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom_exercise, null)
        val editTextCustomExerciseName = dialogView.findViewById<EditText>(R.id.editTextCustomExerciseName)
        val spinnerEquipmentType = dialogView.findViewById<Spinner>(R.id.spinnerEquipmentType)

        val equipmentTypes = listOf("Bodyweight", "Dumbbell", "Barbell", "Machine")
        val equipmentTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, equipmentTypes)
        equipmentTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEquipmentType.adapter = equipmentTypeAdapter

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle("Add Custom Exercise")
        builder.setPositiveButton("Add") { _, _ ->
            val customExerciseName = editTextCustomExerciseName.text.toString().trim()
            val selectedEquipmentType = spinnerEquipmentType.selectedItem.toString()
            if (customExerciseName.contains("(") || customExerciseName.contains(")")) {
                Toast.makeText(this, "Brackets are not allowed in exercise names.", Toast.LENGTH_SHORT).show()
                showAddCustomExerciseDialog()
            } else if (customExerciseName.isNotBlank()) {
                PredefinedExercises.addCustomExercise(this, muscleGroupSpinner.selectedItem.toString(), customExerciseName, selectedEquipmentType)

                val muscleGroupName = muscleGroupSpinner.selectedItem.toString()
                val updatedExercises = PredefinedExercises.getExerciseNamesForMuscleGroup(muscleGroupName)
                val customExerciseIndex = updatedExercises.indexOf(customExerciseName)

                val exerciseAdapter = ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, updatedExercises
                )
                exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                exerciseSpinner.adapter = exerciseAdapter
                exerciseSpinner.setSelection(customExerciseIndex)
            } else {
                Toast.makeText(this, "Please enter a custom exercise name", Toast.LENGTH_SHORT).show()
                showAddCustomExerciseDialog()
            }
        }
        builder.setNegativeButton("Cancel", null)
        customExerciseDialog = builder.create()
        customExerciseDialog?.show()
    }




    private fun navigateToExerciseDetailsActivity(exerciseId: Long, exerciseName: String) {
        Log.d("AddExerciseActivity", "Navigating to ExerciseDetailsActivity with ID: $exerciseId and Name: $exerciseName")

        val intent = Intent(this, ExerciseDetailsActivity::class.java)
        intent.putExtra(ExerciseDetailsActivity.EXTRA_EXERCISE_ID, exerciseId)
        intent.putExtra(ExerciseDetailsActivity.EXTRA_EXERCISE_NAME, exerciseName)
        intent.putExtra(ExerciseDetailsActivity.EXTRA_NAVIGATE_BACK_TO_MAIN, true) // new flag
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        customExerciseDialog?.dismiss()
        customExerciseDialog = null
    }

}

