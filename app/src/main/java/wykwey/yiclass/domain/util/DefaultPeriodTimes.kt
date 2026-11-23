package wykwey.yiclass.domain.util

import java.time.LocalTime

/**
 * 默认节次时间，对齐原 Flutter 版 data_constants.dart 的 defaultPeriodTimes。
 */
object DefaultPeriodTimes {

    /** period(1..16) → (start, end)。 */
    val times: Map<Int, Pair<LocalTime, LocalTime>> = mapOf(
        1 to (LocalTime.of(8, 0) to LocalTime.of(8, 45)),
        2 to (LocalTime.of(8, 50) to LocalTime.of(9, 35)),
        3 to (LocalTime.of(9, 40) to LocalTime.of(10, 25)),
        4 to (LocalTime.of(10, 30) to LocalTime.of(11, 15)),
        5 to (LocalTime.of(11, 20) to LocalTime.of(12, 5)),
        6 to (LocalTime.of(13, 30) to LocalTime.of(14, 15)),
        7 to (LocalTime.of(14, 20) to LocalTime.of(15, 5)),
        8 to (LocalTime.of(15, 10) to LocalTime.of(15, 55)),
        9 to (LocalTime.of(16, 0) to LocalTime.of(16, 45)),
        10 to (LocalTime.of(16, 50) to LocalTime.of(17, 35)),
        11 to (LocalTime.of(18, 30) to LocalTime.of(19, 15)),
        12 to (LocalTime.of(19, 20) to LocalTime.of(20, 5)),
        13 to (LocalTime.of(20, 10) to LocalTime.of(20, 55)),
        14 to (LocalTime.of(21, 0) to LocalTime.of(21, 45)),
        15 to (LocalTime.of(21, 50) to LocalTime.of(22, 35)),
        16 to (LocalTime.of(22, 40) to LocalTime.of(23, 25)),
    )

    fun startEnd(period: Int): Pair<LocalTime, LocalTime> =
        times[period] ?: (LocalTime.MIDNIGHT to LocalTime.MIDNIGHT)
}
