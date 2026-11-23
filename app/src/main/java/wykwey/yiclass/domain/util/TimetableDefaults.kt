package wykwey.yiclass.domain.util

import wykwey.yiclass.domain.model.ClassTime
import wykwey.yiclass.domain.model.TimetableSettings
import java.time.LocalDate

/**
 * 新建课表的默认设置，对齐原 Flutter 版 FactoryService.createSettings：
 * 总周数 20、不显示周末、开始日期为今天，节次时间按 [DefaultPeriodTimes] 生成。
 */
object TimetableDefaults {

    const val DEFAULT_MAX_PERIODS = 12

    fun defaultSettings(
        startDate: LocalDate = LocalDate.now(),
        maxPeriods: Int = DEFAULT_MAX_PERIODS
    ): TimetableSettings = TimetableSettings(
        startDate = startDate,
        totalWeeks = 20,
        showWeekend = false,
        maxPeriods = maxPeriods,
        reminderMinutes = 15,
        classTimes = defaultClassTimes(maxPeriods)
    )

    fun defaultClassTimes(maxPeriods: Int): List<ClassTime> =
        (1..maxPeriods).map { p ->
            val (start, end) = DefaultPeriodTimes.startEnd(p)
            ClassTime(period = p, startTime = start, endTime = end)
        }
}
