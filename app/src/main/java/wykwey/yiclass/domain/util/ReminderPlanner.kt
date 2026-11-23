package wykwey.yiclass.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import wykwey.yiclass.domain.model.Timetable

data class CourseReminder(
    val tag: String,
    val courseName: String,
    val location: String,
    val startsAt: Instant,
    val remindsAt: Instant
)

object ReminderPlanner {
    fun forDate(timetable: Timetable, date: LocalDate, zone: ZoneId): List<CourseReminder> {
        val settings = timetable.settings
        val week = WeekCalculator.currentWeekOrNull(settings.startDate, settings.totalWeeks, date)
            ?: return emptyList()
        val times = settings.classTimes.associateBy { it.period }
        val leadSeconds = settings.reminderMinutes.coerceIn(0, 1_440) * 60L
        return TimetableQuery.dayCourses(timetable, week, date.dayOfWeek.value).flatMap { course ->
            val periods = course.schedules.flatMap { it.periods }
                .filter { it in 1..settings.maxPeriods }.toSortedSet()
            periods.filter { it - 1 !in periods }.mapNotNull { period ->
                val time = times[period] ?: return@mapNotNull null
                val starts = date.atTime(time.startTime).atZone(zone).toInstant()
                CourseReminder(
                    tag = "${timetable.id}/${course.id}/$date/$period",
                    courseName = course.name,
                    location = course.location,
                    startsAt = starts,
                    remindsAt = starts.minusSeconds(leadSeconds)
                )
            }
        }.sortedBy { it.remindsAt }
    }
}
