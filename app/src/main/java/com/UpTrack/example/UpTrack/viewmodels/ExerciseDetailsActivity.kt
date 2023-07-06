package com.UpTrack.example.UpTrack.viewmodels

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.UpTrack.example.UpTrack.MainActivity
import com.UpTrack.example.UpTrack.R
import com.UpTrack.example.UpTrack.adapters.ExerciseDetailsViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class ExerciseDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_details)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        val exerciseId = intent.getLongExtra(EXTRA_EXERCISE_ID, -1)
        val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: ""


        if (exerciseId == -1L) {
            finish()
            return
        }
        Log.d("ExerciseDetailsActivity", "Exercise ID: $exerciseId, Name: $exerciseName")

        title = exerciseName

        // Add this line to enable the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewPagerAdapter = ExerciseDetailsViewPagerAdapter(this, exerciseId)
        viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Add Sets"
                1 -> "History"
                2 -> "Results"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    companion object {
        const val EXTRA_EXERCISE_ID = "com.UpTrack.example.gaintracker.EXTRA_EXERCISE_ID"
        const val EXTRA_EXERCISE_NAME = "com.UpTrack.example.gaintracker.EXTRA_EXERCISE_NAME"
        const val EXTRA_NAVIGATE_BACK_TO_MAIN = "com.UpTrack.example.gaintracker.EXTRA_NAVIGATE_BACK_TO_MAIN"
    }
    override fun onBackPressed() {

        if (intent.getBooleanExtra(EXTRA_NAVIGATE_BACK_TO_MAIN, false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            super.onBackPressed()
        }
    }

}
