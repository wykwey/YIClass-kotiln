package wykwey.yiclass.domain.model

import java.time.LocalTime

/** 单个节次的上下课时间。 */
data class ClassTime(
    val id: Long = 0,
    val timetableId: Long = 0,
    val period: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)
