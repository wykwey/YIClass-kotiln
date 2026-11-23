package wykwey.yiclass.data.local.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import wykwey.yiclass.data.local.entity.CourseEntity
import wykwey.yiclass.data.local.entity.CourseScheduleEntity

/**
 * 课程及其全部上课安排（一对多）。
 */
data class CourseWithSchedules(
    @Embedded val course: CourseEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["courseId"]
    )
    val schedules: List<CourseScheduleEntity>
)
