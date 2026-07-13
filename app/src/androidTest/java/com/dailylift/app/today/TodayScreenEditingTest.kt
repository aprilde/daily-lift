package com.dailylift.app.today

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.dailylift.app.data.CompletionStore
import com.dailylift.app.data.Exercise
import com.dailylift.app.data.Weekday
import com.dailylift.app.data.WorkoutDataStore
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TodayScreenEditingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** A fresh [TodayViewModel] backed by per-test storage, with "today" fixed to [weekday]. */
    private fun newViewModel(weekday: Weekday): TodayViewModel {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.cacheDir, "today-ui-test-${UUID.randomUUID()}").apply { mkdirs() }
        val prefs = context.getSharedPreferences("today-ui-test-${UUID.randomUUID()}", Context.MODE_PRIVATE)
        return TodayViewModel(
            workoutDataStore = WorkoutDataStore(dir),
            completionStore = CompletionStore(prefs),
            clock = clockFor(weekday),
        )
    }

    /** [Weekday.MONDAY] is 2024-01-01; later weekdays are later days that same week. */
    private fun clockFor(weekday: Weekday): Clock {
        val offsetDays = Weekday.entries.indexOf(weekday).toLong()
        return Clock.fixed(Instant.parse("2024-01-01T12:00:00Z").plusSeconds(offsetDays * 86_400), ZoneOffset.UTC)
    }

    @Composable
    private fun EditableTodayScreen(viewModel: TodayViewModel, onExerciseClick: (Exercise) -> Unit = {}) {
        TodayScreen(
            uiState = viewModel.uiState,
            onNavigate = viewModel::navigate,
            onToggleChecked = viewModel::toggleExerciseChecked,
            onUpdateWeight = viewModel::updateWeight,
            onUpdateReps = viewModel::updateReps,
            onRename = viewModel::renameExercise,
            onAddExercise = viewModel::addExercise,
            onDeleteExercise = viewModel::deleteExercise,
            onExerciseClick = onExerciseClick,
        )
    }

    /** D7: deleting an exercise shows a confirm dialog before it's actually removed. */
    @Test
    fun deletingExerciseShowsConfirmDialogBeforeRemoval() {
        val viewModel = newViewModel(Weekday.MONDAY)
        val exerciseName = (viewModel.uiState.content as DayContent.Workout).rows.first().exercise.name

        composeTestRule.setContent { EditableTodayScreen(viewModel) }

        composeTestRule.onNodeWithContentDescription("Delete $exerciseName").performClick()

        // The confirm dialog is showing, and the exercise hasn't been removed yet.
        composeTestRule.onNodeWithText("Remove exercise?").assertExists()
        composeTestRule.onNodeWithText(exerciseName).assertExists()

        composeTestRule.onNodeWithText("Remove").performClick()

        composeTestRule.onNodeWithText(exerciseName).assertDoesNotExist()
    }

    /** D8: a workout day with zero exercises shows "Rest Day" with "Add exercise" still available. */
    @Test
    fun emptyWorkoutDayShowsRestDayWithAddExerciseButton() {
        val viewModel = newViewModel(Weekday.MONDAY)
        val rows = (viewModel.uiState.content as DayContent.Workout).rows
        rows.forEach { viewModel.deleteExercise(it.exercise.id) }

        composeTestRule.setContent { EditableTodayScreen(viewModel) }

        composeTestRule.onNodeWithText("Rest Day").assertExists()
        composeTestRule.onNodeWithText("+ Add exercise").assertExists()

        composeTestRule.onNodeWithText("+ Add exercise").performClick()

        composeTestRule.onNodeWithText("Rest Day").assertDoesNotExist()
        composeTestRule.onNodeWithText("New exercise").assertExists()
    }

    /**
     * D9: day-nav arrows are always >=48x48dp. Checkbox/rename/delete are >=32x32dp at the
     * default (100%) font scale, where they sit in [CompactExerciseRow]'s single dense row
     * alongside the weight/reps fields - full 48dp there wouldn't fit weight+reps next to a
     * readable name on a real phone screen. See [coreControlsReturnTo48dpAtLargerFontScale]
     * for the full 48dp floor once the user increases their text size (Decision 6).
     */
    @Test
    fun coreControlsHaveAtLeast48dpTapTargetsAtDefaultScale() {
        val viewModel = newViewModel(Weekday.MONDAY)
        val rows = (viewModel.uiState.content as DayContent.Workout).rows
        val firstName = rows.first().exercise.name

        composeTestRule.setContent { EditableTodayScreen(viewModel) }

        composeTestRule.onNodeWithContentDescription("Previous day")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithContentDescription("Next day")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)

        val compactDescriptions = listOf(
            "Mark $firstName done",
            "Rename $firstName",
            "Delete $firstName",
        )
        compactDescriptions.forEach { description ->
            composeTestRule.onNodeWithContentDescription(description)
                .assertWidthIsAtLeast(32.dp)
                .assertHeightIsAtLeast(32.dp)
        }
    }

    /** D9: past 100% font scale, checkbox/rename/delete relax to [ExpandedExerciseRow] and grow back to >=48x48dp (Decision 6). */
    @Test
    fun coreControlsReturnTo48dpAtLargerFontScale() {
        val viewModel = newViewModel(Weekday.MONDAY)
        val rows = (viewModel.uiState.content as DayContent.Workout).rows
        val firstName = rows.first().exercise.name

        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.3f)) {
                EditableTodayScreen(viewModel)
            }
        }

        val descriptions = listOf(
            "Mark $firstName done",
            "Rename $firstName",
            "Delete $firstName",
        )
        descriptions.forEach { description ->
            composeTestRule.onNodeWithContentDescription(description)
                .assertWidthIsAtLeast(48.dp)
                .assertHeightIsAtLeast(48.dp)
        }
    }

    /** D10: icon-only controls' contentDescription includes current state, updating as that state changes. */
    @Test
    fun iconControlsExposeContentDescriptionReflectingCurrentState() {
        val viewModel = newViewModel(Weekday.MONDAY)
        val rows = (viewModel.uiState.content as DayContent.Workout).rows
        rows.drop(1).forEach { viewModel.deleteExercise(it.exercise.id) }
        val exerciseName = rows.first().exercise.name

        composeTestRule.setContent { EditableTodayScreen(viewModel) }

        composeTestRule.onNodeWithContentDescription("Mark $exerciseName done").performClick()
        composeTestRule.onNodeWithContentDescription("$exerciseName, marked done").assertExists()
    }

    /** Bug fix: a long exercise list scrolls, so "Add exercise" is always reachable, not clipped. */
    @Test
    fun longExerciseListScrollsToRevealAddExerciseButton() {
        val viewModel = newViewModel(Weekday.MONDAY)
        repeat(15) { viewModel.addExercise() }

        composeTestRule.setContent { EditableTodayScreen(viewModel) }

        composeTestRule.onNodeWithText("+ Add exercise").performScrollTo().assertIsDisplayed()
    }

    /** E3/E4: the all-done banner appears only once every exercise for today is checked. */
    @Test
    fun allDoneBannerAppearsOnlyWhenEveryExerciseIsChecked() {
        val viewModel = newViewModel(Weekday.MONDAY)
        val rows = (viewModel.uiState.content as DayContent.Workout).rows

        composeTestRule.setContent { EditableTodayScreen(viewModel) }

        composeTestRule.onNodeWithText("Nice work - you finished today's workout! 🎉", substring = true)
            .assertDoesNotExist()

        rows.dropLast(1).forEach { viewModel.toggleExerciseChecked(it.exercise.id) }
        composeTestRule.onNodeWithText("Nice work", substring = true).assertDoesNotExist()

        viewModel.toggleExerciseChecked(rows.last().exercise.id)
        composeTestRule.onNodeWithText("Nice work", substring = true).assertExists()
    }

    /** Tapping an exercise's name reports that exercise via onExerciseClick (detail-view entry point). */
    @Test
    fun tappingExerciseNameInvokesOnExerciseClickWithThatExercise() {
        val viewModel = newViewModel(Weekday.MONDAY)
        val firstExercise = (viewModel.uiState.content as DayContent.Workout).rows.first().exercise
        var clicked: Exercise? = null

        composeTestRule.setContent { EditableTodayScreen(viewModel, onExerciseClick = { clicked = it }) }

        composeTestRule.onNodeWithText(firstExercise.name).performClick()

        assertEquals(firstExercise, clicked)
    }
}
