package com.UpTrack.example.UpTrack.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.UpTrack.example.UpTrack.fragments.AddSetsFragment
import com.UpTrack.example.UpTrack.fragments.ExerciseHistoryFragment
class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            AddSetsFragment()
        } else {
            ExerciseHistoryFragment()
        }
    }
}
