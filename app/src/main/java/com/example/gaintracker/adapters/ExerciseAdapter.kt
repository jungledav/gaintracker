package com.example.gaintracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gaintracker.R
import com.example.gaintracker.data.models.Exercise
import com.example.gaintracker.data.models.ExerciseListItem
import java.text.SimpleDateFormat
import java.util.*



class ExerciseAdapter(
    private val onFetchSets: (exercise: Exercise) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = emptyList<ExerciseListItem>()

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewExerciseName: TextView = itemView.findViewById(R.id.textViewExerciseName)
        private val textViewSetsCount: TextView = itemView.findViewById(R.id.textViewSetsCount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick((items[position] as ExerciseListItem.ExerciseItem).exercise)
                }
            }
        }

        fun updateSetsCount(setsCount: Int) {
            val text = if (setsCount == 1) "set" else "sets"
            textViewSetsCount.text = "$setsCount $text"
        }
    }

    inner class DividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
    }

    inner class NoExercisesTodayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EXERCISE -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.exercise_item, parent, false)
                ExerciseViewHolder(itemView)
            }
            VIEW_TYPE_DIVIDER -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.divider_item, parent, false)
                DividerViewHolder(itemView)
            }
            VIEW_TYPE_NO_EXERCISES_TODAY -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.no_exercises_today_item, parent, false)
                NoExercisesTodayViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = items[position]

        when (holder) {
            is ExerciseViewHolder -> {
                val exerciseItem = currentItem as ExerciseListItem.ExerciseItem
                holder.textViewExerciseName.text = exerciseItem.exerciseGroupName
                onFetchSets(exerciseItem.exercise)
            }
            is DividerViewHolder -> {
                val dividerItem = currentItem as ExerciseListItem.DividerItem
                holder.textViewDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dividerItem.date)
            }
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ExerciseListItem.ExerciseItem -> VIEW_TYPE_EXERCISE
            is ExerciseListItem.DividerItem -> VIEW_TYPE_DIVIDER
            ExerciseListItem.NoExercisesTodayItem -> VIEW_TYPE_NO_EXERCISES_TODAY
        }
    }

    fun indexOfExercise(exercise: Exercise): Int {
        return items.indexOfFirst { it is ExerciseListItem.ExerciseItem && it.exercise == exercise }
    }

    fun setItems(items: List<ExerciseListItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(exercise: Exercise)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun getExerciseAt(position: Int): Exercise? {
        val item = items[position]
        return if (item is ExerciseListItem.ExerciseItem) {
            item.exercise
        } else {
            null
        }
    }

    companion object {
        private const val VIEW_TYPE_EXERCISE = 1
        private const val VIEW_TYPE_DIVIDER = 2
        private const val VIEW_TYPE_NO_EXERCISES_TODAY = 3
    }
}
