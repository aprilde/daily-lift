package com.dailylift.app.data

import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Reads and writes [WorkoutData] as a single JSON file inside [storageDir].
 *
 * Takes a plain [File] directory (e.g. `Context.filesDir`) rather than a `Context` so this
 * class can be unit tested without an Android runtime, and so a future widget process can
 * reuse it directly.
 */
class WorkoutDataStore(private val storageDir: File) {

    private val file = File(storageDir, FILE_NAME)
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /** Returns the persisted [WorkoutData], or `null` if no file has been saved yet. */
    fun load(): WorkoutData? =
        if (file.exists()) json.decodeFromString<WorkoutData>(file.readText()) else null

    /** Writes [data] to the JSON file, overwriting any previous contents. */
    fun save(data: WorkoutData) {
        file.writeText(json.encodeToString(data))
    }

    /** Loads existing data, or seeds, persists, and returns the default workout data. */
    fun loadOrSeed(): WorkoutData = load() ?: createSeedWorkoutData().also(::save)

    companion object {
        private const val FILE_NAME = "workout_data.json"
    }
}
