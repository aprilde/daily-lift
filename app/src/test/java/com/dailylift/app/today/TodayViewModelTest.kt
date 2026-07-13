package com.dailylift.app.today

import com.dailylift.app.data.CompletionStore
import com.dailylift.app.data.Exercise
import com.dailylift.app.data.Weekday
import com.dailylift.app.data.WorkoutDataStore
import com.dailylift.app.testutil.FakeSharedPreferences
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** 2024-01-01 is a known Monday (matches [TodayCalendarTest]'s anchor date). */
private val MONDAY_CLOCK: Clock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneOffset.UTC)

class TodayViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun newViewModel(clock: Clock = MONDAY_CLOCK): Pair<TodayViewModel, CompletionStore> {
        val completionStore = CompletionStore(FakeSharedPreferences())
        val viewModel = TodayViewModel(
            workoutDataStore = WorkoutDataStore(tempFolder.newFolder()),
            completionStore = completionStore,
            clock = clock,
        )
        return viewModel to completionStore
    }

    private fun TodayViewModel.workoutRows(): List<ExerciseRow> = (uiState.content as DayContent.Workout).rows

    /** D1: checking off an exercise updates the completion map keyed by id (not position); unchecking reverses it. */
    @Test
    fun toggleExerciseCheckedTogglesCompletionByIdNotPosition() {
        val (viewModel, _) = newViewModel()
        val seededRows = viewModel.workoutRows()
        val firstId = seededRows[0].exercise.id
        val secondId = seededRows[1].exercise.id

        viewModel.toggleExerciseChecked(firstId)

        var rows = viewModel.workoutRows()
        assertTrue(rows.first { it.exercise.id == firstId }.checked)
        assertFalse(rows.first { it.exercise.id == secondId }.checked)

        viewModel.toggleExerciseChecked(firstId)

        rows = viewModel.workoutRows()
        assertFalse(rows.first { it.exercise.id == firstId }.checked)
    }

    /** D2: typing "bodyweight" into the free-text weight field is a normal weight update (Decision 5, edge case #2). */
    @Test
    fun updateWeightAcceptsFreeTextBodyweightEntry() {
        val (viewModel, _) = newViewModel()
        val exerciseId = viewModel.workoutRows().first().exercise.id

        viewModel.updateWeight(exerciseId, "bodyweight")
        var row = viewModel.workoutRows().first { it.exercise.id == exerciseId }
        assertEquals("bodyweight", row.exercise.weight)
        assertEquals("Body", weightDisplay(row.exercise.weight))

        viewModel.updateWeight(exerciseId, "")
        row = viewModel.workoutRows().first { it.exercise.id == exerciseId }
        assertEquals("", row.exercise.weight)
    }

    /** D4: rename input enforces the ~30-character cap (Decision 5, edge case #3). */
    @Test
    fun renameExerciseEnforcesMaxNameLength() {
        val (viewModel, _) = newViewModel()
        val exerciseId = viewModel.workoutRows().first().exercise.id
        val longName = "Extremely long exercise name that goes on and on"

        viewModel.renameExercise(exerciseId, longName)

        val row = viewModel.workoutRows().first { it.exercise.id == exerciseId }
        assertEquals(Exercise.MAX_NAME_LENGTH, row.exercise.name.length)
        assertEquals(longName.take(Exercise.MAX_NAME_LENGTH), row.exercise.name)
    }

    /**
     * D5: "Add exercise" appends a new exercise with a unique id, default tip, and placeholder
     * image refs. Also covers E2 (a custom/added exercise resolves to the generic placeholder
     * pair, Decision 5 edge case #9) - same assertions, no separate test needed.
     */
    @Test
    fun addExerciseAppendsNewPlaceholderExerciseWithUniqueIdAndDefaults() {
        val (viewModel, _) = newViewModel()
        val existingIds = viewModel.workoutRows().map { it.exercise.id }.toSet()

        viewModel.addExercise()

        val rows = viewModel.workoutRows()
        assertEquals(existingIds.size + 1, rows.size)
        val added = rows.last().exercise
        assertFalse(added.id in existingIds)
        assertEquals("10", added.reps)
        assertEquals(Exercise.PLACEHOLDER_IMAGE_START_REF, added.imageStartRef)
        assertEquals(Exercise.PLACEHOLDER_IMAGE_END_REF, added.imageEndRef)
        assertTrue(added.tip.isNotBlank())
    }

    /** D6: deleting an exercise removes it and its completion entry, without corrupting other exercises' state. */
    @Test
    fun deleteExerciseRemovesItAndItsCompletionEntryWithoutCorruptingOthers() {
        val (viewModel, completionStore) = newViewModel()
        val seededRows = viewModel.workoutRows()
        val originalCount = seededRows.size
        val firstId = seededRows[0].exercise.id
        val secondId = seededRows[1].exercise.id

        viewModel.toggleExerciseChecked(firstId)
        viewModel.toggleExerciseChecked(secondId)

        viewModel.deleteExercise(firstId)

        val rows = viewModel.workoutRows()
        assertEquals(originalCount - 1, rows.size)
        assertTrue(rows.none { it.exercise.id == firstId })
        // The remaining exercise's completion state is unaffected (keyed by id, not position).
        assertTrue(rows.first { it.exercise.id == secondId }.checked)
        assertFalse(completionStore.load()!!.done.containsKey(firstId))
    }

    /** D12: refreshing after the date changes resets the persisted completion map to empty for the new date. */
    @Test
    fun refreshTodayResetsCompletionWhenDateChanges() {
        val clock = MutableClock(Instant.parse("2024-01-01T23:00:00Z"), ZoneOffset.UTC)
        val (viewModel, completionStore) = newViewModel(clock)
        val exerciseId = viewModel.workoutRows().first().exercise.id

        viewModel.toggleExerciseChecked(exerciseId)
        assertTrue(completionStore.load()!!.done.containsKey(exerciseId))

        clock.instant = Instant.parse("2024-01-02T01:00:00Z")
        viewModel.refreshToday()

        assertEquals(Weekday.TUESDAY, viewModel.today)
        val reloaded = completionStore.load()
        assertNotNull(reloaded)
        assertEquals("2024-01-02", reloaded!!.date)
        assertTrue(reloaded.done.isEmpty())
    }

    /**
     * The widget-refresh hook: `onDataChanged` fires on every mutation path (toggle, weight/reps/
     * rename/add/delete, and a real day-rollover), never on construction alone.
     */
    @Test
    fun onDataChangedFiresOnEveryMutationButNotOnConstruction() {
        var callCount = 0
        val completionStore = CompletionStore(FakeSharedPreferences())
        val clock = MutableClock(Instant.parse("2024-01-01T12:00:00Z"), ZoneOffset.UTC)
        val viewModel = TodayViewModel(
            workoutDataStore = WorkoutDataStore(tempFolder.newFolder()),
            completionStore = completionStore,
            clock = clock,
            onDataChanged = { callCount++ },
        )
        assertEquals(0, callCount)

        val exerciseId = viewModel.workoutRows().first().exercise.id

        viewModel.toggleExerciseChecked(exerciseId)
        assertEquals(1, callCount)

        viewModel.updateWeight(exerciseId, "20")
        assertEquals(2, callCount)

        viewModel.updateReps(exerciseId, "8")
        assertEquals(3, callCount)

        viewModel.renameExercise(exerciseId, "Renamed")
        assertEquals(4, callCount)

        viewModel.addExercise()
        assertEquals(5, callCount)

        viewModel.deleteExercise(exerciseId)
        assertEquals(6, callCount)

        // No real rollover yet (still Monday) - refreshToday() is a no-op, no extra call.
        viewModel.refreshToday()
        assertEquals(6, callCount)

        // Advancing past midnight is a real rollover - fires once more.
        clock.instant = Instant.parse("2024-01-02T01:00:00Z")
        viewModel.refreshToday()
        assertEquals(7, callCount)
    }

    /** A [Clock] whose [instant] can be advanced, simulating time passing while the app is backgrounded. */
    private class MutableClock(var instant: Instant, private val zone: ZoneId) : Clock() {
        override fun getZone(): ZoneId = zone
        override fun withZone(zone: ZoneId): Clock = MutableClock(instant, zone)
        override fun instant(): Instant = instant
    }
}
