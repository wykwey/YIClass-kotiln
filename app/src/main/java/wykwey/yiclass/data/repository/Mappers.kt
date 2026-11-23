package wykwey.yiclass.data.repository

import kotlinx.serialization.json.Json
import wykwey.yiclass.data.local.entity.ClassTimeEntity
import wykwey.yiclass.data.local.entity.CourseEntity
import wykwey.yiclass.data.local.entity.CourseScheduleEntity
import wykwey.yiclass.data.local.entity.HolidayEntity
import wykwey.yiclass.data.local.entity.TimetableEntity
import wykwey.yiclass.data.local.relation.CourseWithSchedules
import wykwey.yiclass.data.local.relation.TimetableWithDetails
import wykwey.yiclass.domain.model.ClassTime
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.CourseSchedule
import wykwey.yiclass.domain.model.Holiday
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.model.TimetableSettings
import java.time.LocalDate
import java.time.LocalTime

/**
 * Entity ↔ Domain 映射。
 *
 * - periods / weekPattern：Room 存 JSON 字符串（如 "[1,2]"），domain 用 List<Int>
 * - startDate / date：ISO-8601 LocalDate 字符串
 * - startTime / endTime：LocalTime，格式 "HH:mm"
 */
private val json = Json { ignoreUnknownKeys = true }

private fun List<Int>.toJson(): String = json.encodeToString(this)

private fun String.toIntList(): List<Int> =
    if (isBlank()) emptyList() else json.decodeFromString(this)

// ── CourseSchedule ──────────────────────────────────────────────

fun CourseScheduleEntity.toDomain(): CourseSchedule = CourseSchedule(
    id = id,
    courseId = courseId,
    weekday = weekday,
    periods = periods.toIntList(),
    weekPattern = weekPattern.toIntList(),
    reminder = reminder
)

fun CourseSchedule.toEntity(courseId: Long): CourseScheduleEntity = CourseScheduleEntity(
    id = id,
    courseId = courseId,
    weekday = weekday,
    periods = periods.toJson(),
    weekPattern = weekPattern.toJson(),
    reminder = reminder
)

// ── Course ──────────────────────────────────────────────────────

fun CourseWithSchedules.toDomain(): Course = Course(
    id = course.id,
    timetableId = course.timetableId,
    name = course.name,
    location = course.location,
    teacher = course.teacher,
    color = course.color,
    schedules = schedules.map { it.toDomain() }
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id,
    timetableId = timetableId,
    name = name,
    location = location,
    teacher = teacher,
    color = color
)

// ── ClassTime ───────────────────────────────────────────────────

fun ClassTimeEntity.toDomain(): ClassTime = ClassTime(
    id = id,
    timetableId = timetableId,
    period = period,
    startTime = LocalTime.parse(startTime),
    endTime = LocalTime.parse(endTime)
)

fun ClassTime.toEntity(timetableId: Long): ClassTimeEntity = ClassTimeEntity(
    id = id,
    timetableId = timetableId,
    period = period,
    startTime = startTime.toString(),
    endTime = endTime.toString()
)

// ── Holiday ─────────────────────────────────────────────────────

fun HolidayEntity.toDomain(): Holiday = Holiday(
    id = id,
    timetableId = timetableId,
    date = LocalDate.parse(date),
    isMakeup = isMakeup,
    makeupWeekday = makeupWeekday
)

fun Holiday.toEntity(timetableId: Long): HolidayEntity = HolidayEntity(
    id = id,
    timetableId = timetableId,
    date = date.toString(),
    isMakeup = isMakeup,
    makeupWeekday = makeupWeekday
)

// ── Timetable ───────────────────────────────────────────────────

fun TimetableWithDetails.toDomain(): Timetable = Timetable(
    id = timetable.id,
    name = timetable.name,
    settings = TimetableSettings(
        startDate = LocalDate.parse(timetable.startDate),
        totalWeeks = timetable.totalWeeks,
        showWeekend = timetable.showWeekend,
        maxPeriods = timetable.maxPeriods,
        reminderMinutes = timetable.reminderMinutes,
        classTimes = classTimes.map { it.toDomain() },
        holidays = holidays.map { it.toDomain() }
    ),
    courses = courses.map { it.toDomain() }
)

fun Timetable.toEntity(): TimetableEntity = TimetableEntity(
    id = id,
    name = name,
    startDate = settings.startDate.toString(),
    totalWeeks = settings.totalWeeks,
    showWeekend = settings.showWeekend,
    maxPeriods = settings.maxPeriods,
    reminderMinutes = settings.reminderMinutes
)
