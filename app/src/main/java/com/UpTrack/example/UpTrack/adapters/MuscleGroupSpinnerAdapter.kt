package com.UpTrack.example.UpTrack.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.UpTrack.example.UpTrack.R
import com.UpTrack.example.UpTrack.data.predefined.PredefinedExercises

class MuscleGroupSpinnerAdapter(context: Context, private val muscleGroupNames: List<String>) : ArrayAdapter<String>(context, 0, muscleGroupNames) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.muscle_group_spinner_item, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.imageViewMuscleGroupSpinner)
        val textView = view.findViewById<TextView>(R.id.textViewMuscleGroupSpinner)

        val muscleGroupName = muscleGroupNames[position]
        textView.text = muscleGroupName

        val iconRes = when (muscleGroupName) {
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

        if (iconRes != null) {
            imageView.setImageResource(iconRes)
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE
        }
        return view
    }
}

