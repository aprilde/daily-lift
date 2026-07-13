package com.dailylift.app.widget

import com.dailylift.app.data.Exercise
import com.dailylift.app.today.ExerciseRow
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetRowCapTest {

    private fun rows(count: Int): List<ExerciseRow> =
        (1..count).map { ExerciseRow(exercise = Exercise.new("Exercise $it"), checked = false) }

    @Test
    fun fewerRowsThanMaxShowsAllWithNoOverflow() {
        val result = capRows(rows(2), max = 4)

        assertEquals(2, result.shown.size)
        assertEquals(0, result.overflowCount)
    }

    @Test
    fun exactlyAtMaxShowsAllWithNoOverflow() {
        val result = capRows(rows(4), max = 4)

        assertEquals(4, result.shown.size)
        assertEquals(0, result.overflowCount)
    }

    @Test
    fun moreRowsThanMaxTruncatesAndCountsTheRest() {
        val all = rows(7)

        val result = capRows(all, max = 4)

        assertEquals(4, result.shown.size)
        assertEquals(all.take(4), result.shown)
        assertEquals(3, result.overflowCount)
    }

    @Test
    fun emptyListWithNonZeroMaxShowsNothingNoOverflow() {
        val result = capRows(emptyList(), max = 4)

        assertEquals(0, result.shown.size)
        assertEquals(0, result.overflowCount)
    }

    @Test
    fun maxOfZeroWithNonEmptyRowsShowsNothingAndCountsAll() {
        val result = capRows(rows(3), max = 0)

        assertEquals(0, result.shown.size)
        assertEquals(3, result.overflowCount)
    }
}
