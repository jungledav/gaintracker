package com.example.gaintracker.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gaintracker.adapters.ExerciseHistoryAdapter
import com.example.gaintracker.databinding.FragmentExerciseHistoryBinding
import com.example.gaintracker.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import com.example.gaintracker.repositories.MainRepository

class ExerciseHistoryFragment : Fragment() {
    companion object {
        const val ARG_EXERCISE_GROUP_ID = "exercise_group_id"

        fun newInstance(exerciseGroupId: Long): ExerciseHistoryFragment {
            val fragment = ExerciseHistoryFragment()
            val args = Bundle()
            args.putLong(ARG_EXERCISE_GROUP_ID, exerciseGroupId)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentExerciseHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var exerciseHistoryAdapter: ExerciseHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val exerciseId = arguments?.getLong(ARG_EXERCISE_GROUP_ID) ?: -1

        // Initialize ViewModel, ExerciseHistoryAdapter, and other variables
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        exerciseHistoryAdapter = ExerciseHistoryAdapter(requireContext())

        binding.recyclerViewExerciseHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseHistoryAdapter
        }

        viewModel.getExerciseGroupId(exerciseId).observe(viewLifecycleOwner) { exerciseGroupId ->
            Log.d("ExerciseHistoryFragment", "Exercise Group ID: $exerciseGroupId, Exercise ID: $exerciseId")

            updateSetsForExerciseGroup(exerciseGroupId.toInt())
        }
    }

    private fun updateSetsForExerciseGroup(exerciseGroupId: Int) {
        viewModel.getSetsForExerciseGroup(exerciseGroupId).observe(viewLifecycleOwner) { sets ->
            exerciseHistoryAdapter.setExerciseHistory(sets)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
