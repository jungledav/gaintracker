package com.UpTrack.example.UpTrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.UpTrack.example.UpTrack.R
import com.UpTrack.example.UpTrack.data.models.ExerciseSet
import com.UpTrack.example.UpTrack.databinding.ItemSetBinding
import android.view.animation.Animation
import android.view.animation.AnimationUtils

class SetAdapter(
private val listener: SetInteractionListener,
private val isHistoryView: Boolean = false,
private val savedUnit: String
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

    override fun onBindViewHolder(holder: SetAdapter.SetViewHolder, position: Int) {
        val currentSet = sets[position]
        holder.binding.textViewSetNumber.text = "Set ${sets.size - position}"
        holder.binding.textViewReps.text = "${currentSet.reps} reps"
        holder.binding.textViewWeight.text = "${currentSet.weight} $savedUnit"

        holder.binding.imageViewDeleteSet.setOnClickListener {
            listener.onSetDeleteClick(currentSet)
        }

        holder.binding.imageViewEditSet.setOnClickListener {
            listener.onSetEditClick(currentSet)
        }

        // Apply animation
        if (position == 0) {  // New items appear at position 0
            val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in)
            holder.itemView.startAnimation(animation)
        }
    }



    override fun getItemCount() = sets.size

    interface SetInteractionListener {
        fun onSetEditClick(set: ExerciseSet)
        fun onSetDeleteClick(set: ExerciseSet)
    }

    inner class SetViewHolder(val binding: ItemSetBinding) : RecyclerView.ViewHolder(binding.root)
}
