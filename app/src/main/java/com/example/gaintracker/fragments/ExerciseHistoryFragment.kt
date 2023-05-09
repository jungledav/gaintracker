package com.example.gaintracker.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gaintracker.adapters.ExerciseHistoryAdapter
import com.example.gaintracker.data.dao.ExerciseDao
import com.example.gaintracker.databinding.FragmentExerciseHistoryBinding
import com.example.gaintracker.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.gaintracker.data.database.GainTrackerDatabase
import com.example.gaintracker.data.models.ExerciseSet
import java.util.Date

class ExerciseHistoryFragment : Fragment() {

    data class ExerciseSetWithExerciseDate(
        val id: Long,
        val exercise_id: Long,
        val date: Long,
        val reps: Int,
        val weight: Double,
        val exerciseDate: Long
    )
    data class ExerciseSetsByDate(val date: Date, val sets: List<ExerciseSet>)

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
    private lateinit var exerciseDao: ExerciseDao

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

        // Initialize ViewModel, ExerciseHistoryAdapter, ExerciseDao, and other variables
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        exerciseHistoryAdapter = ExerciseHistoryAdapter()
        exerciseDao = GainTrackerDatabase.getDatabase(requireContext()).exerciseDao()

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
        lifecycleScope.launch {
            val exerciseDao = GainTrackerDatabase.getDatabase(requireContext()).exerciseDao()
            val setsWithExerciseDate = withContext(Dispatchers.IO) {
                exerciseDao.getSetsForExerciseGroupWithExerciseDate(exerciseGroupId.toLong())
            }

            val setsByDate = setsWithExerciseDate.groupBy { it.exerciseDate }
                .map { (date, sets) -> ExerciseSetsByDate(Date(date), sets.map { ExerciseSet(it.id, it.exercise_id, it.date, it.reps, it.weight) }.sortedByDescending { it.date }) }

            val recordSets = calculateRecordSets(setsByDate)
            exerciseHistoryAdapter.submitList(setsByDate, recordSets)
        }
    }

    private fun calculateRecordSets(setsByDate: List<ExerciseSetsByDate>): Set<Long> {
        val sets = setsByDate.flatMap { it.sets }
        val recordSets = mutableListOf<ExerciseSet>()

        for (set in sets.sortedWith(compareByDescending<ExerciseSet> { it.weight }.thenByDescending { it.reps }.thenBy { it.date })) {
            if (recordSets.none { it.weight >= set.weight && it.reps >= set.reps }) {
                recordSets.add(set)
            }
        }

        return recordSets.map { it.id }.toSet()
    }







    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
