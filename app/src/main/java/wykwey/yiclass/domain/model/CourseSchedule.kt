package wykwey.yiclass.domain.model

/**
 * 一门课程的一条上课安排。
 *
 * @param weekday 1=周一 ~ 7=周日
 * @param periods 节次列表，如 [1, 2] 表示连上第 1、2 节
 * @param weekPattern 生效周次，如 [1,2,3,4,5,6]
 */
data class CourseSchedule(
    val id: Long = 0,
    val courseId: Long = 0,
    val weekday: Int,
    val periods: List<Int>,
    val weekPattern: List<Int>,
    val reminder: String = ""
)
