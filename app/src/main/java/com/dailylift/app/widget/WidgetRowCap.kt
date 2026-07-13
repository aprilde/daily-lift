package com.dailylift.app.widget

import com.dailylift.app.today.ExerciseRow

/** [shown] rows to render, plus how many more exist beyond that ("+N more", Decision 7). */
data class CappedRows(val shown: List<ExerciseRow>, val overflowCount: Int)

/** Caps [rows] at [max] entries; anything beyond that is counted in [CappedRows.overflowCount] instead of rendered. */
fun capRows(rows: List<ExerciseRow>, max: Int): CappedRows =
    if (rows.size <= max) {
        CappedRows(shown = rows, overflowCount = 0)
    } else {
        CappedRows(shown = rows.take(max), overflowCount = rows.size - max)
    }
