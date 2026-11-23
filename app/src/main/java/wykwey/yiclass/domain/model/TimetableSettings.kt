package wykwey.yiclass.domain.model

import java.time.LocalDate

data class TimetableSettings(
    val startDate: LocalDate,
    val totalWeeks: Int = 20,
    val showWeekend: Boolean = false,
    val maxPeriods: Int = 12,
    val reminderMinutes: Int = 15,
    val classTimes: List<ClassTime> = emptyList(),
    val holidays: List<Holiday> = emptyList()
)
