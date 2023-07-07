package com.UpTrack.example.UpTrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.UpTrack.example.UpTrack.adapters.ExerciseAdapter
import com.UpTrack.example.UpTrack.data.models.*
import com.UpTrack.example.UpTrack.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.UpTrack.example.UpTrack.viewmodels.MainViewModelFactory
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*
import androidx.lifecycle.Observer
import com.UpTrack.example.UpTrack.viewmodels.ExerciseDetailsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView



interface OnNoExercisesTodayClickListener {
    fun onNoExercisesTodayClick()
}

interface onAddAnotherExerciseClickListener {
    fun onAddAnotherExerciseClick()
}

class MainActivity : BaseActivity(), onAddAnotherExerciseClickListener,OnNoExercisesTodayClickListener {

    private lateinit var adapter: ExerciseAdapter
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(appContainer.mainRepository) }
    private val appContainer by lazy {
        (application as GainTrackerApplication).appContainer
    }

    companion object {
        private const val ADD_EXERCISE_REQUEST_CODE = 1
        const val EXTRA_EXERCISE_ID = "extra_exercise_id"
        const val EXTRA_EXERCISE_NAME = "extra_exercise_name"
    }

    override fun onNoExercisesTodayClick() {
        openAddExerciseActivity()
    }

   override fun onAddAnotherExerciseClick() {
        openAddExerciseActivity()
    }
    private fun generateExerciseListItems(exercisesWithGroupNames: List<ExerciseWithGroupName>): List<ExerciseListItem> {
        val items = mutableListOf<ExerciseListItem>()
        var previousDate: Date? = null
        var currentDate: Date
        var isExerciseForCurrentDayAdded = false

        exercisesWithGroupNames.forEach { exerciseWithGroupName ->
            currentDate = Date(exerciseWithGroupName.exercise.date)

            if (previousDate?.let { isSameDay(it, currentDate) } != true) {
                items.add(ExerciseListItem.DividerItem(currentDate))
                if (isSameDay(currentDate, Date())) {
                    isExerciseForCurrentDayAdded = true
                }
            }

            if(isExerciseForCurrentDayAdded && isSameDay(currentDate, Date())) {
                items.add(ExerciseListItem.AddAnotherExerciseItem)
                isExerciseForCurrentDayAdded = false
            }

            items.add(
                ExerciseListItem.ExerciseItem(
                    exercise = exerciseWithGroupName.exercise,
                    exerciseGroupName = exerciseWithGroupName.groupName
                )
            )

            previousDate = currentDate
        }

        if (exercisesWithGroupNames.isEmpty() || !isSameDay(Date(), Date(exercisesWithGroupNames[0].exercise.date))) {
         //   items.add(0, ExerciseListItem.DividerItem(Date()))
            items.add(0, ExerciseListItem.NoExercisesTodayItem)
        }

        return items
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        calendar1.time = date1
        val calendar2 = Calendar.getInstance()
        calendar2.time = date2

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    private var previousDate: Date? = null

    private fun refreshExercises() {
        viewModel.allExercisesWithGroupNames.observe(this) { exercisesWithGroupNames ->
            val items = generateExerciseListItems(exercisesWithGroupNames)
            adapter.setItems(items)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshExercises()
    }

    internal fun openExerciseDetails(exerciseId: Long, exerciseName: String) {
        val intent = Intent(this, ExerciseDetailsActivity::class.java).apply {
            putExtra(ExerciseDetailsActivity.EXTRA_EXERCISE_ID, exerciseId)
            putExtra(ExerciseDetailsActivity.EXTRA_EXERCISE_NAME, exerciseName)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }
    /*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        return when (item.itemId) {
            R.id.action_exercises -> {
                // Do nothing, we're already here
                true
            }
            R.id.action_dashboard -> {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            R.id.action_settings -> {
                // Handle settings action
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.activity_content))
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.visibility = View.GONE

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewExercises)
        previousDate = null
        adapter = ExerciseAdapter({ exercise ->
            lifecycleScope.launch {
                viewModel.getSetsForExerciseFlow(exercise.id.toLong())
                    .collect { sets ->
                        val position = adapter.indexOfExercise(exercise)
                        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                        if (viewHolder is ExerciseAdapter.ExerciseViewHolder) {
                            viewHolder.updateSetsCount(sets.size)
                        }
                    }
            }
        }, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        viewModel.allExercisesWithGroupNames.observe(this) { exercisesWithGroupNames ->
            val items = generateExerciseListItems(exercisesWithGroupNames)
            adapter.setItems(items)
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val exercise = adapter.getExerciseAt(position)

                if (exercise != null) {
                    viewModel.deleteExercise(exercise)
                    adapter.notifyItemRemoved(position)
                    Snackbar.make(recyclerView, "Exercise deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            undoDeleteExercise(exercise)
                        }.show()
                } else {
                    adapter.notifyItemChanged(position) // In case it's a DividerItem, we reset the swipe
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
        adapter.setOnItemClickListener(object : ExerciseAdapter.OnItemClickListener {
            override fun onItemClick(exercise: Exercise) {
                lifecycleScope.launch {
                    val exerciseGroupName = viewModel.getExerciseGroupNameById(exercise.exerciseGroupId.toInt())
                    openExerciseDetails(exercise.id.toLong(), exerciseGroupName)
                }
            }
        })

    }

    private fun undoDeleteExercise(exercise: Exercise) {
        viewModel.getExerciseGroupName(exercise.exerciseGroupId.toInt())
            .observe(this, { exerciseName ->
                if (exerciseName != null) {
                    lifecycleScope.launch {
                        viewModel.insertExercise(exerciseName)
                    }
                }
            })
    }

   fun openAddExerciseActivity() {
        val intent = Intent(this, AddExerciseActivity::class.java)
        intent.putExtra(AddExerciseActivity.EXTRA_FROM_MAIN_ACTIVITY, true)
        startActivityForResult(intent, ADD_EXERCISE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_EXERCISE_REQUEST_CODE && resultCode == RESULT_OK) {
            val exerciseId = data?.getLongExtra(ExerciseDetailsActivity.EXTRA_EXERCISE_ID, -1) ?: -1
            val exerciseName = data?.getStringExtra(ExerciseDetailsActivity.EXTRA_EXERCISE_NAME)

            if (exerciseId != -1L && exerciseName != null) {
                Log.d(
                    "MainActivity",
                    "Received from AddExerciseActivity: ID: $exerciseId, Name: $exerciseName"
                )
                // Use exerciseId and exerciseName here...
            } else {
                Log.d("MainActivity", "No exercise received from AddExerciseActivity.")
            }
        }
    }
}
