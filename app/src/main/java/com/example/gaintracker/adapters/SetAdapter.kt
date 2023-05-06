package com.example.gaintracker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gaintracker.data.models.ExerciseSet
import com.example.gaintracker.databinding.ItemSetBinding

class SetAdapter(
    private val context: Context,
    private val interactionListener: SetInteractionListener
) : RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    private val sets = mutableListOf<ExerciseSet>()


    fun setSets(sets: List<ExerciseSet>) {
        this.sets.clear()
        this.sets.addAll(sets)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemSetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val currentSet = sets[position]

        holder.binding.textViewSetNumber.text = "Set ${sets.size - position}" // Display set numbers in descending order
        holder.binding.textViewReps.text = "${currentSet.reps} reps"
        holder.binding.textViewWeight.text = "x ${currentSet.weight} kg"

        holder.binding.imageViewDeleteSet.setOnClickListener {
            interactionListener.onSetDeleteClick(currentSet)
        }

        holder.binding.imageViewEditSet.setOnClickListener {
            interactionListener.onSetEditClick(currentSet)
        }


    }

    override fun getItemCount() = sets.size

    interface SetInteractionListener {
        fun onSetEditClick(set: ExerciseSet)
        fun onSetDeleteClick(set: ExerciseSet)
    }
    inner class SetViewHolder(val binding: ItemSetBinding) : RecyclerView.ViewHolder(binding.root)




}
