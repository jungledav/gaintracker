package com.example.gaintracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gaintracker.GainTrackerApplication
import com.example.gaintracker.R
import com.example.gaintracker.viewmodels.MainViewModel

class ExerciseResultsFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels {
        (requireActivity().application as GainTrackerApplication).appContainer.mainViewModelFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_exercise_results, container, false)

        val tvMaxWeight = view.findViewById<TextView>(R.id.tv_max_weight)
        val tvMaxWeightType = view.findViewById<TextView>(R.id.tv_max_weight_type)

        val exerciseId = arguments?.getLong("exerciseId") ?: throw IllegalStateException("No exerciseId provided")

        viewModel.getMaxWeightForExercise(exerciseId).observe(viewLifecycleOwner, { maxWeight ->
            tvMaxWeight.text = "Max weight: $maxWeight"
        })

        viewModel.getMaxWeightForExerciseType(exerciseId).observe(viewLifecycleOwner, { maxWeightType ->
            tvMaxWeightType.text = "Max weight for exercise type: $maxWeightType"
        })

        return view
    }


    companion object {
        fun newInstance(exerciseId: Long): ExerciseResultsFragment {
            val args = Bundle()
            args.putLong("exerciseId", exerciseId)
            val fragment = ExerciseResultsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
