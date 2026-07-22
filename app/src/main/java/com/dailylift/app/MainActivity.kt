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
import com.dailylift.app.data.Exercise
import com.dailylift.app.detail.ExerciseDetailScreen
import com.dailylift.app.today.TodayScreen
import com.dailylift.app.ui.theme.DailyLiftTheme

/** Widget deep-link: opens straight to this exercise's detail screen, if it still exists. */
const val EXTRA_EXERCISE_ID = "com.dailylift.app.EXTRA_EXERCISE_ID"

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
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> {
                                    viewModel.refreshToday()
                                    viewModel.reloadFromDisk()
                                    // Unconditional, unlike refreshToday's own onDataChanged, which
                                    // only fires when the weekday actually changed: opening the app
                                    // is a cheap moment to repair a widget that drifted for any reason.
                                    context.refreshWidget()
                                }
                                // Leaving the app is the moment just before the widget gets looked
                                // at, so this is what makes an edit appear to be there already
                                // rather than arriving a beat late.
                                Lifecycle.Event.ON_PAUSE -> context.refreshWidget()
                                else -> Unit
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }

                    var selectedExercise by remember {
                        val deepLinkedId = intent.getStringExtra(EXTRA_EXERCISE_ID)
                        mutableStateOf(deepLinkedId?.let { viewModel.findExercise(it) })
                    }
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
