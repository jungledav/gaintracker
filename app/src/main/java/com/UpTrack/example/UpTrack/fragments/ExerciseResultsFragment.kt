package com.UpTrack.example.UpTrack.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.UpTrack.example.UpTrack.GainTrackerApplication
import com.UpTrack.example.UpTrack.R
import com.UpTrack.example.UpTrack.viewmodels.MainViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val maxWeightCardView = view.findViewById<MaterialCardView>(R.id.card_max_weight)
        val card_max_rep = view.findViewById<MaterialCardView>(R.id.card_max_rep)
        val card_total_rep = view.findViewById<MaterialCardView>(R.id.card_total_rep)
        val card_one_max_rep = view.findViewById<MaterialCardView>(R.id.one_max_rep)
        val card_exercise_volume = view.findViewById<MaterialCardView>(R.id.card_exercise_volume)
        val card_max_set_volume = view.findViewById<MaterialCardView>(R.id.card_max_set_volume)
        //val barChart: BarChart = view.findViewById(R.id.barChart)
        val lineChart: LineChart = view.findViewById(R.id.lineChart)
        var dataChoice = "default"

        if (!exerciseSetExists) {
            Log.d("ExerciseResultsFragment", "Entered if block when exerciseSetExists is $exerciseSetExists")

            // If there is no set for the exerciseGroupId, show a message and return
            tvMessage.text = "Results will be shown after a first set is added."
            tvMessage.visibility = View.VISIBLE
            maxWeightCardView.visibility = View.GONE
            card_max_rep.visibility = View.GONE
            card_total_rep.visibility = View.GONE
            card_one_max_rep.visibility = View.GONE
            card_exercise_volume.visibility = View.GONE
            card_max_set_volume.visibility = View.GONE
            lineChart.visibility = View.GONE
        } else {

            Log.d("ExerciseResultsFragment", "Entered else block when exerciseSetExists is $exerciseSetExists")
            // reversing view setting first
            tvMessage.visibility = View.GONE
            maxWeightCardView.visibility = View.VISIBLE
            card_max_rep.visibility = View.VISIBLE
            card_total_rep.visibility = View.VISIBLE
            card_one_max_rep.visibility = View.VISIBLE
            card_exercise_volume.visibility = View.VISIBLE
            card_max_set_volume.visibility = View.VISIBLE
           // barChart.visibility = View.VISIBLE
            lineChart.visibility = View.VISIBLE


// Values for "Max Weight Lifted" Card on Exercise Result tab
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
            maxWeightCardView.setOnClickListener {
                dataChoice = "maxweight"
                viewModel.getMaxWeightOverTime(exerciseId).observe(viewLifecycleOwner, { data ->
                    val chartData = data.map { Pair(Date(it.exerciseDate), it.maxWeight) } // Convert Long to Date
                    updateLineChart(lineChart, chartData, dataChoice)

                })

            }
 // Values for "Calculated One Max Rep" Card
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
            card_one_max_rep.setOnClickListener {
                dataChoice = "onemaxrep"
                viewModel.getMaxOneRepOverTime(exerciseId).observe(viewLifecycleOwner, { data ->
                    updateLineChart(lineChart, data, dataChoice)
                })
            }

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
            card_total_rep.setOnClickListener {
                dataChoice = "MaxRepsOneWorkout"
                viewModel.getMaxRepsInWorkoutOverTime(exerciseId).observe(viewLifecycleOwner, { data ->
                    updateLineChart(lineChart, data,dataChoice)
                })
            }


// Values for "Max Workout Volume" Card

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
            card_exercise_volume.setOnClickListener {
                dataChoice = "MaxWorkoutVolume"
                viewModel.getMaxWorkoutVolumeOverTime(exerciseId).observe(viewLifecycleOwner, { data ->
                    updateLineChart(lineChart, data,dataChoice)
                })
            }

// Values for "Max Set Volume" Card
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
            card_max_set_volume.setOnClickListener {
                dataChoice = "MaxSetVolume"
                viewModel.getMaxSetVolumeOverTime(exerciseId).observe(viewLifecycleOwner, { data ->
                    updateLineChart(lineChart, data,dataChoice)
                })
            }

// Values for "Max reps in One Set" Card

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

            card_max_rep.setOnClickListener {
                Log.d("FragmentDebug", "Card Clicked!")

                dataChoice = "MaxRepsOneSet"
                viewModel.getMaxRepsOneSetOverTime(exerciseId).observe(viewLifecycleOwner, { data ->
                    updateLineChart(lineChart, data,dataChoice)
                    Log.d("FragmentDebug", "MaxRepsOneSet Data sent to chart $data")

                })

            }
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

    private fun updateLineChart(lineChart: LineChart, data: List<Pair<Date, Float>>, dataChoice: String) {
        // Convert your data to 'entries'
        val entries = data.reversed().mapIndexed { index, (_, weight) ->
            Entry(index.toFloat(), weight)
        }
        val dataSet = LineDataSet(entries, "Max Weight")

        // Set line mode and styles
        dataSet.mode = LineDataSet.Mode.LINEAR
        dataSet.setDrawCircles(true)
        dataSet.setDrawCircleHole(true)

        context?.let {
            // Set color of the line and the circle
            dataSet.color = ContextCompat.getColor(it, R.color.black)
            dataSet.setCircleColor(ContextCompat.getColor(it, R.color.black))
        }

        // Create a LineData object and set it to the chart
        val lineData = LineData(dataSet)
        lineChart.clear()
        lineChart.data = lineData
        lineChart.legend.isEnabled = false
        lineChart.description.isEnabled = false

        // X-Axis customization with Date labels
        val dates = data.map { it.first }.reversed()
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() >= 0 && value.toInt() < dates.size) {
                    dateFormat.format(dates[value.toInt()])
                } else {
                    ""
                }
            }
        }

        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.granularity = 1f

        // Determine the window size based on the data
        val numDays = if (dates.isNotEmpty()) {
            val diff = daysBetween(dates.first(), dates.last())
            Math.min(diff, 90f) // We want a max of 3 months, i.e., approx 90 days
        } else {
            0f
        }
       // lineChart.xAxis.axisMaximum = numDays
        lineChart.xAxis.axisMaximum = data.size.toFloat() - 1


        val tvChartDescription = view?.findViewById<TextView>(R.id.tv_chart_description)
        tvChartDescription?.text = when(dataChoice) {
            "maxwight" -> "Max Weight over last 3 months"
            "onemaxrep" -> "Calculated maximum one rep over last 3 months"
            "MaxRepsOneSet" -> "Maximum Reps in one Set over last 3 months"
            "MaxRepsOneWorkout" -> "Maximum Reps in one Workout over last 3 months"
            "MaxSetVolume" -> "Maximum Volume in one Set over last 3 months"
            "MaxWorkoutVolume" -> "Maximum Volume in one Workout over last 3 months"
            else -> "Max Weight over last 3 months"
        }

        lineChart.invalidate() // refreshes the chart
    }
    private fun daysBetween(d1: Date, d2: Date): Float {
        return TimeUnit.DAYS.convert(d2.time - d1.time, TimeUnit.MILLISECONDS).toFloat()
    }






}

