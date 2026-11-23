package wykwey.yiclass.domain.util

import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import wykwey.yiclass.domain.model.ClassTime
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.CourseSchedule
import wykwey.yiclass.domain.model.Holiday
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.model.TimetableSettings

object TimetableJson {
    private const val FORMAT = "yiclass-timetable"
    private const val VERSION = 1
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun toJson(timetable: Timetable): String = json.encodeToString(Backup(FORMAT, VERSION, timetable.toPayload()))

    fun fromJson(text: String): Timetable {
        val backup = json.decodeFromString<Backup>(text)
        require(backup.format == FORMAT && backup.version == VERSION) { "无法识别的 YIClass 备份格式" }
        return backup.timetable.toTimetable()
    }

    fun fromProviderJson(text: String): Timetable = json.decodeFromString<Payload>(text).toTimetable()

    @Serializable
    private data class Backup(val format: String, val version: Int, val timetable: Payload)

    @Serializable
    private data class Payload(
        val name: String = "",
        val settings: Settings = Settings(),
        val courses: List<CourseData>
    )

    @Serializable
    private data class Settings(
        val startDate: String? = null,
        val totalWeeks: Int = 20,
        val showWeekend: Boolean = false,
        val maxPeriods: Int = 16,
        val reminderMinutes: Int = 15,
        val classTimes: List<PeriodTime> = emptyList(),
        val calendar: List<CalendarDay> = emptyList()
    )

    @Serializable
    private data class PeriodTime(val period: Int, val startTime: String, val endTime: String)

    @Serializable
    private data class CalendarDay(
        val date: String,
        val makeupWeekday: Int? = null,
        val isMakeup: Boolean = makeupWeekday != null
    )

    @Serializable
    private data class CourseData(
        val name: String,
        val location: String = "",
        val teacher: String = "",
        val color: Long = 0,
        val schedules: List<Schedule>
    )

    @Serializable
    private data class Schedule(val weekday: Int, val periods: List<Int>, val weekPattern: List<Int>)

    private fun Timetable.toPayload() = Payload(
        name = name,
        settings = Settings(
            startDate = settings.startDate.toString(),
            totalWeeks = settings.totalWeeks,
            showWeekend = settings.showWeekend,
            maxPeriods = settings.maxPeriods,
            reminderMinutes = settings.reminderMinutes,
            classTimes = settings.classTimes.map { PeriodTime(it.period, it.startTime.toString(), it.endTime.toString()) },
            calendar = settings.holidays.map { CalendarDay(it.date.toString(), it.makeupWeekday, it.isMakeup) }
        ),
        courses = courses.map { course ->
            CourseData(
                name = course.name,
                location = course.location,
                teacher = course.teacher,
                color = course.color.toUInt().toLong(),
                schedules = course.schedules.map { Schedule(it.weekday, it.periods, it.weekPattern) }
            )
        }
    )

    private fun Payload.toTimetable(): Timetable {
        require(settings.totalWeeks in 1..20 && settings.maxPeriods in 1..16) { "周数或节次数超出范围" }
        val periodTimes = settings.classTimes.associate { (period, start, end) ->
            period to ClassTime(period = period, startTime = LocalTime.parse(start), endTime = LocalTime.parse(end))
        }
        return Timetable(
            name = name.ifBlank { "导入的课表" },
            settings = TimetableSettings(
                startDate = settings.startDate?.let(LocalDate::parse) ?: LocalDate.now(),
                totalWeeks = settings.totalWeeks,
                showWeekend = settings.showWeekend,
                maxPeriods = settings.maxPeriods,
                reminderMinutes = settings.reminderMinutes,
                classTimes = TimetableDefaults.defaultClassTimes(settings.maxPeriods).map { periodTimes[it.period] ?: it },
                holidays = settings.calendar.map { (date, weekday, isMakeup) ->
                    require(weekday == null || weekday in 1..7) { "补课星期超出范围" }
                    Holiday(date = LocalDate.parse(date), isMakeup = isMakeup, makeupWeekday = weekday)
                }.distinctBy { it.date }.sortedBy { it.date }
            ),
            courses = courses.map { course ->
                Course(
                    timetableId = 0,
                    name = course.name,
                    location = course.location,
                    teacher = course.teacher,
                    color = course.color.toInt(),
                    schedules = course.schedules.map { schedule ->
                        require(schedule.weekday in 1..7) { "课程星期超出范围" }
                        CourseSchedule(
                            weekday = schedule.weekday,
                            periods = schedule.periods,
                            weekPattern = schedule.weekPattern
                        )
                    }
                )
            }
        )
    }
}
