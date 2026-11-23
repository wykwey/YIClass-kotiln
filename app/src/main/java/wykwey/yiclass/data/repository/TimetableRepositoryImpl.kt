package wykwey.yiclass.data.repository

import androidx.room3.withWriteTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wykwey.yiclass.data.local.AppDatabase
import wykwey.yiclass.data.local.dao.ClassTimeDao
import wykwey.yiclass.data.local.dao.CourseDao
import wykwey.yiclass.data.local.dao.CourseScheduleDao
import wykwey.yiclass.data.local.dao.HolidayDao
import wykwey.yiclass.data.local.dao.TimetableDao
import wykwey.yiclass.domain.model.ClassTime
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.Holiday
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.model.TimetableSettings
import wykwey.yiclass.domain.repository.TimetableRepository
import java.time.LocalDate
import javax.inject.Inject

class TimetableRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val timetableDao: TimetableDao,
    private val courseDao: CourseDao,
    private val courseScheduleDao: CourseScheduleDao,
    private val classTimeDao: ClassTimeDao,
    private val holidayDao: HolidayDao
) : TimetableRepository {

    override fun observeTimetables(): Flow<List<Timetable>> =
        timetableDao.observeAll().map { entities ->
            entities.map { e ->
                Timetable(
                    id = e.id,
                    name = e.name,
                    settings = TimetableSettings(
                        startDate = LocalDate.parse(e.startDate),
                        totalWeeks = e.totalWeeks,
                        showWeekend = e.showWeekend,
                        maxPeriods = e.maxPeriods,
                        reminderMinutes = e.reminderMinutes
                    )
                )
            }
        }

    override fun observeTimetableDetails(id: Long): Flow<Timetable?> =
        timetableDao.observeDetailsById(id).map { it?.toDomain() }

    override suspend fun getTimetableDetails(id: Long): Timetable? =
        timetableDao.getDetailsById(id)?.toDomain()

    override suspend fun getCourse(id: Long): Course? =
        courseDao.getWithSchedules(id)?.toDomain()

    override suspend fun deleteTimetable(id: Long) {
        // courses / schedules / classTimes / holidays 由外键 CASCADE 级联删除
        timetableDao.deleteById(id)
    }

    override suspend fun renameTimetable(id: Long, name: String) = timetableDao.renameById(id, name)

    override suspend fun setReminderMinutes(id: Long, minutes: Int) = timetableDao.updateReminderMinutes(id, minutes)

    override suspend fun setShowWeekend(id: Long, show: Boolean) = timetableDao.updateShowWeekend(id, show)

    override suspend fun setStartDate(id: Long, date: LocalDate) = timetableDao.updateStartDate(id, date.toString())

    override suspend fun updateTimeSettings(id: Long, weeks: Int, periods: Int, times: List<ClassTime>): Boolean =
        db.withWriteTransaction {
            if (timetableDao.updateTimeSettings(id, weeks, periods) == 0) return@withWriteTransaction false
            classTimeDao.deleteByTimetable(id)
            classTimeDao.insertAll(times.map { it.toEntity(id) })
            true
        }

    override suspend fun putHoliday(id: Long, holiday: Holiday) {
        db.withWriteTransaction {
            if (timetableDao.getById(id) == null) return@withWriteTransaction
            holidayDao.deleteByDate(id, holiday.date.toString())
            holidayDao.insert(holiday.toEntity(id))
        }
    }

    override suspend fun deleteHoliday(id: Long, date: LocalDate) = holidayDao.deleteByDate(id, date.toString())

    override suspend fun addCalendarEntries(id: Long, entries: List<Holiday>): List<Holiday> = db.withWriteTransaction {
        val existing = holidayDao.dates(id).toSet()
        val additions = entries.distinctBy { it.date }.filter { it.date.toString() !in existing }
        holidayDao.insertAll(additions.map { it.toEntity(id) })
        additions
    }

    override suspend fun addTimetable(timetable: Timetable): Long = db.withWriteTransaction {
        val newTimetableId = timetableDao.insert(timetable.toEntity().copy(id = 0))
        classTimeDao.insertAll(timetable.settings.classTimes.map { it.toEntity(newTimetableId).copy(id = 0) })
        holidayDao.insertAll(timetable.settings.holidays.map { it.toEntity(newTimetableId).copy(id = 0) })

        timetable.courses.forEach { course ->
            val courseId = courseDao.insert(course.toEntity().copy(id = 0, timetableId = newTimetableId))
            courseScheduleDao.insertAll(course.schedules.map { it.toEntity(courseId).copy(id = 0) })
        }

        newTimetableId
    }

    override suspend fun upsertCourse(course: Course): Long = db.withWriteTransaction {
        // 已存在的课程用 @Update（避免 REPLACE 的 DELETE+INSERT 触发级联删除）
        val courseId = if (course.id != 0L) {
            courseDao.update(course.toEntity())
            course.id
        } else {
            courseDao.insert(course.toEntity())
        }

        // schedule 整体替换：先删后插，避免残留
        courseScheduleDao.deleteByCourse(courseId)
        if (course.schedules.isNotEmpty()) {
            courseScheduleDao.insertAll(course.schedules.map { it.toEntity(courseId) })
        }
        courseId
    }

    override suspend fun deleteCourse(id: Long) {
        // schedules 由外键 CASCADE 级联删除
        courseDao.deleteById(id)
    }
}
