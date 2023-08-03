package com.UpTrack.example.UpTrack.fragments

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.UpTrack.example.UpTrack.adapters.SetAdapter
import com.UpTrack.example.UpTrack.data.models.ExerciseSet
import com.UpTrack.example.UpTrack.databinding.FragmentAddSetsBinding
import com.UpTrack.example.UpTrack.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class AddSetsFragment : Fragment() {

    private var _binding: FragmentAddSetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var setAdapter: SetAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private val currentExerciseId by lazy { requireArguments().getLong("exerciseId", -1) }

    private fun onAddSetButtonClick() {
        Log.d("AddSetsFragment", "Add set button clicked")

        val weight = binding.editTextWeight.text.toString().toDoubleOrNull()
        val reps = binding.editTextReps.text.toString().toIntOrNull()

        if (weight != null && reps != null) {
            val exerciseSet = ExerciseSet(
                exercise_id = currentExerciseId,
                weight = weight,
                reps = reps,
                date = System.currentTimeMillis()
            )
            // Wrap the call in a coroutine using lifecycleScope
            lifecycleScope.launch {
                viewModel.insertExerciseSet(exerciseSet)
            }
            binding.editTextWeight.setText("")
            binding.editTextReps.setText("")

            // Add vibration feedback here
            val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator?.hasVibrator() == true) {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    // API level 26 or higher
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    // API level 25 or lower
                    vibrator.vibrate(200)
                }
            }

        } else {
            Toast.makeText(requireContext(), "Please enter valid values for weight and reps.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (currentExerciseId == -1L) {
            requireActivity().finish()
            return
        }

        val savedUnit = viewModel.getSavedUnit()

        setAdapter = SetAdapter( object : SetAdapter.SetInteractionListener {
            override fun onSetEditClick(set: ExerciseSet) {
                // Handle set edit click
            }

            override fun onSetDeleteClick(set: ExerciseSet) {
                lifecycleScope.launch {
                        viewModel.getSetsForExerciseFlow(currentExerciseId).collect { sets ->
                            setAdapter.setSets(sets.reversed())
                        }
                    }
            }
        },  false,savedUnit)

        binding.recyclerViewSets.adapter = setAdapter
        binding.recyclerViewSets.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            viewModel.getSetsForExerciseFlow(currentExerciseId).collect { sets ->
                setAdapter.setSets(sets.reversed())
            }
        }

        binding.buttonAddSet.setOnClickListener {
            onAddSetButtonClick()

            // Add vibration feedback here
            val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator?.hasVibrator() == true) {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    // API level 26 or higher
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    // API level 25 or lower
                    vibrator.vibrate(200)
                }
            }
        }


    }

    companion object {
        fun newInstance(exerciseId: Long, exerciseName: String): AddSetsFragment {
            val args = Bundle()
            args.putLong("exerciseId", exerciseId)
            args.putString("exerciseName", exerciseName)

            val fragment = AddSetsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
