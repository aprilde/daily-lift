package com.dailylift.app.today

import com.dailylift.app.data.Weekday
import java.time.Clock
import java.time.LocalDate

/** [Weekday] for the current date in [clock]'s zone. [java.time.DayOfWeek] and [Weekday] share Monday-first ordinals. */
fun currentWeekday(clock: Clock): Weekday = Weekday.entries[LocalDate.now(clock).dayOfWeek.ordinal]

/** ISO-8601 date string (e.g. "2026-06-15") for the current date in [clock]'s zone. */
fun currentDateString(clock: Clock): String = LocalDate.now(clock).toString()

/** `true` for Monday-Friday, `false` for Saturday/Sunday (rest days). */
fun Weekday.isWorkoutDay(): Boolean = this in Weekday.MONDAY..Weekday.FRIDAY

/** Title-case day name for display (e.g. [Weekday.MONDAY] -> "Monday"). */
fun Weekday.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

/** The weekday [direction] steps away, wrapping around all 7 days (e.g. `next(-1)` from Monday is Sunday). */
fun Weekday.next(direction: Int): Weekday {
    val days = Weekday.entries
    val index = (days.indexOf(this) + direction).mod(days.size)
    return days[index]
}

/**
 * Tracks "today" as computed from [clock], recomputed on demand via [refreshToday].
 *
 * Used to satisfy "recompute today on every resume, not just at launch" — the caller invokes
 * [refreshToday] from an `ON_RESUME` lifecycle callback.
 */
class TodayClockTracker(private val clock: Clock = Clock.systemDefaultZone()) {

    var today: Weekday = currentWeekday(clock)
        private set

    fun refreshToday() {
        today = currentWeekday(clock)
    }
}
