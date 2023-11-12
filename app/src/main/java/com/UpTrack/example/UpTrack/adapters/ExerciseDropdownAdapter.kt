package com.UpTrack.example.UpTrack.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.UpTrack.example.UpTrack.R
import com.UpTrack.example.UpTrack.data.models.ExerciseDropdownItem

class ExerciseDropdownAdapter(context: Context, items: List<ExerciseDropdownItem>) :
    ArrayAdapter<ExerciseDropdownItem>(context, 0, items) {

    init {
        items.forEachIndexed { index, item ->
            Log.d("ExerciseDropdownAdapter", "Item at position $index: $item")
        }
    }
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.spinner_selected_exercise_item, parent, false)
        val item = getItem(position)
        if (item is ExerciseDropdownItem.Exercise) {
            val textView: TextView = view.findViewById(R.id.textViewSelectedExerciseName)
            textView.text = item.name
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) ?: return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)

        return when (item) {
            is ExerciseDropdownItem.Exercise -> {
                val view = convertView ?: inflater.inflate(R.layout.spinner_dropdown_item, parent, false)
                val nameTextView: TextView = view.findViewById(R.id.textViewExerciseName)
                val lastTrainedTextView: TextView = view.findViewById(R.id.textViewLastTrained)

                nameTextView.text = item.name

                // Check if the item is the "Please Select..." prompt, and if so, do not display the last trained text
                if (item.name == "Please select...") {
                    lastTrainedTextView.text = "" // Clear any last trained text
                } else {
                    lastTrainedTextView.text = item.lastTrained ?: "Never"
                }

                view
            }
            is ExerciseDropdownItem.SubHeader -> {
                val view = convertView ?: inflater.inflate(R.layout.spinner_subheader, parent, false)
                val titleTextView: TextView? = view.findViewById(R.id.textViewSubHeader)
                titleTextView?.text = item.title ?: "Section"
                view
            }
        }
    }



    override fun isEnabled(position: Int): Boolean {
        // Disable selection of the "Please Select..." option and subheaders
        val item = getItem(position)
        return when (item) {
            is ExerciseDropdownItem.Exercise -> item.name != "Please select..."
            is ExerciseDropdownItem.SubHeader -> false
            else -> false
        }
    }
}

