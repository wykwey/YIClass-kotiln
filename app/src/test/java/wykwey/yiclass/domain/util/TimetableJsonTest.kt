package wykwey.yiclass.domain.util

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.CourseSchedule
import wykwey.yiclass.domain.model.Holiday
import wykwey.yiclass.domain.model.Timetable

class TimetableJsonTest {
    @Test
    fun backupRestoresCourseColorClassTimesAndCalendar() {
        val initial = TimetableDefaults.defaultSettings(startDate = LocalDate.of(2026, 9, 2), maxPeriods = 3)
        val timetable = Timetable(
            name = "秋季学期",
            settings = initial.copy(
                classTimes = initial.classTimes.map { period ->
                    if (period.period == 1) period.copy(startTime = LocalTime.of(7, 45)) else period
                },
                holidays = listOf(
                    Holiday(date = LocalDate.of(2026, 9, 4)),
                    Holiday(date = LocalDate.of(2026, 9, 5), isMakeup = true, makeupWeekday = 5),
                    Holiday(date = LocalDate.of(2026, 9, 6), isMakeup = true)
                )
            ),
            courses = listOf(Course(
                timetableId = 0,
                name = "数据结构",
                color = 0xFFBBDEFB.toInt(),
                schedules = listOf(CourseSchedule(weekday = 5, periods = listOf(1, 2), weekPattern = listOf(1, 2)))
            ))
        )

        assertEquals(timetable, TimetableJson.fromJson(TimetableJson.toJson(timetable)))
    }

    @Test
    fun providerImportUsesItsOwnEntryPoint() {
        val payload = """{"name":"学校课表","version":"2.0","courses":[{"name":"体育","schedules":[{"weekday":2,"periods":[3],"weekPattern":[1]}]}]}"""
        assertEquals("体育", TimetableJson.fromProviderJson(payload).courses.single().name)
        assertThrows(IllegalArgumentException::class.java) { TimetableJson.fromJson(payload) }
    }

    @Test
    fun backupRejectsUnknownVersion() {
        val backup = """{"format":"yiclass-timetable","version":2,"timetable":{"courses":[]}}"""
        assertThrows(IllegalArgumentException::class.java) { TimetableJson.fromJson(backup) }
    }
}
