package com.dailylift.app.today

import com.dailylift.app.data.Completion
import com.dailylift.app.data.Weekday
import com.dailylift.app.data.createSeedWorkoutData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayUiStateTest {

    private val workoutData = createSeedWorkoutData()

    /** C1: each Monday-Friday weekday maps to its seeded focus label and exercise list. */
    @Test
    fun workoutDayShowsItsSeededFocusAndExercises() {
        val day = workoutData.getValue(Weekday.WEDNESDAY)

        val state = buildTodayUiState(
            workoutData = workoutData,
            completion = Completion(date = "2026-06-17"),
            today = Weekday.MONDAY,
            viewedWeekday = Weekday.WEDNESDAY,
        )

        val content = state.content as DayContent.Workout
        assertEquals(day.focus, content.focus)
        assertEquals(day.exercises.map { it.id }, content.rows.map { it.exercise.id })
    }

    /** C2: Saturday and Sunday both map to the rest-day state. */
    @Test
    fun weekendDaysMapToRestState() {
        listOf(Weekday.SATURDAY, Weekday.SUNDAY).forEach { weekend ->
            val state = buildTodayUiState(
                workoutData = workoutData,
                completion = Completion(date = "2026-06-20"),
                today = Weekday.MONDAY,
                viewedWeekday = weekend,
            )

            assertTrue("Expected rest state for $weekend", state.content is DayContent.Rest)
        }
    }

    /** Rest day's "Next up" previews Monday's first three exercises (Decision 5, item #1 follow-on). */
    @Test
    fun restDayPreviewsMondaysFirstExercises() {
        val monday = workoutData.getValue(Weekday.MONDAY)

        val state = buildTodayUiState(
            workoutData = workoutData,
            completion = Completion(date = "2026-06-20"),
            today = Weekday.MONDAY,
            viewedWeekday = Weekday.SATURDAY,
        )

        val content = state.content as DayContent.Rest
        assertEquals(monday.focus, content.nextUpFocus)
        assertEquals(monday.exercises.take(3).map { it.name }, content.nextUpExercises)
        assertTrue(content.hasMore)
    }

    /** C8: viewing a non-today day always shows its exercises as unchecked, regardless of stored completion. */
    @Test
    fun nonTodayDayAlwaysShowsExercisesUnchecked() {
        val monday = workoutData.getValue(Weekday.MONDAY)
        val completion = Completion(date = "2026-06-15", done = monday.exercises.associate { it.id to true })

        val state = buildTodayUiState(
            workoutData = workoutData,
            completion = completion,
            today = Weekday.TUESDAY,
            viewedWeekday = Weekday.MONDAY,
        )

        val content = state.content as DayContent.Workout
        assertFalse(state.isToday)
        assertTrue(content.rows.all { !it.checked })
    }

    /** Viewing today reflects the stored completion map. */
    @Test
    fun todaysViewReflectsStoredCompletion() {
        val monday = workoutData.getValue(Weekday.MONDAY)
        val firstId = monday.exercises.first().id
        val completion = Completion(date = "2026-06-15", done = mapOf(firstId to true))

        val state = buildTodayUiState(
            workoutData = workoutData,
            completion = completion,
            today = Weekday.MONDAY,
            viewedWeekday = Weekday.MONDAY,
        )

        val content = state.content as DayContent.Workout
        assertTrue(state.isToday)
        assertTrue(content.rows.first { it.exercise.id == firstId }.checked)
        assertTrue(content.rows.filter { it.exercise.id != firstId }.all { !it.checked })
    }

    @Test
    fun resetCompletionIfStaleDropsYesterdaysData() {
        val stale = Completion(date = "2026-06-14", done = mapOf("ex-1" to true))

        assertEquals(Completion(date = "2026-06-15"), resetCompletionIfStale(stale, todayDate = "2026-06-15"))
    }

    @Test
    fun resetCompletionIfStaleKeepsTodaysData() {
        val today = Completion(date = "2026-06-15", done = mapOf("ex-1" to true))

        assertEquals(today, resetCompletionIfStale(today, todayDate = "2026-06-15"))
    }

    @Test
    fun resetCompletionIfStaleHandlesNoStoredCompletion() {
        assertEquals(Completion(date = "2026-06-15"), resetCompletionIfStale(null, todayDate = "2026-06-15"))
    }

    /** D3: weight display text - "" -> "—", "bodyweight" -> "Body" (no unit suffix), numeric -> "{value} lb". */
    @Test
    fun weightDisplayShowsDashForEmptyBodyForBodyweightAndPoundsForNumeric() {
        assertEquals("—", weightDisplay(""))
        assertEquals("Body", weightDisplay("bodyweight"))
        assertEquals("Body", weightDisplay("BodyWeight"))
        assertEquals("15 lb", weightDisplay("15"))
    }

    /** D11: the "—" empty-weight indicator has a spoken label ("no weight set"), not a literal dash. */
    @Test
    fun weightAccessibilityLabelDescribesEmptyBodyweightAndNumericWeights() {
        assertEquals("no weight set", weightAccessibilityLabel(""))
        assertEquals("Bodyweight", weightAccessibilityLabel("bodyweight"))
        assertEquals("Bodyweight", weightAccessibilityLabel("BodyWeight"))
        assertEquals("15 pounds", weightAccessibilityLabel("15"))
    }

    /** E3: the all-done message triggers when every exercise for today is checked and the list is non-empty. */
    @Test
    fun isAllDoneTrueWhenEveryExerciseForTodayIsChecked() {
        val monday = workoutData.getValue(Weekday.MONDAY)
        val completion = Completion(date = "2026-06-15", done = monday.exercises.associate { it.id to true })

        val state = buildTodayUiState(
            workoutData = workoutData,
            completion = completion,
            today = Weekday.MONDAY,
            viewedWeekday = Weekday.MONDAY,
        )

        assertTrue(isAllDone(state.content))
    }

    /** E3: not all-done while at least one exercise for today is still unchecked. */
    @Test
    fun isAllDoneFalseWhenAnyExerciseForTodayIsUnchecked() {
        val monday = workoutData.getValue(Weekday.MONDAY)
        val completion = Completion(date = "2026-06-15", done = mapOf(monday.exercises.first().id to true))

        val state = buildTodayUiState(
            workoutData = workoutData,
            completion = completion,
            today = Weekday.MONDAY,
            viewedWeekday = Weekday.MONDAY,
        )

        assertFalse(isAllDone(state.content))
    }

    /** E4: the all-done message never triggers on a Rest Day (Sat/Sun). */
    @Test
    fun isAllDoneFalseOnRestDay() {
        val state = buildTodayUiState(
            workoutData = workoutData,
            completion = Completion(date = "2026-06-20"),
            today = Weekday.SATURDAY,
            viewedWeekday = Weekday.SATURDAY,
        )

        assertFalse(isAllDone(state.content))
    }

    /** E4: the all-done message never triggers on an empty weekday (shown as "Rest Day"). */
    @Test
    fun isAllDoneFalseOnEmptyWorkoutDay() {
        val emptyData = workoutData + (Weekday.MONDAY to workoutData.getValue(Weekday.MONDAY).copy(exercises = emptyList()))

        val state = buildTodayUiState(
            workoutData = emptyData,
            completion = Completion(date = "2026-06-15"),
            today = Weekday.MONDAY,
            viewedWeekday = Weekday.MONDAY,
        )

        assertFalse(isAllDone(state.content))
    }
}
