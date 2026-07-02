package com.dailylift.app.today

import com.dailylift.app.data.Weekday
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

private val UTC = ZoneOffset.UTC

/** 2024-01-01 is a known Monday; the rest of that week follows in calendar order. */
private fun clockFor(isoDate: String): Clock = Clock.fixed(Instant.parse("${isoDate}T12:00:00Z"), UTC)

class TodayCalendarTest {

    /** C1/C2: a known Monday-Sunday week maps to the matching [Weekday] for each date. */
    @Test
    fun currentWeekdayMatchesKnownCalendarWeek() {
        val expected = listOf(
            "2024-01-01" to Weekday.MONDAY,
            "2024-01-02" to Weekday.TUESDAY,
            "2024-01-03" to Weekday.WEDNESDAY,
            "2024-01-04" to Weekday.THURSDAY,
            "2024-01-05" to Weekday.FRIDAY,
            "2024-01-06" to Weekday.SATURDAY,
            "2024-01-07" to Weekday.SUNDAY,
        )

        expected.forEach { (date, weekday) ->
            assertEquals(weekday, currentWeekday(clockFor(date)))
        }
    }

    @Test
    fun currentDateStringMatchesIsoDate() {
        assertEquals("2024-01-01", currentDateString(clockFor("2024-01-01")))
    }

    /** C2: Monday-Friday are workout days, Saturday/Sunday are rest days. */
    @Test
    fun isWorkoutDayIsTrueOnlyMondayThroughFriday() {
        val workoutDays = setOf(
            Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY, Weekday.THURSDAY, Weekday.FRIDAY,
        )

        Weekday.entries.forEach { weekday ->
            assertEquals(weekday in workoutDays, weekday.isWorkoutDay())
        }
    }

    @Test
    fun nextWrapsAroundAllSevenDays() {
        assertEquals(Weekday.TUESDAY, Weekday.MONDAY.next(1))
        assertEquals(Weekday.SUNDAY, Weekday.MONDAY.next(-1))
        assertEquals(Weekday.MONDAY, Weekday.SUNDAY.next(1))
    }

    @Test
    fun displayNameIsTitleCase() {
        assertEquals("Monday", Weekday.MONDAY.displayName())
        assertEquals("Sunday", Weekday.SUNDAY.displayName())
    }

    /** C3: resuming after the date changed while backgrounded recomputes "today". */
    @Test
    fun refreshTodayRecomputesAfterClockAdvancesToNextDay() {
        val clock = MutableClock(Instant.parse("2024-01-01T23:00:00Z"), UTC)
        val tracker = TodayClockTracker(clock)
        assertEquals(Weekday.MONDAY, tracker.today)

        clock.instant = Instant.parse("2024-01-02T01:00:00Z")
        tracker.refreshToday()

        assertEquals(Weekday.TUESDAY, tracker.today)
    }

    /** A [Clock] whose [instant] can be advanced, simulating time passing while the app is backgrounded. */
    private class MutableClock(var instant: Instant, private val zone: ZoneId) : Clock() {
        override fun getZone(): ZoneId = zone
        override fun withZone(zone: ZoneId): Clock = MutableClock(instant, zone)
        override fun instant(): Instant = instant
    }
}
