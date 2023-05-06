package com.example.gaintracker.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gaintracker.data.models.ExerciseSet
import com.example.gaintracker.databinding.ItemExerciseHistoryBinding

class ExerciseHistoryAdapter(private val context: Context) : RecyclerView.Adapter<ExerciseHistoryAdapter.ExerciseHistoryViewHolder>() {
    private var exerciseHistoryList: List<ExerciseSet> = emptyList()

    fun setExerciseHistory(newExerciseHistory: List<ExerciseSet>) {
        exerciseHistoryList = newExerciseHistory
        Log.d("ExerciseHistoryAdapter", "New exercise history list: $exerciseHistoryList")

        notifyDataSetChanged()
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
        val exerciseHistory = exerciseHistoryList[position]
        holder.binding.textViewSet.text = "Set ${position + 1}: "
        holder.binding.textViewReps.text = "${exerciseHistory.reps} reps"
        holder.binding.textViewWeight.text = " x${exerciseHistory.weight} kg"

    }


    override fun getItemCount(): Int {
        return exerciseHistoryList.size
    }

    inner class ExerciseHistoryViewHolder(val binding: ItemExerciseHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Add any necessary ViewHolder functionality here
    }
}
