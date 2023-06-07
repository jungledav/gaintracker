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

        val tvMaxRep = view.findViewById<TextView>(R.id.tv_max_rep)
        val tvTotalReps = view.findViewById<TextView>(R.id.tv_total_reps)
        val tvExerciseVolume = view.findViewById<TextView>(R.id.tv_exercise_volume)
        val tvMaxSetVolume = view.findViewById<TextView>(R.id.tv_max_set_volume)

        viewModel.getMaxRepForExercise(exerciseId).observe(viewLifecycleOwner, { maxRep ->
            tvMaxRep.text = "Max rep: $maxRep"
        })

        viewModel.getTotalRepsForExercise(exerciseId).observe(viewLifecycleOwner, { totalReps ->
            tvTotalReps.text = "Total reps: $totalReps"
        })

        viewModel.getExerciseVolumeForExercise(exerciseId).observe(viewLifecycleOwner, { exerciseVolume ->
            tvExerciseVolume.text = "Exercise volume: $exerciseVolume"
        })

        viewModel.getMaxSetVolumeForExercise(exerciseId).observe(viewLifecycleOwner, { maxSetVolume ->
            tvMaxSetVolume.text = "Max set volume: $maxSetVolume"
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
