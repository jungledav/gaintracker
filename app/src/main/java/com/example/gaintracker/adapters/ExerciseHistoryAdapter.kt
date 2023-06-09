package com.example.gaintracker.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaintracker.R
import com.example.gaintracker.data.models.ExerciseSet
import com.example.gaintracker.databinding.ItemExerciseHistoryBinding
import com.example.gaintracker.databinding.ItemSetBinding
import com.example.gaintracker.fragments.ExerciseHistoryFragment
import java.text.SimpleDateFormat
import java.util.Locale

class ExerciseHistoryAdapter(
    private var exerciseSetsByDate: List<ExerciseHistoryFragment.ExerciseSetsByDate> = emptyList(),
    private var recordSetIds: Set<Long> = setOf()) :
    RecyclerView.Adapter<ExerciseHistoryAdapter.ExerciseHistoryViewHolder>() {

    inner class ExerciseHistoryViewHolder(val binding: ItemExerciseHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class SetsAdapter(private val sets: List<ExerciseSet>, private val isHistoryView: Boolean, private val recordSetIds: Set<Long>) :
        RecyclerView.Adapter<SetsAdapter.SetsViewHolder>() {

        inner class SetsViewHolder(val binding: ItemSetBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetsViewHolder {
            val binding = ItemSetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SetsViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SetsViewHolder, position: Int) {
            val reversedPosition = sets.size - 1 - position
            val currentSet = sets[reversedPosition]
            holder.binding.textViewSetNumber.text = "Set ${reversedPosition + 1}"
            holder.binding.textViewReps.text = "${currentSet.reps} reps"
            holder.binding.textViewWeight.text = "${currentSet.weight} kg"

            if (isRecordSet(currentSet)) {
                holder.binding.root.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.record_set_background))
            } else {
                holder.binding.root.setBackgroundColor(Color.TRANSPARENT)
            }
            if (isHistoryView) {
                holder.binding.imageViewDeleteSet.visibility = View.GONE
                holder.binding.imageViewEditSet.visibility = View.GONE
            } else {
                holder.binding.imageViewDeleteSet.visibility = View.VISIBLE
                holder.binding.imageViewEditSet.visibility = View.VISIBLE
            }
        }


        override fun getItemCount(): Int {
            return sets.size
        }

        private fun isRecordSet(currentSet: ExerciseSet): Boolean {
            return currentSet.id in recordSetIds
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseHistoryViewHolder {
        val binding = ItemExerciseHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExerciseHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseHistoryViewHolder, position: Int) {
        val currentSetsByDate = exerciseSetsByDate[position]
        val date = currentSetsByDate.date
        val sets = currentSetsByDate.sets.reversed() // Reverse the sets list here
        holder.binding.dividerDate.textViewDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

        holder.binding.setsRecyclerView.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = SetsAdapter(sets, true, recordSetIds)
        }

    }
    private fun isRecordSet(currentSet: ExerciseSet): Boolean {
        return recordSetIds.contains(currentSet.id)
    }
    override fun getItemCount(): Int {
        return exerciseSetsByDate.size
    }

    fun submitList(newSetsByDate: List<ExerciseHistoryFragment.ExerciseSetsByDate>, newRecordSetIds: Set<Long>) {
        exerciseSetsByDate = newSetsByDate
        recordSetIds = newRecordSetIds
        notifyDataSetChanged()
    }

    private fun calculateRecordSets(): Map<Long, ExerciseSet> {
        val sets = exerciseSetsByDate.flatMap { it.sets }
            .sortedWith(compareBy<ExerciseSet> { it.weight }.thenByDescending { it.reps }.thenBy { it.id })

        val recordSets = mutableMapOf<Pair<Double, Int>, ExerciseSet>()

        for (set in sets) {
            val currentRecordSet = recordSets[Pair(set.weight, set.reps)]
            if (currentRecordSet == null || set.id < currentRecordSet.id) {
                // If two sets have the same weight, reps and date, choose the set with the lower id
                recordSets[Pair(set.weight, set.reps)] = set
            }
        }

        return recordSets.values.associateBy { it.id }
    }




}