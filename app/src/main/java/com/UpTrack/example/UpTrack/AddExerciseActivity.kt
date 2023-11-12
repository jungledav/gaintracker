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
            // Get the selected item from the spinner, which is an instance of ExerciseDropdownItem
            val selectedItem = exerciseSpinner.selectedItem as? ExerciseDropdownItem.Exercise

            // Check if the selected item is not null and is not the "Please Select" prompt
            if (selectedItem != null && selectedItem.name != "Please Select") {
                lifecycleScope.launch {
                    // Extract the exercise name from the ExerciseDropdownItem
                    val exerciseName = selectedItem.name
                    Log.d("AddExerciseActivity", "Inserting exercise: $exerciseName")

                    // Insert the exercise and retrieve the ID
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
                // Prompt the user to select an exercise if "Please Select" is the current selection
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
        exerciseSpinnerAdapter = ExerciseDropdownAdapter(this@AddExerciseActivity, defaultExerciseItem)
        exerciseSpinner.adapter = exerciseSpinnerAdapter

        muscleGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val muscleGroupName = muscleGroupSpinner.selectedItem.toString()

                if (muscleGroupName != "Please select...") {
                    // Enable the exercise spinner and load associated exercises
                    val exerciseNames = PredefinedExercises.getExerciseNamesForMuscleGroup(muscleGroupName)
                    viewModel.loadDaysSinceLastTrained(exerciseNames) // Pass the list of exercise names to the method

                    // Update the adapter with a default "Please Select..." option
                    val items = mutableListOf(ExerciseDropdownItem.Exercise("Please select...", null))
                    exerciseSpinnerAdapter = ExerciseDropdownAdapter(this@AddExerciseActivity, items)
                    exerciseSpinner.adapter = exerciseSpinnerAdapter
                    exerciseSpinner.isEnabled = true
                    buttonAddExercise.isEnabled = false
                    exerciseSpinner.setSelection(0, false) // Set to "Please Select" without triggering item selected listener
                } else {
                    // Muscle group not selected, reset the exercise spinner
                    resetExerciseSpinner()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                resetExerciseSpinner()
            }
        }

        viewModel.exercisesWithLastTraining.observe(this, { exercises ->
            Log.d("AddExerciseActivity", "Exercises received: ${exercises.size}, ${exercises.joinToString()}")

            // Start with "Please Select..." prompt
            val items = mutableListOf<ExerciseDropdownItem>(
                ExerciseDropdownItem.Exercise("Please select...", null)
            )

            // Add the "Recently trained" header if there are any recently trained exercises
            if (exercises.any { it.daysAgo != null }) {
                items.add(ExerciseDropdownItem.SubHeader("Recently trained"))
                // Add recently trained exercises
                exercises.filter { it.daysAgo != null }
                    .sortedByDescending { it.daysAgo }
                    .mapTo(items) { ExerciseDropdownItem.Exercise(it.exerciseName, "${it.daysAgo} days ago") }
            }

            // Add the "Others" header if there are any other exercises
            if (exercises.any { it.daysAgo == null }) {
                items.add(ExerciseDropdownItem.SubHeader("Others"))
                // Add other exercises
                exercises.filter { it.daysAgo == null }
                    .mapTo(items) { ExerciseDropdownItem.Exercise(it.exerciseName, null) }
            }

            Log.d("AddExerciseActivity", "Dropdown items prepared: ${items.size}, ${items.joinToString()}")
            exerciseSpinnerAdapter = ExerciseDropdownAdapter(this@AddExerciseActivity, items)
            exerciseSpinner.adapter = exerciseSpinnerAdapter
        })

        exerciseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (exerciseSpinner.selectedItem is ExerciseDropdownItem.Exercise) {
                    val exercise = exerciseSpinner.selectedItem as ExerciseDropdownItem.Exercise
                    if (exercise.name != "Please select...") {
                        buttonAddExercise.isEnabled = true
                    } else {
                        buttonAddExercise.isEnabled = false
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                buttonAddExercise.isEnabled = false // Deactivate the buttonAddExercise
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

