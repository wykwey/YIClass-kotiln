package wykwey.yiclass.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.CourseSchedule
import wykwey.yiclass.domain.model.Holiday
import wykwey.yiclass.domain.model.Timetable

class ReminderPlannerTest {
    private val zone = ZoneId.of("Asia/Shanghai")
    private val firstMonday = LocalDate.of(2026, 9, 7)
    private val timetable = Timetable(
        id = 1,
        name = "课表",
        settings = TimetableDefaults.defaultSettings(firstMonday).copy(totalWeeks = 2),
        courses = listOf(Course(
            id = 10,
            timetableId = 1,
            name = "数学",
            color = 0,
            schedules = listOf(
                CourseSchedule(weekday = 1, periods = listOf(1, 2, 4), weekPattern = listOf(1)),
                CourseSchedule(weekday = 1, periods = listOf(2, 3), weekPattern = listOf(2))
            )
        ))
    )

    @Test
    fun consecutivePeriodsNotifyOncePerCourseBlock() {
        val reminders = ReminderPlanner.forDate(timetable, firstMonday, zone)
        assertEquals(listOf(Instant.parse("2026-09-06T23:45:00Z"), Instant.parse("2026-09-07T02:15:00Z")),
            reminders.map { it.remindsAt })
    }

    @Test
    fun makeupDayUsesThatWeeksScheduleAndHolidayIsSkipped() {
        val makeup = firstMonday.plusDays(5)
        val adjusted = timetable.copy(settings = timetable.settings.copy(holidays = listOf(
            Holiday(date = firstMonday), Holiday(date = makeup, isMakeup = true, makeupWeekday = 1)
        )))
        assertTrue(ReminderPlanner.forDate(adjusted, firstMonday, zone).isEmpty())
        assertEquals(2, ReminderPlanner.forDate(adjusted, makeup, zone).size)
        assertEquals(listOf(6), TimetableQuery.weekCourses(adjusted, 1).single().schedules.map { it.weekday })
        assertTrue(ReminderPlanner.forDate(adjusted, firstMonday.plusWeeks(2), zone).isEmpty())
    }

    @Test
    fun unassignedAdjustedDayKeepsItsRegularClasses() {
        val saturday = firstMonday.plusDays(5)
        val classOnSaturday = Course(
            id = 11,
            timetableId = 1,
            name = "周六的课",
            color = 0,
            schedules = listOf(CourseSchedule(weekday = 6, periods = listOf(1), weekPattern = listOf(1)))
        )
        val adjusted = timetable.copy(
            settings = timetable.settings.copy(holidays = listOf(Holiday(date = saturday, isMakeup = true))),
            courses = timetable.courses + classOnSaturday
        )
        assertEquals(listOf("周六的课"), ReminderPlanner.forDate(adjusted, saturday, zone).map { it.courseName })
    }

    @Test
    fun supportsCrossMidnightAndHonorsActualSemesterStart() {
        val settings = timetable.settings.copy(
            classTimes = timetable.settings.classTimes.map {
                if (it.period == 1) it.copy(startTime = LocalTime.of(0, 5)) else it
            }
        )
        val reminder = ReminderPlanner.forDate(timetable.copy(settings = settings), firstMonday, zone).first()
        assertEquals(Instant.parse("2026-09-06T15:50:00Z"), reminder.remindsAt)
        val beginsTuesday = timetable.copy(settings = settings.copy(startDate = firstMonday.plusDays(1)))
        assertTrue(ReminderPlanner.forDate(beginsTuesday, firstMonday, zone).isEmpty())
    }
}
