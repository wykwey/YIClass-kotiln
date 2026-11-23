package wykwey.yiclass.domain.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 学期周次计算。第 1 周为包含 startDate 的那一周（周一为一周起点）。
 */
object WeekCalculator {

    /**
     * 返回 [today] 相对 [startDate] 所在的教学周（从 1 开始）。
     * 早于开学日返回 1，晚于总周数则夹取到 [totalWeeks]。
     */
    fun currentWeek(
        startDate: LocalDate,
        totalWeeks: Int,
        today: LocalDate = LocalDate.now()
    ): Int {
        return weekIndex(startDate, today).coerceIn(1, totalWeeks.coerceAtLeast(1))
    }

    /**
     * 返回 [today] 相对 [startDate] 所在的教学周（从 1 开始）；
     * 超出 [1, totalWeeks] 范围（学期未开始/已结束）返回 null，不夹取。
     * 用于日视图区分"今天"是否在学期内。
     */
    fun currentWeekOrNull(
        startDate: LocalDate,
        totalWeeks: Int,
        today: LocalDate = LocalDate.now()
    ): Int? {
        if (today.isBefore(startDate)) return null
        return weekIndex(startDate, today).takeIf { it in 1..totalWeeks }
    }

    fun dateInWeek(startDate: LocalDate, week: Int, weekday: Int): LocalDate =
        startDate.with(DayOfWeek.MONDAY).plusWeeks(week - 1L).plusDays(weekday - 1L)

    private fun weekIndex(startDate: LocalDate, date: LocalDate): Int =
        ChronoUnit.WEEKS.between(startDate.with(DayOfWeek.MONDAY), date.with(DayOfWeek.MONDAY)).toInt() + 1
}
