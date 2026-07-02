package com.dailylift.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** B6: save data, then reload from a fresh store over the same directory ("app restart"). */
class WorkoutDataStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun saveThenReloadMatchesSavedData() {
        val data = createSeedWorkoutData()
        WorkoutDataStore(tempFolder.root).save(data)

        val reloaded = WorkoutDataStore(tempFolder.root).load()

        assertEquals(data, reloaded)
    }

    @Test
    fun loadReturnsNullBeforeAnythingIsSaved() {
        assertNull(WorkoutDataStore(tempFolder.root).load())
    }

    @Test
    fun loadOrSeedPersistsSeedDataForNextLoad() {
        val seeded = WorkoutDataStore(tempFolder.root).loadOrSeed()

        val reloaded = WorkoutDataStore(tempFolder.root).load()

        assertEquals(seeded, reloaded)
    }
}
