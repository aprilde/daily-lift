package com.dailylift.app.today

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dailylift.app.data.Completion
import com.dailylift.app.data.CompletionStore
import com.dailylift.app.data.Exercise
import com.dailylift.app.data.Weekday
import com.dailylift.app.data.WorkoutData
import com.dailylift.app.data.WorkoutDataStore
import com.dailylift.app.data.WorkoutDay
import java.time.Clock

/**
 * Holds the Today screen's state: the seeded/persisted [WorkoutData], today's [Completion], which
 * weekday is currently being viewed, and "today" itself (recomputed via [refreshToday]).
 */
class TodayViewModel(
    private val workoutDataStore: WorkoutDataStore,
    private val completionStore: CompletionStore,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val onDataChanged: () -> Unit = {},
) {
    private var workoutData: WorkoutData by mutableStateOf(workoutDataStore.loadOrSeed())
    private val tracker = TodayClockTracker(clock)

    var today: Weekday by mutableStateOf(tracker.today)
        private set

    var viewedWeekday: Weekday by mutableStateOf(today)
        private set

    private var completion: Completion by mutableStateOf(loadOrResetCompletion())

    val uiState: TodayUiState
        get() = buildTodayUiState(workoutData, completion, today, viewedWeekday)

    /** Recomputes "today" from [clock]; call from an `ON_RESUME` lifecycle callback. */
    fun refreshToday() {
        tracker.refreshToday()
        if (tracker.today != today) {
            today = tracker.today
            completion = loadOrResetCompletion()
            onDataChanged()
        }
    }

    /** Moves the viewed day by [direction] (+1 = next day, -1 = previous day), wrapping around all 7 days. */
    fun navigate(direction: Int) {
        viewedWeekday = viewedWeekday.next(direction)
    }

    /** Looks up [exerciseId] across every day, or `null` if it no longer exists (e.g. deleted). */
    fun findExercise(exerciseId: String): Exercise? =
        workoutData.values.firstNotNullOfOrNull { day -> day.exercises.find { it.id == exerciseId } }

    /** Toggles [exerciseId]'s done state for today (D1). A no-op when [viewedWeekday] isn't today. */
    fun toggleExerciseChecked(exerciseId: String) {
        if (viewedWeekday != today) return
        val done = completion.done.toMutableMap()
        done[exerciseId] = !(done[exerciseId] ?: false)
        completion = completion.copy(done = done)
        completionStore.save(completion)
        onDataChanged()
    }

    /** Sets [exerciseId]'s weight to [weight] (a numeric string, or `""` for "no weight set"). */
    fun updateWeight(exerciseId: String, weight: String) =
        updateExercise(exerciseId) { it.copy(weight = weight) }

    /** Sets [exerciseId]'s reps text. */
    fun updateReps(exerciseId: String, reps: String) =
        updateExercise(exerciseId) { it.copy(reps = reps) }

    /** Renames [exerciseId], capping the new name at [Exercise.MAX_NAME_LENGTH] characters (D4). */
    fun renameExercise(exerciseId: String, name: String) =
        updateExercise(exerciseId) { it.copy(name = name.take(Exercise.MAX_NAME_LENGTH)) }

    /** Appends a new placeholder exercise to the viewed day (D5). */
    fun addExercise() = updateViewedDay { day -> day.copy(exercises = day.exercises + Exercise.new()) }

    /** Removes [exerciseId] from the viewed day and from today's completion map (D6). */
    fun deleteExercise(exerciseId: String) {
        updateViewedDay { day -> day.copy(exercises = day.exercises.filterNot { it.id == exerciseId }) }
        if (exerciseId in completion.done) {
            completion = completion.copy(done = completion.done - exerciseId)
            completionStore.save(completion)
        }
    }

    private fun updateExercise(exerciseId: String, transform: (Exercise) -> Exercise) {
        updateViewedDay { day ->
            day.copy(exercises = day.exercises.map { if (it.id == exerciseId) transform(it) else it })
        }
    }

    private fun updateViewedDay(transform: (WorkoutDay) -> WorkoutDay) {
        val day = workoutData.getValue(viewedWeekday)
        workoutData = workoutData + (viewedWeekday to transform(day))
        workoutDataStore.save(workoutData)
        onDataChanged()
    }

    private fun loadOrResetCompletion(): Completion {
        val resolved = resetCompletionIfStale(completionStore.load(), currentDateString(clock))
        completionStore.save(resolved)
        return resolved
    }
}
