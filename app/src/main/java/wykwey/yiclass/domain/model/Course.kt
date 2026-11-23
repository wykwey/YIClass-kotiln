package wykwey.yiclass.domain.model

/**
 * 课程。color 为 ARGB Int，与 Compose Color(argb) 直接兼容。
 */
data class Course(
    val id: Long = 0,
    val timetableId: Long,
    val name: String,
    val location: String = "",
    val teacher: String = "",
    val color: Int,
    val schedules: List<CourseSchedule> = emptyList()
)
