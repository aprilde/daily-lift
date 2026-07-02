package com.dailylift.app.today

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dailylift.app.data.Completion
import com.dailylift.app.data.Weekday
import com.dailylift.app.data.createSeedWorkoutData
import org.junit.Rule
import org.junit.Test

class TodayScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val workoutData = createSeedWorkoutData()
    private val emptyCompletion = Completion(date = "2026-06-15")

    private val expectedFocus = mapOf(
        Weekday.MONDAY to "Lower body",
        Weekday.TUESDAY to "Upper body push",
        Weekday.WEDNESDAY to "Lower body",
        Weekday.THURSDAY to "Upper body pull",
        Weekday.FRIDAY to "Full body",
    )

    private fun setContentForDay(viewedWeekday: Weekday, today: Weekday = viewedWeekday) {
        composeTestRule.setContent {
            var viewed by remember { mutableStateOf(viewedWeekday) }
            val state = buildTodayUiState(workoutData, emptyCompletion, today = today, viewedWeekday = viewed)
            TodayScreen(uiState = state, onNavigate = { direction -> viewed = viewed.next(direction) })
        }
    }

    /** C4: left/right arrows cycle through all 7 days, each showing its correct focus label (or rest screen). */
    @Test
    fun dayNavigationCyclesThroughAllSevenDaysWithCorrectFocusLabels() {
        setContentForDay(Weekday.MONDAY)

        var current = Weekday.MONDAY
        repeat(7) {
            composeTestRule.onNodeWithText(current.displayName()).assertExists()
            if (current.isWorkoutDay()) {
                composeTestRule.onNodeWithText(expectedFocus.getValue(current)).assertExists()
            } else {
                composeTestRule.onNodeWithText("Rest & recover").assertExists()
            }
            composeTestRule.onNodeWithContentDescription("Next day").performClick()
            current = current.next(1)
        }

        // Seven clicks from Monday cycles all the way back to Monday.
        composeTestRule.onNodeWithText("Monday").assertExists()
    }

    /** C5: the static "Do 3 sets of each exercise below" line renders exactly once per workout-day view. */
    @Test
    fun do3SetsLineRendersOncePerWorkoutDay() {
        setContentForDay(Weekday.MONDAY)

        composeTestRule.onAllNodesWithText("Do 3 sets of each exercise below").assertCountEquals(1)
    }

    /** C6: Saturday/Sunday show "Rest & recover" plus a "Next up" preview of Monday's first exercises. */
    @Test
    fun restDayShowsRestMessageAndNextUpPreview() {
        setContentForDay(Weekday.SATURDAY, today = Weekday.MONDAY)

        composeTestRule.onNodeWithText("Rest & recover").assertExists()

        val monday = workoutData.getValue(Weekday.MONDAY)
        val previewText = monday.exercises.take(3).joinToString(", ") { it.name } + " + more"
        composeTestRule.onNodeWithText(previewText, substring = true).assertExists()
    }
}
