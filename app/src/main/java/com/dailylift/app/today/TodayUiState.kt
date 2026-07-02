package com.dailylift.app.today

import com.dailylift.app.data.Completion
import com.dailylift.app.data.Exercise
import com.dailylift.app.data.Weekday
import com.dailylift.app.data.WorkoutData

/** How many of [DayContent.Rest]'s next-up exercises to preview, mirroring the prototype's `slice(0,3)`. */
private const val NEXT_UP_PREVIEW_COUNT = 3

/** A single exercise row as shown on the Today screen. */
data class ExerciseRow(
    val exercise: Exercise,
    val checked: Boolean,
)

/** The main content area of the Today screen: either a workout day's exercise list or the rest-day message. */
sealed interface DayContent {
    data class Workout(val focus: String, val rows: List<ExerciseRow>) : DayContent
    data class Rest(val nextUpFocus: String, val nextUpExercises: List<String>, val hasMore: Boolean) : DayContent
}

/** Everything [com.dailylift.app.today.TodayScreen] needs to render the currently viewed day. */
data class TodayUiState(
    val viewedWeekday: Weekday,
    val isToday: Boolean,
    val content: DayContent,
)

/**
 * Builds the Today screen's state for [viewedWeekday].
 *
 * [isToday] and [DayContent.Workout.rows]' checked state come from [today]/[completion] — viewing a
 * non-today day always shows its exercises as unchecked (C8), since [Completion] only tracks today.
 */
fun buildTodayUiState(
    workoutData: WorkoutData,
    completion: Completion,
    today: Weekday,
    viewedWeekday: Weekday,
): TodayUiState {
    val isToday = viewedWeekday == today
    val content = if (viewedWeekday.isWorkoutDay()) {
        val day = workoutData.getValue(viewedWeekday)
        val rows = day.exercises.map { exercise ->
            ExerciseRow(
                exercise = exercise,
                checked = isToday && completion.done[exercise.id] == true,
            )
        }
        DayContent.Workout(focus = day.focus, rows = rows)
    } else {
        val monday = workoutData.getValue(Weekday.MONDAY)
        val preview = monday.exercises.take(NEXT_UP_PREVIEW_COUNT).map { it.name }
        DayContent.Rest(
            nextUpFocus = monday.focus,
            nextUpExercises = preview,
            hasMore = monday.exercises.size > preview.size,
        )
    }
    return TodayUiState(viewedWeekday = viewedWeekday, isToday = isToday, content = content)
}

/** Returns [stored] if it's still for [todayDate], otherwise a fresh, empty [Completion] for today. */
fun resetCompletionIfStale(stored: Completion?, todayDate: String): Completion =
    if (stored?.date == todayDate) stored else Completion(date = todayDate)

/**
 * Step E, point 8: the all-done celebration triggers only for a non-empty workout day where every
 * exercise is checked. Naturally false for [DayContent.Rest] and for an empty day (shown as
 * "Rest Day"), and naturally false for a non-today view since [ExerciseRow.checked] is always
 * false there (C8) - no separate "isToday" check is needed.
 */
fun isAllDone(content: DayContent): Boolean =
    content is DayContent.Workout && content.rows.isNotEmpty() && content.rows.all { it.checked }

/** Display text for [Exercise.weight]: `""` -> "—", `"bodyweight"` -> "Body", numeric -> "{value} lb". */
fun weightDisplay(weight: String): String = when {
    weight.isEmpty() -> "—"
    weight.equals("bodyweight", ignoreCase = true) -> "Body"
    else -> "$weight lb"
}

/** Spoken accessibility label for [weightDisplay] (D11: "—" reads as "no weight set", not a literal dash). */
fun weightAccessibilityLabel(weight: String): String = when {
    weight.isEmpty() -> "no weight set"
    weight.equals("bodyweight", ignoreCase = true) -> "Bodyweight"
    else -> "$weight pounds"
}
