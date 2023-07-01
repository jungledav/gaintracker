package com.example.gaintracker.fragments

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.room.InvalidationTracker
import com.example.gaintracker.GainTrackerApplication
import com.example.gaintracker.R
import com.example.gaintracker.viewmodels.MainViewModel
import com.google.android.material.card.MaterialCardView
import java.util.Date
import java.util.Locale

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
        viewModel.getExerciseGroupId(exerciseId).observe(viewLifecycleOwner) { exerciseGroupId ->
            viewModel.doesExerciseSetExist(exerciseGroupId)
                .observe(viewLifecycleOwner) { exerciseSetExists ->
                    if (!exerciseSetExists) {
                        // If there is no set for the exerciseGroupId, show a message and return
                        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
                        tvMessage.text = "No results yet"
                        tvMessage.visibility = View.VISIBLE
                        val maxWeightCardView = view.findViewById<MaterialCardView>(R.id.card_max_weight)
                        maxWeightCardView.visibility = View.GONE
                        val card_max_rep = view.findViewById<MaterialCardView>(R.id.card_max_rep)
                        card_max_rep.visibility = View.GONE
                        val card_total_rep = view.findViewById<MaterialCardView>(R.id.card_total_rep)
                        card_total_rep.visibility = View.GONE
                        val card_exercise_volume = view.findViewById<MaterialCardView>(R.id.card_exercise_volume)
                        card_exercise_volume.visibility = View.GONE
                        val card_max_set_volume = view.findViewById<MaterialCardView>(R.id.card_max_set_volume)
                        card_max_set_volume.visibility = View.GONE

                    } else {
// Values for Max Weight Card on Exercise Result tab
                        val tvMaxWeightDate = view.findViewById<TextView>(R.id.tv_max_weight_date)
                        val tvMaxWeightValue = view.findViewById<TextView>(R.id.tv_max_weight_value)
                        val tvMaxWeightToday = view.findViewById<TextView>(R.id.tv_max_weight_today)
                        viewModel.getMaxTotalRepsForExerciseGroup(exerciseId)

                        viewModel.getMaxWeightForExerciseType(exerciseId)
                            .observe(viewLifecycleOwner, { maxWeight ->
                                tvMaxWeightValue.text = "$maxWeight kg"
                            })

                        viewModel.getMaxWeightDateForExercise(exerciseId)
                            .observe(viewLifecycleOwner, { maxWeightDate ->
                                tvMaxWeightDate.text = "$maxWeightDate"
                            })

                        viewModel.getTodayMaxWeightForExercise(exerciseId)
                            .observe(viewLifecycleOwner, { maxWeightToday ->
                                tvMaxWeightToday.text = "This workout: $maxWeightToday kg"
                            })

                        /// Values for Max One Rep Card
                        val tvOneMaxRep = view.findViewById<TextView>(R.id.tv_one_max_rep_value)
                        val tvOneMaxRepDate = view.findViewById<TextView>(R.id.tv_one_max_rep_date)
                        viewModel.calculateGroupMaxOneRep(exerciseId)
                        viewModel.groupMaxOneRep.observe(viewLifecycleOwner, { oneGroupRepMax ->
                            oneGroupRepMax?.let {
                                val formattedOnegroupRepMax = String.format("%.2f", it)
                                tvOneMaxRep.text = "$formattedOnegroupRepMax kg"
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
                                tvOneMaxRepToday.text = "This workout: $formattedOneRepMax kg"
                            } ?: run {
                                tvOneMaxRepToday.text = "N/A"
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
                                tvTotalRepsToday.text = "This workout: $totalRepsToday reps"
                            })


// Values for "Max Exercise per workout Volume" Card

                        val tvExerciseVolumetoday = view.findViewById<TextView>(R.id.tv_exercise_volume_today)
                        viewModel.getExerciseVolumeForExercise(exerciseId)
                            .observe(viewLifecycleOwner, { exerciseVolume ->
                                tvExerciseVolumetoday.text = "This workout: $exerciseVolume kg"
                            })
                        val tvMaxExerciseVolumeDate = view.findViewById<TextView>(R.id.tv_exercise_volume_date)
                        val tvMaxExerciseVolumeTotal = view.findViewById<TextView>(R.id.tv_exercise_volume_value)
                        viewModel.getMaxExerciseVolumeForGroup(exerciseId)
                            .observe(viewLifecycleOwner, { MaxExerciseVolumeForGroup ->
                                tvMaxExerciseVolumeDate.text = "${MaxExerciseVolumeForGroup.date}"
                                tvMaxExerciseVolumeTotal.text = "${MaxExerciseVolumeForGroup.max_volume} kg"
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
                                tvMaxSetVolumeValueTotal.text = "${exerciseSetVolume.max_volume} kg"
                            })

                        viewModel.getMaxSetVolumeForExercise(exerciseId)
                            .observe(viewLifecycleOwner, { maxSetVolume ->
                                tvMaxSetVolumetoday.text = "This workout: $maxSetVolume kg"
                            })


                        val tvMaxRepsGroupDate = view.findViewById<TextView>(R.id.tv_max_reps_date)
                        val tvMaxRepsValue = view.findViewById<TextView>(R.id.tv_max_reps_value)
                        val tvMaxRepsGroup = view.findViewById<TextView>(R.id.tv_max_reps_group)

                        viewModel.getMaxRepsForExercise(exerciseId)
                            .observe(viewLifecycleOwner, { maxReps ->
                                tvMaxRepsValue.text = "This workout: $maxReps reps"
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
        }
        return view
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

