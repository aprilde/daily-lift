package com.dailylift.app.detail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dailylift.app.data.Exercise
import org.junit.Rule
import org.junit.Test

class ExerciseDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val exercise = Exercise(
        id = "ex-1",
        name = "Goblet squat",
        weight = "15",
        reps = "8-12 reps",
        tip = "Hold the weight at your chest, sit back like into a chair.",
        imageStartRef = "goblet_squat_start",
        imageEndRef = "goblet_squat_end",
    )

    /** E5: detail view renders name, weight (no reps - PM direction, Addendum 11), both images labeled "Start"/"End", and the tip. */
    @Test
    fun detailViewRendersNameWeightImagesAndTipButNotReps() {
        composeTestRule.setContent { ExerciseDetailScreen(exercise = exercise, onBack = {}) }

        composeTestRule.onNodeWithText("Goblet squat").assertExists()
        composeTestRule.onNodeWithText("15 lb").assertExists()
        composeTestRule.onNodeWithText(exercise.reps, substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Start").assertExists()
        composeTestRule.onNodeWithText("End").assertExists()
        composeTestRule.onNodeWithContentDescription("Start position demo image of Goblet squat").assertExists()
        composeTestRule.onNodeWithContentDescription("End position demo image of Goblet squat").assertExists()
        composeTestRule.onNodeWithText(exercise.tip).assertExists()
    }

    /** Tapping back invokes onBack. */
    @Test
    fun tappingBackInvokesOnBack() {
        var backPressed = false

        composeTestRule.setContent { ExerciseDetailScreen(exercise = exercise, onBack = { backPressed = true }) }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(backPressed)
    }
}
