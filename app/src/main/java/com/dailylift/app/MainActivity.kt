package com.dailylift.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dailylift.app.data.CompletionStore
import com.dailylift.app.data.Exercise
import com.dailylift.app.data.WorkoutDataStore
import com.dailylift.app.detail.ExerciseDetailScreen
import com.dailylift.app.today.TodayScreen
import com.dailylift.app.today.TodayViewModel
import com.dailylift.app.ui.theme.DailyLiftTheme

private const val PREFS_NAME = "daily_lift_prefs"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyLiftTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = LocalContext.current
                    val viewModel = remember { context.createTodayViewModel() }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshToday()
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }

                    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
                    val exercise = selectedExercise

                    if (exercise != null) {
                        ExerciseDetailScreen(
                            exercise = exercise,
                            onBack = { selectedExercise = null },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        )
                    } else {
                        TodayScreen(
                            uiState = viewModel.uiState,
                            onNavigate = viewModel::navigate,
                            onToggleChecked = viewModel::toggleExerciseChecked,
                            onUpdateWeight = viewModel::updateWeight,
                            onUpdateReps = viewModel::updateReps,
                            onRename = viewModel::renameExercise,
                            onAddExercise = viewModel::addExercise,
                            onDeleteExercise = viewModel::deleteExercise,
                            onExerciseClick = { selectedExercise = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}

private fun Context.createTodayViewModel() = TodayViewModel(
    workoutDataStore = WorkoutDataStore(filesDir),
    completionStore = CompletionStore(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
)
