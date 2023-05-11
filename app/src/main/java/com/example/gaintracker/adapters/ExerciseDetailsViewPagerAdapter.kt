package com.example.gaintracker.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gaintracker.fragments.ExerciseSetsFragment
import com.example.gaintracker.fragments.ExerciseHistoryFragment
import com.example.gaintracker.fragments.ExerciseResultsFragment

class ExerciseDetailsViewPagerAdapter(activity: FragmentActivity, private val exerciseId: Long) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ExerciseSetsFragment.newInstance(exerciseId)
            1 -> ExerciseHistoryFragment.newInstance(exerciseId)
            2 -> ExerciseResultsFragment.newInstance(exerciseId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
