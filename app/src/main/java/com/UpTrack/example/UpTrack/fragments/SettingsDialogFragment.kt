package com.UpTrack.example.UpTrack.fragments
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.UpTrack.example.UpTrack.R

class SettingsDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException("Activity cannot be null")

        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_settings, null)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group)
        val radioKg = view.findViewById<RadioButton>(R.id.radio_kg)
        val radioLbs = view.findViewById<RadioButton>(R.id.radio_lbs)

        val sharedPref = activity.applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val defaultUnit = "kg"
        val savedUnit = sharedPref.getString("unit_key", defaultUnit)

        if(savedUnit == "kg") {
            radioKg.isChecked = true
        } else {
            radioLbs.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val unit = if (checkedId == R.id.radio_kg) "kg" else "lbs"
            with (sharedPref.edit()) {
                putString("unit_key", unit)
                apply()
            }
        }

        builder.setView(view)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id ->
                    // Dismiss the dialog
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })

        return builder.create()
    }


}
