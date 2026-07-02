package com.dailylift.app.data

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Reads and writes today's [Completion] state as a single JSON string in [prefs].
 */
class CompletionStore(private val prefs: SharedPreferences) {

    private val json = Json

    /** Returns the persisted [Completion], or `null` if nothing has been saved yet. */
    fun load(): Completion? =
        prefs.getString(KEY_COMPLETION, null)?.let { json.decodeFromString<Completion>(it) }

    /** Writes [completion] to [prefs], overwriting any previous value. */
    fun save(completion: Completion) {
        prefs.edit { putString(KEY_COMPLETION, json.encodeToString(completion)) }
    }

    companion object {
        private const val KEY_COMPLETION = "completion"
    }
}
