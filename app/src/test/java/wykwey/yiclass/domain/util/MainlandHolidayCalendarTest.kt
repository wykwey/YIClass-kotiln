package wykwey.yiclass.domain.util

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainlandHolidayCalendarTest {
    @Test
    fun clipsOfficialDatesToTheSemesterAcrossCalendarYears() {
        val entries = MainlandHolidayCalendar.adjustmentsBetween(LocalDate.of(2026, 9, 26), LocalDate.of(2027, 1, 2))
        assertEquals(11, entries.count { !it.isMakeup })
        assertEquals(LocalDate.of(2026, 10, 10), entries.single { it.isMakeup }.date)
        assertEquals(LocalDate.of(2026, 9, 26), entries.first().date)
        assertEquals(LocalDate.of(2027, 1, 2), entries.last().date)
    }

    @Test
    fun importsOfficialAdjustedWorkdaysWithoutGuessingSchoolTimetables() {
        val entries = MainlandHolidayCalendar.adjustmentsBetween(LocalDate.of(2027, 1, 31), LocalDate.of(2027, 2, 14))
        assertEquals((5..13).map { LocalDate.of(2027, 2, it) }, entries.filterNot { it.isMakeup }.map { it.date })
        val adjustedWorkdays = entries.filter { it.isMakeup }
        assertEquals(listOf(LocalDate.of(2027, 1, 31), LocalDate.of(2027, 2, 14)), adjustedWorkdays.map { it.date })
        assertTrue(adjustedWorkdays.all { it.makeupWeekday == null })
    }

    @Test
    fun hasNoUnpublishedFutureDates() {
        assertEquals(setOf(2025, 2026, 2027), MainlandHolidayCalendar.coveredYears)
        assertTrue(MainlandHolidayCalendar.adjustmentsBetween(LocalDate.of(2028, 1, 1), LocalDate.of(2028, 12, 31)).isEmpty())
    }
}
