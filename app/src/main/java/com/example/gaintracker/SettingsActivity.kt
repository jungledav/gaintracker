package com.example.gaintracker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.InputStreamReader
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gaintracker.data.database.GainTrackerDatabase
import com.example.gaintracker.data.models.Exercise
import com.example.gaintracker.data.models.ExerciseGroup
import com.example.gaintracker.data.models.ExerciseSet
import com.example.gaintracker.repositories.MainRepository
import com.example.gaintracker.viewmodels.MainViewModel
import com.example.gaintracker.viewmodels.MainViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private val READ_REQUEST_CODE = 42
    // We'll need to access your ViewModel and through it the Repository here. Replace this with actual access to your ViewModel.
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
// Initialize ViewModel using MainViewModelFactory
        val database = GainTrackerDatabase.getDatabase(applicationContext)
        val exerciseGroupDao = database.exerciseGroupDao()
        val exerciseDao = database.exerciseDao()
        val exerciseSetDao = database.exerciseSetDao()
        val repository = MainRepository(exerciseDao, exerciseSetDao,exerciseGroupDao)
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        val uploadButton: Button = findViewById(R.id.btn_upload)
        uploadButton.setOnClickListener {
            performFileSearch()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_exercises -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.action_dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.action_settings -> {
                    // Do nothing, we're already here
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.menu.findItem(R.id.action_settings).isChecked = true
    }

    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Allows any file type. Change to specific types if needed.
        }
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            resultData?.data?.also { uri ->
                contentResolver.openInputStream(uri)?.let { inputStream ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val records = CSVFormat.DEFAULT
                            .withHeader("exercise_group_name", "muscle_group_name", "exercise_date", "exercise_set_date", "exercise_set_reps", "exercise_set_weight")
                            .withFirstRecordAsHeader()
                            .parse(InputStreamReader(inputStream))
                        for (record in records) {
                            processRecord(record)
                        }
                    }
                }
            }
        }
    }

    private suspend fun processRecord(record: CSVRecord) {
        val exerciseGroupName = record["exercise_group_name"]
        val exerciseMuscleGroupName = record["muscle_group_name"]
        val exerciseDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(record["exercise_date"])?.time ?: System.currentTimeMillis()
        val exerciseSetDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(record["exercise_set_date"])?.time ?: System.currentTimeMillis()
        val exerciseSetReps = record["exercise_set_reps"].toInt()
        val exerciseSetWeight = record["exercise_set_weight"].toDouble()

        val group = viewModel.getExerciseGroupByName(exerciseGroupName)
        val groupId = group?.id?.toLong() ?: viewModel.insertExerciseGroup(ExerciseGroup(name = exerciseGroupName))

        // Check if the exercise for the given date and group already exists
        val existingExercise = viewModel.getExerciseByDateAndGroup(exerciseDate, groupId)

        // If the exercise exists, we use its id. Otherwise, we insert a new exercise and use its id.
        val exerciseId = existingExercise?.id?.toLong() ?: viewModel.insertExerciseWithDetails(Exercise(exerciseGroupId = groupId, date = exerciseDate))

        // Insert a new set for the existing or new exercise.
        viewModel.insertExerciseSet(ExerciseSet(exercise_id = exerciseId, date = exerciseSetDate, reps = exerciseSetReps, weight = exerciseSetWeight))
    }


}

