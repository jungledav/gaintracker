package com.UpTrack.example.UpTrack.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.UpTrack.example.UpTrack.MainActivity
import com.UpTrack.example.UpTrack.R
import com.UpTrack.example.UpTrack.data.models.Exercise
import com.UpTrack.example.UpTrack.data.models.ExerciseListItem
import com.UpTrack.example.UpTrack.data.predefined.PredefinedExercises
import java.text.SimpleDateFormat
import java.util.*

class ExerciseAdapter(
    private val context: MainActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = emptyList<ExerciseListItem>()

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewExerciseName: TextView = itemView.findViewById(R.id.textViewExerciseName)
        val textViewSetsCount: TextView = itemView.findViewById(R.id.textViewSetsCount)
        val imageViewMuscleGroup: ImageView = itemView.findViewById(R.id.imageViewMuscleGroup)


        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val exerciseItem = items[position] as ExerciseListItem.ExerciseItem
                    listener?.onItemClick(Exercise(exerciseItem.exerciseId.toInt(), exerciseItem.exerciseGroupId, exerciseItem.exerciseDate))
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

    inner class NoExercisesTodayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                context.onNoExercisesTodayClick()
            }
        }
    }

    inner class AddAnotherExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addAnotherExerciseButton: Button = itemView.findViewById(R.id.addAnotherExerciseButton)

        init {
            addAnotherExerciseButton.setOnClickListener {
                context.onAddAnotherExerciseClick()
            }
        }
    }

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
            VIEW_TYPE_ADD_ANOTHER_EXERCISE -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.add_another_exercise_item, parent, false)
                AddAnotherExerciseViewHolder(itemView)
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
                holder.updateSetsCount(exerciseItem.totalSets)
                // Lookup the muscle group using the new method
                val muscleGroupName = PredefinedExercises.findMuscleGroupByExerciseName(exerciseItem.exerciseGroupName)

                // Match the exercise's group name to the correct icon
                val muscleGroupIcon = when (muscleGroupName)  {
                    "Chest" -> R.drawable.chestcropped
                    "Back" -> R.drawable.backcropped
                    "Shoulders" -> R.drawable.shouldercropped
                    "Biceps" -> R.drawable.bizepscropped
                    "Triceps" -> R.drawable.tricepscropped
                    "Legs" -> R.drawable.legsistock
                    "Abs" -> R.drawable.abscropped
                    "Gluteus" -> R.drawable.gluteuscropped
                    "Forearms" -> R.drawable.forearmcropped
                    else -> null
                }
                if (muscleGroupIcon != null) {
                    holder.imageViewMuscleGroup.setImageResource(muscleGroupIcon)
                    holder.imageViewMuscleGroup.visibility = View.VISIBLE

                    holder.imageViewMuscleGroup.postDelayed({
                        val drawable = holder.imageViewMuscleGroup.drawable
                        Log.d("ExerciseAdapter", "Delayed Drawable check: $drawable")
                    }, 100)
                } else {
                    holder.imageViewMuscleGroup.visibility = View.GONE
                }
                Log.d("ExerciseAdapter", "Exercise Group Name: ${exerciseItem.exerciseGroupName}")
                Log.d("ExerciseAdapter", "Muscle Group Name: $muscleGroupName")

            }
            is DividerViewHolder -> {
                val dividerItem = currentItem as ExerciseListItem.DividerItem
                holder.textViewDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dividerItem.date)
            }
            is NoExercisesTodayViewHolder, is AddAnotherExerciseViewHolder -> {
                // Nothing to bind
            }
        }
    }


    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ExerciseListItem.ExerciseItem -> VIEW_TYPE_EXERCISE
            is ExerciseListItem.DividerItem -> VIEW_TYPE_DIVIDER
            is ExerciseListItem.NoExercisesTodayItem -> VIEW_TYPE_NO_EXERCISES_TODAY
            is ExerciseListItem.AddAnotherExerciseItem -> VIEW_TYPE_ADD_ANOTHER_EXERCISE
        }
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
            Exercise(item.exerciseId.toInt(), item.exerciseGroupId, item.exerciseDate)
        } else {
            null
        }
    }

    companion object {
        private const val VIEW_TYPE_EXERCISE = 1
        private const val VIEW_TYPE_DIVIDER = 2
        private const val VIEW_TYPE_NO_EXERCISES_TODAY = 3
        private const val VIEW_TYPE_ADD_ANOTHER_EXERCISE = 4
    }
}
