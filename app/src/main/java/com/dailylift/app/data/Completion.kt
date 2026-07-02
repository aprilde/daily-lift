package com.dailylift.app.data

import kotlinx.serialization.Serializable

/**
 * Today's completion state. [done] is keyed by [Exercise.id], not list position,
 * and resets whenever [date] no longer matches the current device-local date.
 */
@Serializable
data class Completion(
    val date: String,
    val done: Map<String, Boolean> = emptyMap(),
)
