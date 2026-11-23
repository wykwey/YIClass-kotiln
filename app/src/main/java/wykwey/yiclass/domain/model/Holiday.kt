package wykwey.yiclass.domain.model

import java.time.LocalDate

/**
 * 节假日 / 补课日。
 *
 * @param isMakeup 调休日或补课日为 true，停课日为 false。
 * @param makeupWeekday 按星期几的课表补课。调休日为 null 时，等待用户确认星期。
 */
data class Holiday(
    val id: Long = 0,
    val timetableId: Long = 0,
    val date: LocalDate,
    val isMakeup: Boolean = false,
    val makeupWeekday: Int? = null
)
