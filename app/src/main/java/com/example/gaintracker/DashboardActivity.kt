package com.example.gaintracker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)
        welcomeTextView.text = "Welcome back! Start a new workout now."

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
