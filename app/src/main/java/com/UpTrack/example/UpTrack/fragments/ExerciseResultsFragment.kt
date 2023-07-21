package com.UpTrack.example.UpTrack.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.UpTrack.example.UpTrack.GainTrackerApplication
import com.UpTrack.example.UpTrack.R
import com.UpTrack.example.UpTrack.viewmodels.MainViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ExerciseResultsFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels {
        (requireActivity().application as GainTrackerApplication).appContainer.mainViewModelFactory
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_exercise_results, container, false)
        val exerciseId = arguments?.getLong("exerciseId")
            ?: throw IllegalStateException("No exerciseId provided")

        // Use the exerciseGroupId to fetch data from the DB
        viewModel.viewModelScope.launch {
            viewModel.getExerciseGroupId(exerciseId).asFlow().collect { exerciseGroupId ->
                viewModel.doesExerciseSetExist(exerciseGroupId).asFlow().collect { exerciseSetExists ->
                    Log.d("ExerciseResultsFragment", "ExerciseID: $exerciseId, ExerciseGroupID: $exerciseGroupId, ExerciseSetExists: $exerciseSetExists")

                    updateUI(view, exerciseSetExists, exerciseId)
                }
            }
        }
        return view
    }
    private fun updateUI(view: View, exerciseSetExists: Boolean, exerciseId: Long) {
        val savedUnit = viewModel.getSavedUnit()

        if (!exerciseSetExists) {
            Log.d("ExerciseResultsFragment", "Entered if block when exerciseSetExists is $exerciseSetExists")

            // If there is no set for the exerciseGroupId, show a message and return
            val tvMessage = view.findViewById<TextView>(R.id.tv_message)
            tvMessage.text = "Results will be shown after a first set is added."
            tvMessage.visibility = View.VISIBLE
            val maxWeightCardView = view.findViewById<MaterialCardView>(R.id.card_max_weight)
            maxWeightCardView.visibility = View.GONE
            val card_max_rep = view.findViewById<MaterialCardView>(R.id.card_max_rep)
            card_max_rep.visibility = View.GONE
            val card_total_rep = view.findViewById<MaterialCardView>(R.id.card_total_rep)
            card_total_rep.visibility = View.GONE
            val card_one_max_rep = view.findViewById<MaterialCardView>(R.id.one_max_rep)
            card_one_max_rep.visibility = View.GONE
            val card_exercise_volume = view.findViewById<MaterialCardView>(R.id.card_exercise_volume)
            card_exercise_volume.visibility = View.GONE
            val card_max_set_volume = view.findViewById<MaterialCardView>(R.id.card_max_set_volume)
            card_max_set_volume.visibility = View.GONE
        } else {

            Log.d("ExerciseResultsFragment", "Entered else block when exerciseSetExists is $exerciseSetExists")
            // reversing view setting first
            val tvMessage = view.findViewById<TextView>(R.id.tv_message)
            tvMessage.visibility = View.GONE
            val maxWeightCardView = view.findViewById<MaterialCardView>(R.id.card_max_weight)
            maxWeightCardView.visibility = View.VISIBLE
            val card_max_rep = view.findViewById<MaterialCardView>(R.id.card_max_rep)
            card_max_rep.visibility = View.VISIBLE
            val card_total_rep = view.findViewById<MaterialCardView>(R.id.card_total_rep)
            card_total_rep.visibility = View.VISIBLE
            val card_one_max_rep = view.findViewById<MaterialCardView>(R.id.one_max_rep)
            card_one_max_rep.visibility = View.VISIBLE
            val card_exercise_volume = view.findViewById<MaterialCardView>(R.id.card_exercise_volume)
            card_exercise_volume.visibility = View.VISIBLE
            val card_max_set_volume = view.findViewById<MaterialCardView>(R.id.card_max_set_volume)
            card_max_set_volume.visibility = View.VISIBLE
// Values for Max Weight Card on Exercise Result tab
            val tvMaxWeightDate = view.findViewById<TextView>(R.id.tv_max_weight_date)
            val tvMaxWeightValue = view.findViewById<TextView>(R.id.tv_max_weight_value)
            val tvMaxWeightToday = view.findViewById<TextView>(R.id.tv_max_weight_today)
            viewModel.getMaxTotalRepsForExerciseGroup(exerciseId)

            viewModel.getMaxWeightForExerciseType(exerciseId)
                .observe(viewLifecycleOwner, { maxWeight ->
                    tvMaxWeightValue.text = "$maxWeight $savedUnit"
                })

            viewModel.getMaxWeightDateForExercise(exerciseId)
                .observe(viewLifecycleOwner, { maxWeightDate ->
                    tvMaxWeightDate.text = "$maxWeightDate"
                })

            viewModel.getTodayMaxWeightForExercise(exerciseId).observe(viewLifecycleOwner, { maxWeightToday ->
                maxWeightToday?.let{
                    tvMaxWeightToday.text = "This workout: $maxWeightToday $savedUnit"
                }
                    ?: run {
                        tvMaxWeightToday.text = "This workout: N/A"
                    }
            })

            /// Values for Max One Rep Card
            val tvOneMaxRep = view.findViewById<TextView>(R.id.tv_one_max_rep_value)
            val tvOneMaxRepDate = view.findViewById<TextView>(R.id.tv_one_max_rep_date)
            viewModel.calculateGroupMaxOneRep(exerciseId)
            viewModel.groupMaxOneRep.observe(viewLifecycleOwner, { oneGroupRepMax ->
                oneGroupRepMax?.let {
                    val formattedOnegroupRepMax = String.format("%.2f", it)
                    tvOneMaxRep.text = "$formattedOnegroupRepMax $savedUnit"
                } ?: run {
                    tvOneMaxRep.text = "N/A"
                }
            })

            viewModel.groupMaxOneRepDate.observe(viewLifecycleOwner, { dateString ->
                dateString?.let {
                    tvOneMaxRepDate.text = "$dateString"
                } ?: run {
                    tvOneMaxRepDate.text = "N/A"
                }
            })

// Calculate the one rep max
            val tvOneMaxRepToday = view.findViewById<TextView>(R.id.tv_one_max_rep_today)
            viewModel.calculateOneRepMax(exerciseId)
            viewModel.oneRepMax.observe(viewLifecycleOwner, { oneRepMax ->
                oneRepMax?.let {
                    val formattedOneRepMax = String.format("%.2f", it)
                    tvOneMaxRepToday.text = "This workout: $formattedOneRepMax $savedUnit"
                } ?: run {
                    tvOneMaxRepToday.text = "This workout: N/A"
                }
            })



/// Values for "Max Reps in One Workout" Card on Exercise Result tab

            val tvTotalRepsDate = view.findViewById<TextView>(R.id.tv_total_reps_date)
            val tvTotalRepsValue = view.findViewById<TextView>(R.id.tv_total_reps_value)
            val tvTotalRepsToday = view.findViewById<TextView>(R.id.tv_total_reps_today)

            viewModel.getMaxTotalRepsForExerciseGroup(exerciseId)
                .observe(viewLifecycleOwner, { exerciseMaxReps ->
                    tvTotalRepsDate.text = exerciseMaxReps.date
                    tvTotalRepsValue.text = "${exerciseMaxReps.totalReps} reps"
                })

            viewModel.getTodayTotalRepsForExercise(exerciseId)
                .observe(viewLifecycleOwner, { totalRepsToday ->
                    totalRepsToday?.let{
                        tvTotalRepsToday.text = "This workout: $totalRepsToday reps"
                    }?: run {
                        tvTotalRepsToday.text = "This workout: N/A"}
                })


// Values for "Max Exercise per workout Volume" Card

            val tvExerciseVolumetoday = view.findViewById<TextView>(R.id.tv_exercise_volume_today)
            viewModel.getExerciseVolumeForExercise(exerciseId)
                .observe(viewLifecycleOwner, { exerciseVolume ->
                    exerciseVolume?.let{
                        tvExerciseVolumetoday.text = "This workout: $exerciseVolume $savedUnit"
                    }?: run {
                        tvExerciseVolumetoday.text = "This workout: N/A"}
                })
            val tvMaxExerciseVolumeDate = view.findViewById<TextView>(R.id.tv_exercise_volume_date)
            val tvMaxExerciseVolumeTotal = view.findViewById<TextView>(R.id.tv_exercise_volume_value)
            viewModel.getMaxExerciseVolumeForGroup(exerciseId)
                .observe(viewLifecycleOwner, { MaxExerciseVolumeForGroup ->
                    tvMaxExerciseVolumeDate.text = "${MaxExerciseVolumeForGroup.date}"
                    tvMaxExerciseVolumeTotal.text = "${MaxExerciseVolumeForGroup.max_volume} $savedUnit"
                })
// Values for "Max Set Volume in One Workout" Card
            val tvMaxSetVolumetoday =
                view.findViewById<TextView>(R.id.tv_max_set_volume_today)
            val tvMaxSetVolumeDate =
                view.findViewById<TextView>(R.id.tv_max_set_volume_date)
            val tvMaxSetVolumeValueTotal =
                view.findViewById<TextView>(R.id.tv_max_set_volume_value_total)

            viewModel.getMaxSetVolumeForGroup(exerciseId)
                .observe(viewLifecycleOwner, { exerciseSetVolume ->
                    tvMaxSetVolumeDate.text = "${exerciseSetVolume.date}"
                    tvMaxSetVolumeValueTotal.text = "${exerciseSetVolume.max_volume} $savedUnit"
                })

            viewModel.getMaxSetVolumeForExercise(exerciseId)
                .observe(viewLifecycleOwner, { maxSetVolume ->
                    maxSetVolume?.let{
                        tvMaxSetVolumetoday.text = "This workout: $maxSetVolume $savedUnit"
                    }?: run {
                        tvMaxSetVolumetoday.text = "This workout: N/A"
                    }
                })


            val tvMaxRepsGroupDate = view.findViewById<TextView>(R.id.tv_max_reps_date)
            val tvMaxRepsValue = view.findViewById<TextView>(R.id.tv_max_reps_value)
            val tvMaxRepsGroup = view.findViewById<TextView>(R.id.tv_max_reps_group)

            viewModel.getMaxRepsForExercise(exerciseId)
                .observe(viewLifecycleOwner, { maxReps ->
                    maxReps?.let{
                        tvMaxRepsValue.text = "This workout: $maxReps reps"
                    }?: run {
                        tvMaxRepsValue.text = "This workout: N/A"
                    }
                })


            viewModel.getMaxRepsForExerciseGroup(exerciseId)
                .observe(viewLifecycleOwner, { maxRepsGroup ->
                    tvMaxRepsGroup.text = "$maxRepsGroup reps"
                })

            viewModel.getMaxRepsDateForExerciseGroup(exerciseId)
                .observe(viewLifecycleOwner, { maxRepsDateGroup ->
                    tvMaxRepsGroupDate.text = "$maxRepsDateGroup"
                })
        }
        }

            companion object {
        fun newInstance(exerciseId: Long): ExerciseResultsFragment {
            val args = Bundle()
            args.putLong("exerciseId", exerciseId)
            val fragment = ExerciseResultsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

