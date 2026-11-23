package wykwey.yiclass.domain.util

import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.Timetable

object TimetableQuery {

    fun visibleDays(timetable: Timetable, week: Int): List<Int> {
        if (timetable.settings.showWeekend) return (1..7).toList()
        return (1..5).toList() + (6..7).filter { day ->
            val date = WeekCalculator.dateInWeek(timetable.settings.startDate, week, day)
            timetable.settings.holidays.any { it.date == date && it.isMakeup }
        }
    }

    fun weekCourses(timetable: Timetable, week: Int): List<Course> {
        val weekdays = (1..7).mapNotNull { day ->
            scheduledWeekday(timetable, week, day)?.let { day to it }
        }.groupBy(keySelector = { it.second }, valueTransform = { it.first })
        return timetable.courses.mapNotNull { course ->
            val schedules = course.schedules.filter { week in it.weekPattern }.flatMap { schedule ->
                weekdays[schedule.weekday].orEmpty().map { schedule.copy(weekday = it) }
            }.sortedBy { it.weekday }
            if (schedules.isEmpty()) null else course.copy(schedules = schedules)
        }
    }

    fun dayCourses(timetable: Timetable, week: Int, weekday: Int): List<Course> {
        val scheduledDay = scheduledWeekday(timetable, week, weekday) ?: return emptyList()
        return timetable.courses.mapNotNull { course ->
            val schedules = course.schedules.filter { it.weekday == scheduledDay && week in it.weekPattern }
                .map { it.copy(weekday = weekday) }
            if (schedules.isEmpty()) null else course.copy(schedules = schedules)
        }.sortedBy { course -> course.schedules.flatMap { it.periods }.minOrNull() ?: Int.MAX_VALUE }
    }

    private fun scheduledWeekday(timetable: Timetable, week: Int, weekday: Int): Int? {
        val settings = timetable.settings
        if (week !in 1..settings.totalWeeks || weekday !in 1..7) return null
        val date = WeekCalculator.dateInWeek(settings.startDate, week, weekday)
        if (date.isBefore(settings.startDate)) return null
        val holiday = settings.holidays.firstOrNull { it.date == date } ?: return weekday
        return if (holiday.isMakeup) holiday.makeupWeekday ?: weekday else null
    }
}
