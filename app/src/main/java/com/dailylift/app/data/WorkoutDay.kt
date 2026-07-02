package com.dailylift.app.data

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutDay(
    val focus: String,
    val exercises: List<Exercise>,
)

/**
 * Monday-Friday only. Saturday/Sunday are rest days and have no entry.
 */
typealias WorkoutData = Map<Weekday, WorkoutDay>
