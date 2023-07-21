package com.UpTrack.example.UpTrack.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.UpTrack.example.UpTrack.GainTrackerApplication
import com.UpTrack.example.UpTrack.adapters.SetAdapter
import com.UpTrack.example.UpTrack.data.models.ExerciseSet
import com.UpTrack.example.UpTrack.databinding.FragmentAddSetsBinding
import com.UpTrack.example.UpTrack.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect


class ExerciseSetsFragment : Fragment() {
    private var _binding: FragmentAddSetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var setAdapter: SetAdapter
    private var selectedSetForEdit: ExerciseSet? = null

    private val viewModel: MainViewModel by activityViewModels {
        (requireActivity().application as GainTrackerApplication).appContainer.mainViewModelFactory
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
        val currentExerciseId = arguments?.getLong("exerciseId") ?: throw IllegalStateException("No exerciseId provided")
        val application = requireActivity().application as GainTrackerApplication
        val repository = application.appContainer.mainRepository
        val savedUnit = viewModel.getSavedUnit()
        setAdapter = SetAdapter(object : SetAdapter.SetInteractionListener {

            override fun onSetEditClick(set: ExerciseSet) {
                selectedSetForEdit = set
                binding.editTextReps.setText(set.reps.toString())
                binding.editTextWeight.setText(set.weight.toString())
                binding.buttonAddSet.text = "Update Set"
            }

            override fun onSetDeleteClick(set: ExerciseSet) {
                lifecycleScope.launch {
                    viewModel.deleteExerciseSet(set)
                    // viewModel.deleteExerciseSet(set)
                   // val currentExerciseId = arguments?.getLong("exerciseId") ?: throw IllegalStateException("No exerciseId provided")
                   // val updatedSets = viewModel.getSetsForExercise(currentExerciseId).value ?: emptyList()
                   // setAdapter.setSets(updatedSets)
                }
            }
        }, false, savedUnit)

        binding.recyclerViewSets.adapter = setAdapter
        binding.recyclerViewSets.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getSetsForExerciseFlow(currentExerciseId).collect { sets ->
                setAdapter.setSets(sets)
            }
        }

        binding.buttonAddSet.setOnClickListener { onAddSetButtonClick() }
// Retrieve the saved unit and update the EditText's hint
        binding.editTextWeight.hint = "Lifted weight in $savedUnit:"
        binding.editTextReps.hint = "Number of repetitions:"

    }

    private fun onAddSetButtonClick() {
        val reps = binding.editTextReps.text.toString().toIntOrNull()
        val weight = binding.editTextWeight.text.toString().toDoubleOrNull()

        if (reps != null && weight != null) {
            val currentExerciseId = arguments?.getLong("exerciseId")
                ?: throw IllegalStateException("No exerciseId provided")

            if (selectedSetForEdit == null) {
                // Add new set
                val newSet = ExerciseSet(exercise_id = currentExerciseId, reps = reps, weight = weight)
                lifecycleScope.launch {
                    viewModel.insertExerciseSet(newSet)
                  //  viewModel.getSetsForExercise(currentExerciseId).observe(viewLifecycleOwner, { sets ->
                  //      setAdapter.setSets(sets)
                  //})
                }
            } else {
                // Update existing set
                selectedSetForEdit?.let { set ->
                    val updatedSet = ExerciseSet(id = set.id, exercise_id = currentExerciseId, reps = reps, weight = weight, date = set.date)
                    lifecycleScope.launch {
                        viewModel.updateExerciseSet(updatedSet)
                        // ...
                        // Reset input fields and button text
                        binding.editTextReps.setText("")
                        binding.editTextWeight.setText("")
                        binding.buttonAddSet.text = "Add Set"
                        selectedSetForEdit = null
                    }
                }
            }
        } else {
            // Show an error message if reps or weight input is invalid
            Toast.makeText(requireContext(), "Please enter valid reps and weight values", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(exerciseId: Long): ExerciseSetsFragment {
            val args = Bundle()
            args.putLong("exerciseId", exerciseId)

            val fragment = ExerciseSetsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
