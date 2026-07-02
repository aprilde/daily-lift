package com.dailylift.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedWorkoutDataTest {

    private val data = createSeedWorkoutData()
    private val allExercises = data.values.flatMap { it.exercises }

    /** B2: seed data contains no literal "AMRAP" string anywhere in `reps`. */
    @Test
    fun noExerciseUsesAmrap() {
        allExercises.forEach { exercise ->
            assertFalse(
                "Expected no literal AMRAP in reps for '${exercise.name}', found '${exercise.reps}'",
                exercise.reps.contains("AMRAP", ignoreCase = true),
            )
        }
    }

    /** B3: every seeded exercise has a unique, non-empty id. */
    @Test
    fun everyExerciseHasUniqueNonEmptyId() {
        val ids = allExercises.map { it.id }

        ids.forEach { id -> assertTrue("Expected non-empty id", id.isNotEmpty()) }
        assertEquals("Expected all ids to be unique", ids.size, ids.toSet().size)
    }

    /** B4: seed data covers Monday-Friday only, with no Saturday/Sunday entries. */
    @Test
    fun coversMondayThroughFridayOnly() {
        val expectedDays = setOf(
            Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY, Weekday.THURSDAY, Weekday.FRIDAY,
        )

        assertEquals(expectedDays, data.keys)
    }

    /** B5: every seeded exercise has non-empty imageStartRef and imageEndRef values. */
    @Test
    fun everyExerciseHasImageRefs() {
        allExercises.forEach { exercise ->
            assertTrue("Expected non-empty imageStartRef for '${exercise.name}'", exercise.imageStartRef.isNotEmpty())
            assertTrue("Expected non-empty imageEndRef for '${exercise.name}'", exercise.imageEndRef.isNotEmpty())
        }
    }

    /** E1: a seeded exercise resolves to its own slug-based image refs and tip, not the generic placeholder. */
    @Test
    fun seededExerciseResolvesToItsOwnImageRefsAndTip() {
        val gobletSquat = allExercises.first { it.name == "Goblet squat" }

        assertEquals("goblet_squat_start", gobletSquat.imageStartRef)
        assertEquals("goblet_squat_end", gobletSquat.imageEndRef)
        assertEquals(
            "Hold the weight at your chest, sit back like into a chair, knees tracking over toes.",
            gobletSquat.tip,
        )
        assertFalse(gobletSquat.imageStartRef == Exercise.PLACEHOLDER_IMAGE_START_REF)
        assertFalse(gobletSquat.imageEndRef == Exercise.PLACEHOLDER_IMAGE_END_REF)
    }

    /** E1: exercises that repeat across days (same name) resolve to the same image refs, not duplicates. */
    @Test
    fun repeatedExerciseNameAcrossDaysResolvesToTheSameImageRefs() {
        val dumbbellRdls = allExercises.filter { it.name == "Dumbbell RDL" }

        assertTrue("Expected 'Dumbbell RDL' to appear on more than one day", dumbbellRdls.size > 1)
        assertEquals(1, dumbbellRdls.map { it.imageStartRef }.toSet().size)
        assertEquals(1, dumbbellRdls.map { it.imageEndRef }.toSet().size)
    }
}
