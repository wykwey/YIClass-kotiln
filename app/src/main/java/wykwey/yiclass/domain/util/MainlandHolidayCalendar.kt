package wykwey.yiclass.domain.util

import java.time.LocalDate
import wykwey.yiclass.domain.model.Holiday

object MainlandHolidayCalendar {
    // 国务院办公厅年度放假通知：国办发明电〔2024〕12号、〔2025〕7号、〔2026〕10号。
    private val daysOff = listOf(
        "2025-01-01" to "2025-01-01",
        "2025-01-28" to "2025-02-04",
        "2025-04-04" to "2025-04-06",
        "2025-05-01" to "2025-05-05",
        "2025-05-31" to "2025-06-02",
        "2025-10-01" to "2025-10-08",
        "2026-01-01" to "2026-01-03",
        "2026-02-15" to "2026-02-23",
        "2026-04-04" to "2026-04-06",
        "2026-05-01" to "2026-05-05",
        "2026-06-19" to "2026-06-21",
        "2026-09-25" to "2026-09-27",
        "2026-10-01" to "2026-10-07",
        "2027-01-01" to "2027-01-03",
        "2027-02-05" to "2027-02-13",
        "2027-04-03" to "2027-04-05",
        "2027-05-01" to "2027-05-05",
        "2027-06-09" to "2027-06-09",
        "2027-09-15" to "2027-09-15",
        "2027-10-01" to "2027-10-07"
    ).flatMap { (first, last) ->
        val end = LocalDate.parse(last)
        generateSequence(LocalDate.parse(first)) { it.plusDays(1) }.takeWhile { it <= end }.toList()
    }

    private val adjustedWorkdays = listOf(
        "2025-01-26", "2025-02-08", "2025-04-27", "2025-09-28", "2025-10-11",
        "2026-01-04", "2026-02-14", "2026-02-28", "2026-05-09", "2026-09-20", "2026-10-10",
        "2027-01-31", "2027-02-14", "2027-05-08", "2027-09-26", "2027-10-09"
    ).map(LocalDate::parse)

    private val adjustments = (
        daysOff.map { Holiday(date = it) } + adjustedWorkdays.map { Holiday(date = it, isMakeup = true) }
    ).sortedBy { it.date }

    val coveredYears = adjustments.map { it.date.year }.toSet()

    fun adjustmentsBetween(start: LocalDate, end: LocalDate): List<Holiday> =
        adjustments.filter { it.date in start..end }
}
