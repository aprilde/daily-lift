package com.dailylift.app.data

import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * A single exercise within a [WorkoutDay].
 *
 * [weight] is `""` (no weight set), `"bodyweight"`, or a numeric string (e.g. `"15"`).
 * [imageStartRef] / [imageEndRef] are drawable resource names, not resolved here.
 */
@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val weight: String,
    val reps: String,
    val tip: String,
    val imageStartRef: String,
    val imageEndRef: String,
) {
    companion object {
        /** Rename input cap (Decision 5, edge case #3). */
        const val MAX_NAME_LENGTH = 30

        /** Generic fallback image pair for exercises without dedicated art (Decision 5, edge case #9). */
        const val PLACEHOLDER_IMAGE_START_REF = "placeholder_start"
        const val PLACEHOLDER_IMAGE_END_REF = "placeholder_end"

        /** A freshly added exercise: unique [id], default tip, and the generic placeholder image pair. */
        fun new(name: String = "New exercise"): Exercise = Exercise(
            id = UUID.randomUUID().toString(),
            name = name.take(MAX_NAME_LENGTH),
            weight = "",
            reps = "10",
            tip = "Add your own note here by editing this exercise.",
            imageStartRef = PLACEHOLDER_IMAGE_START_REF,
            imageEndRef = PLACEHOLDER_IMAGE_END_REF,
        )
    }
}
