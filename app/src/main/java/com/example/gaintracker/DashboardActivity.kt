package com.example.gaintracker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import com.example.gaintracker.viewmodels.MainViewModel
import com.example.gaintracker.viewmodels.MainViewModelFactory
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.Instant

class DashboardActivity : AppCompatActivity() {
    private val appContainer by lazy {
        (application as GainTrackerApplication).appContainer
    }
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(appContainer.mainRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Add the new code here
        lifecycleScope.launch {
            val latestExercise = viewModel.getLatestExercise()
            if (latestExercise != null) {
                val latestExerciseDate = Instant.ofEpochMilli(latestExercise.date).atZone(ZoneId.systemDefault()).toLocalDate()
                val currentDate = LocalDate.now(ZoneId.systemDefault())
                val daysSinceLastExercise = ChronoUnit.DAYS.between(latestExerciseDate, currentDate)
                findViewById<TextView>(R.id.welcomeTextView).text = "Hi, welcome back! It's been $daysSinceLastExercise days since your last workout. Start a new workout now."
            }

            if (latestExercise == null) { // No exercise added by users yet
                findViewById<TextView>(R.id.welcomeTextView).text = "Hi, welcome to GainTracker. Are you ready for your first workout?"


            }
            else { // Last workout is today
                findViewById<TextView>(R.id.welcomeTextView).text = "Hi, welcome back! Would you like to continue today's workout?"
            }
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
                    // Do nothing, we're already here
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.menu.findItem(R.id.action_dashboard).isChecked = true
    }
}
