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
import com.UpTrack.example.UpTrack.adapters.ExerciseDropdownAdapter
import com.UpTrack.example.UpTrack.adapters.MuscleGroupSpinnerAdapter
import com.UpTrack.example.UpTrack.data.models.ExerciseDropdownItem


class AddExerciseActivity : AppCompatActivity() {

    private val appContainer by lazy {
        (application as GainTrackerApplication).appContainer
    }
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(appContainer.mainRepository) }
    private lateinit var muscleGroupAdapter: ArrayAdapter<String>
    private lateinit var muscleGroupSpinner: Spinner
    private lateinit var exerciseSpinner: Spinner
    private lateinit var buttonAddExercise: Button
    private lateinit var exerciseSpinnerAdapter: ExerciseDropdownAdapter


    companion object {
        const val EXTRA_FROM_MAIN_ACTIVITY = "com.UpTrack.example.UpTrack.EXTRA_FROM_MAIN_ACTIVITY"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_add_exercise)
        // Initialization of muscleGroupAdapter should happen here
        val muscleGroupNames = PredefinedExercises.getMuscleGroupNames()
        val fullMuscleGroupNameList = listOf("Please select...") + muscleGroupNames

        muscleGroupAdapter = MuscleGroupSpinnerAdapter(this, fullMuscleGroupNameList)
        muscleGroupSpinner = findViewById(R.id.muscleGroupSpinner)
        muscleGroupSpinner.adapter = muscleGroupAdapter

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
            val selectedItem = exerciseSpinner.selectedItem as? ExerciseDropdownItem.Exercise
            if (selectedItem != null && selectedItem.name != "Please Select") {
                lifecycleScope.launch {
                    val exerciseName = selectedItem.name
                    val exerciseId = viewModel.insertExercise(exerciseName)
                    if (exerciseId != 0L) {
                        navigateToExerciseDetailsActivity(exerciseId, exerciseName)
                    } else {
                        Toast.makeText(this@AddExerciseActivity, "Failed to insert exercise.", Toast.LENGTH_SHORT).show()
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
        val muscleGroupNames = PredefinedExercises.getMuscleGroupNames()
        val fullMuscleGroupNameList = listOf("Please select...") + muscleGroupNames

        muscleGroupAdapter = MuscleGroupSpinnerAdapter(this, fullMuscleGroupNameList)
        muscleGroupSpinner.adapter = muscleGroupAdapter

        // Initially deactivate the exerciseSpinner
        exerciseSpinner.isEnabled = false

        // Prepare default "Please Select..." option for exerciseSpinner
        val defaultExerciseItem = listOf(ExerciseDropdownItem.Exercise("Please select...", null))
        exerciseSpinnerAdapter =
            ExerciseDropdownAdapter(this@AddExerciseActivity, defaultExerciseItem)
        exerciseSpinner.adapter = exerciseSpinnerAdapter

        muscleGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val muscleGroupName = muscleGroupSpinner.selectedItem.toString()

                if (muscleGroupName != "Please select...") {
                    // Enable the exercise spinner and load associated exercises
                    val exerciseNames =
                        PredefinedExercises.getExerciseNamesForMuscleGroup(muscleGroupName)
                    viewModel.loadDaysSinceLastTrained(exerciseNames) // Pass the list of exercise names to the method

                    // Update the adapter with a default "Please Select..." option
                    val items =
                        mutableListOf(ExerciseDropdownItem.Exercise("Please select...", null))
                    exerciseSpinnerAdapter =
                        ExerciseDropdownAdapter(this@AddExerciseActivity, items)
                    exerciseSpinner.adapter = exerciseSpinnerAdapter
                    exerciseSpinner.isEnabled = true
                    buttonAddExercise.isEnabled = false
                    exerciseSpinner.setSelection(
                        0,
                        false
                    ) // Set to "Please Select" without triggering item selected listener
                } else {
                    // Muscle group not selected, reset the exercise spinner
                    resetExerciseSpinner()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                resetExerciseSpinner()
            }
        }

        viewModel.exercisesWithLastTraining.observe(this) { exercises ->
            val items = mutableListOf<ExerciseDropdownItem>()
            items.add(ExerciseDropdownItem.Exercise("Please select...", null)) // Add default prompt

            // Filter and sort the exercises that were trained, excluding today's workouts
            val todaysExercises = exercises.filter { it.daysAgo == 0 }

            val recentlyTrainedExercises = exercises.filter { it.daysAgo != null && it.daysAgo > 0 }
                .sortedBy { it.daysAgo }

            // Add recently trained exercises, if any
            if (recentlyTrainedExercises.isNotEmpty()||todaysExercises.isNotEmpty()) {
                items.add(ExerciseDropdownItem.SubHeader("Recently trained"))
                recentlyTrainedExercises.mapTo(items) {
                    ExerciseDropdownItem.Exercise(it.exerciseName, "${it.daysAgo} days ago")
                }
            }

            // Add today's exercises at the end of the recently trained section
            if (todaysExercises.isNotEmpty()) {
                todaysExercises.mapTo(items) {
                    ExerciseDropdownItem.Exercise(it.exerciseName, "Today")
                }
            }

            // Add the "Others" header if there are any other exercises
            val otherExercises = exercises.filter { it.daysAgo == null }
            if (otherExercises.isNotEmpty()) {
                items.add(ExerciseDropdownItem.SubHeader("Others"))
                otherExercises.mapTo(items) {
                    ExerciseDropdownItem.Exercise(
                        it.exerciseName,
                        "Never"
                    )
                }
            }

            exerciseSpinnerAdapter = ExerciseDropdownAdapter(this@AddExerciseActivity, items)
            exerciseSpinner.adapter = exerciseSpinnerAdapter
        }

        exerciseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = exerciseSpinner.selectedItem as? ExerciseDropdownItem.Exercise
                when (selectedItem?.name) {
                    "+ Add your own exercise" -> {
                        showAddCustomExerciseDialog()
                    }

                    "Please select..." -> {
                        buttonAddExercise.isEnabled = false
                    }

                    null -> {
                        buttonAddExercise.isEnabled = false
                        Toast.makeText(
                            this@AddExerciseActivity,
                            "Please select an exercise",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        // If a valid exercise is selected (other than special cases), enable the button
                        buttonAddExercise.isEnabled = true
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                buttonAddExercise.isEnabled = false
            }
        }
    }
        private fun resetExerciseSpinner() {
        // Reset the exercise spinner to its default state with "Please Select..."
        val defaultExerciseList = listOf(ExerciseDropdownItem.Exercise("Please select...", null))
        exerciseSpinnerAdapter = ExerciseDropdownAdapter(this, defaultExerciseList)
        exerciseSpinner.adapter = exerciseSpinnerAdapter
        exerciseSpinner.isEnabled = false
        buttonAddExercise.isEnabled = false
        exerciseSpinner.setSelection(0, false) // Set to "Please Select" without triggering item selected listener
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
            val selectedMuscleGroup = muscleGroupSpinner.selectedItem.toString()

            if (customExerciseName.isNotBlank()) {
                // First, add the exercise type with its muscle group association
                PredefinedExercises.addCustomExercise(
                    context = this,
                    muscleGroupName = selectedMuscleGroup,
                    exerciseName = customExerciseName,
                    equipmentType = selectedEquipmentType
                )

                // Then, insert an instance of the exercise to get an ID
                lifecycleScope.launch {
                    val exerciseId = viewModel.insertExercise(customExerciseName)
                    if (exerciseId != -1L) {
                        navigateToExerciseDetailsActivity(exerciseId, customExerciseName)
                    } else {
                        Toast.makeText(this@AddExerciseActivity, "Failed to insert custom exercise.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a custom exercise name", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        customExerciseDialog = builder.create()
        customExerciseDialog?.show()
    }


    private fun refreshExerciseSpinnerData() {
        val muscleGroupName = muscleGroupSpinner.selectedItem.toString()
        val exerciseNames = PredefinedExercises.getExerciseNamesForMuscleGroup(muscleGroupName)

        // This assumes viewModel.loadDaysSinceLastTrained will trigger an update to the spinner data
        viewModel.loadDaysSinceLastTrained(exerciseNames)
    }


    private fun navigateToExerciseDetailsActivity(exerciseId: Long, exerciseName: String) {
        Log.d("AddExerciseActivity", "Navigating to ExerciseDetailsActivity with ID: $exerciseId and Name: $exerciseName")

        val intent = Intent(this, ExerciseDetailsActivity::class.java).apply {
            putExtra(ExerciseDetailsActivity.EXTRA_EXERCISE_ID, exerciseId)
            putExtra(ExerciseDetailsActivity.EXTRA_EXERCISE_NAME, exerciseName)
            putExtra(ExerciseDetailsActivity.EXTRA_NAVIGATE_BACK_TO_MAIN, true)
        }
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

