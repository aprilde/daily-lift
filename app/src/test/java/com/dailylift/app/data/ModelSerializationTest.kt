package com.dailylift.app.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/** B1: JSON round-trip for Exercise, WorkoutDay, WorkoutData, and Completion. */
class ModelSerializationTest {

    private val json = Json

    @Test
    fun exerciseRoundTrip() {
        val exercise = sampleExercise()

        val decoded = json.decodeFromString<Exercise>(json.encodeToString(exercise))

        assertEquals(exercise, decoded)
    }

    @Test
    fun workoutDayRoundTrip() {
        val day = WorkoutDay(focus = "Lower body", exercises = listOf(sampleExercise()))

        val decoded = json.decodeFromString<WorkoutDay>(json.encodeToString(day))

        assertEquals(day, decoded)
    }

    @Test
    fun workoutDataRoundTrip() {
        val data: WorkoutData = createSeedWorkoutData()

        val decoded = json.decodeFromString<WorkoutData>(json.encodeToString(data))

        assertEquals(data, decoded)
    }

    @Test
    fun completionRoundTrip() {
        val completion = Completion(date = "2026-06-15", done = mapOf("ex-1" to true, "ex-2" to false))

        val decoded = json.decodeFromString<Completion>(json.encodeToString(completion))

        assertEquals(completion, decoded)
    }

    private fun sampleExercise() = Exercise(
        id = "abc-123",
        name = "Goblet squat",
        weight = "bodyweight",
        reps = "8-12 reps",
        tip = "Hold the weight at your chest, sit back like into a chair.",
        imageStartRef = "goblet_squat_start",
        imageEndRef = "goblet_squat_end",
    )
}
