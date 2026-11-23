package wykwey.yiclass.domain.repository

import kotlinx.coroutines.flow.Flow
import wykwey.yiclass.domain.model.ClassTime
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.Holiday
import wykwey.yiclass.domain.model.Timetable
import java.time.LocalDate

/**
 * 课表数据访问入口。实现层负责 entity↔domain 映射与事务组合。
 */
interface TimetableRepository {

    /** 所有课表（仅基本信息，不含课程明细）。 */
    fun observeTimetables(): Flow<List<Timetable>>

    /** 指定课表的完整明细（课程 + 节次 + 节假日），随数据变化持续发射。 */
    fun observeTimetableDetails(id: Long): Flow<Timetable?>

    suspend fun getTimetableDetails(id: Long): Timetable?

    /** 单门课程（含上课安排）；不存在返回 null。 */
    suspend fun getCourse(id: Long): Course?

    suspend fun deleteTimetable(id: Long)

    /** 仅重命名，不影响课程/节次/节假日等其余数据。 */
    suspend fun renameTimetable(id: Long, name: String)

    suspend fun setReminderMinutes(id: Long, minutes: Int)

    suspend fun setShowWeekend(id: Long, show: Boolean)

    suspend fun setStartDate(id: Long, date: LocalDate)

    suspend fun updateTimeSettings(id: Long, weeks: Int, periods: Int, times: List<ClassTime>): Boolean

    suspend fun putHoliday(id: Long, holiday: Holiday)

    suspend fun addCalendarEntries(id: Long, entries: List<Holiday>): List<Holiday>

    suspend fun deleteHoliday(id: Long, date: LocalDate)

    /** 将课表、节次、调休和课程在同一个事务内写入。 */
    suspend fun addTimetable(timetable: Timetable): Long

    /** 保存课程及其上课安排（先删旧 schedule 再插新），返回课程 id。 */
    suspend fun upsertCourse(course: Course): Long

    suspend fun deleteCourse(id: Long)
}
