package com.example.gaintracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_exercises -> {
                    // Handle exercise action
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.action_dashboard -> {
                    // Handle dashboard action
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }
                else -> return@setOnNavigationItemSelectedListener false
            }
        }
    }
    override fun onResume() {
        super.onResume()
        setNavigationItemSelected()
    }

    private fun setNavigationItemSelected() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        when (this.javaClass.simpleName) {
            "MainActivity" -> bottomNavigationView.menu.findItem(R.id.action_exercises).isChecked = true
            "DashboardActivity" -> bottomNavigationView.menu.findItem(R.id.action_dashboard).isChecked = true
        }
    }

}

