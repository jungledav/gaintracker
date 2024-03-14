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
            items.add(ExerciseDropdownItem.Exercise("Please select...", null)) // Default prompt

            // Separate exercises trained today
            val todaysExercises = exercises.filter { it.daysAgo == 0 }

            // Filter recently trained exercises excluding today, and sort them so the most recent is at the top
            val recentlyTrainedExercises = exercises.filter { it.daysAgo in 1..14 }.sortedBy { it.daysAgo }

            // Combine the sorted recently trained exercises with today's exercises at the end
            val combinedRecentlyTrained = recentlyTrainedExercises + todaysExercises

            if (combinedRecentlyTrained.isNotEmpty()) {
                items.add(ExerciseDropdownItem.SubHeader("Recently Trained"))
                combinedRecentlyTrained.forEach { exercise ->
                    val label = when (exercise.daysAgo) {
                        0 -> "Today"
                        else -> "${exercise.daysAgo} days ago"
                    }
                    items.add(ExerciseDropdownItem.Exercise(exercise.exerciseName, label))
                }
            }

            // Others: Include exercises never trained or trained more than 14 days ago, with never trained at the end
            val othersWithDate = exercises.filter { it.daysAgo != null && it.daysAgo > 14 }.sortedByDescending { it.daysAgo }
            val neverTrained = exercises.filter { it.daysAgo == null }

            if (othersWithDate.isNotEmpty() || neverTrained.isNotEmpty()) {
                items.add(ExerciseDropdownItem.SubHeader("Others"))
                othersWithDate.forEach { exercise ->
                    items.add(ExerciseDropdownItem.Exercise(exercise.exerciseName, "${exercise.daysAgo} days ago"))
                }
                neverTrained.forEach {
                    items.add(ExerciseDropdownItem.Exercise(it.exerciseName, "Never"))
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
        // Do not use setPositiveButton here. It will be added manually later to override the default dismiss behavior.

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            exerciseSpinner.setSelection(0)
        }

        customExerciseDialog = builder.create().apply {
            // Set the onCancelListener to reset the spinner if the dialog is canceled
            setOnCancelListener {
                exerciseSpinner.setSelection(0)
            }
            // Show the dialog before setting the positive button to override its onClickListener
            show()

            getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val customExerciseName = editTextCustomExerciseName.text.toString().trim()
                val selectedEquipmentType = spinnerEquipmentType.selectedItem.toString()
                val selectedMuscleGroup = muscleGroupSpinner.selectedItem.toString()

                if (customExerciseName.isNotBlank()) {
                    val existingExercises = PredefinedExercises.getExerciseNamesForMuscleGroup(selectedMuscleGroup)

                    if (customExerciseName in existingExercises) {
                        Toast.makeText(this@AddExerciseActivity, "An exercise with this name already exists for $selectedMuscleGroup.", Toast.LENGTH_SHORT).show()
                    } else {
                        PredefinedExercises.addCustomExercise(
                            context = this@AddExerciseActivity,
                            muscleGroupName = selectedMuscleGroup,
                            exerciseName = customExerciseName,
                            equipmentType = selectedEquipmentType
                        )

                        lifecycleScope.launch {
                            val exerciseId = viewModel.insertExercise(customExerciseName)
                            if (exerciseId != -1L) {
                                navigateToExerciseDetailsActivity(exerciseId, customExerciseName)
                                dismiss() // Only dismiss the dialog if the operation is successful
                            } else {
                                Toast.makeText(this@AddExerciseActivity, "Failed to insert custom exercise.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@AddExerciseActivity, "Please enter a custom exercise name", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Adding the positive button manually to override the default dismiss behavior
        customExerciseDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.let {
            it.text = getString(android.R.string.ok)
            it.visibility = View.VISIBLE
        }
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

