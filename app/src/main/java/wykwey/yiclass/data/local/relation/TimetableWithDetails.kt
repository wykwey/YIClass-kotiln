package wykwey.yiclass.data.local.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import wykwey.yiclass.data.local.entity.ClassTimeEntity
import wykwey.yiclass.data.local.entity.CourseEntity
import wykwey.yiclass.data.local.entity.HolidayEntity
import wykwey.yiclass.data.local.entity.TimetableEntity

/**
 * 课表 + 其下全部课程（含每门课的上课安排）+ 节次时间 + 节假日。
 *
 * 课程的 schedules 通过 CourseEntity → CourseScheduleEntity 的嵌套 @Relation
 * 由 Room 自动填充（entity = CourseEntity，returns CourseWithSchedules）。
 */
data class TimetableWithDetails(
    @Embedded val timetable: TimetableEntity,
    @Relation(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        entityColumns = ["timetableId"]
    )
    val courses: List<CourseWithSchedules>,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["timetableId"]
    )
    val classTimes: List<ClassTimeEntity>,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["timetableId"]
    )
    val holidays: List<HolidayEntity>
)
