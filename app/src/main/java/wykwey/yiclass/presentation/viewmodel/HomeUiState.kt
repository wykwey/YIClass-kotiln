package wykwey.yiclass.presentation.viewmodel

import wykwey.yiclass.domain.model.Timetable
import java.time.LocalDate

enum class ViewMode { WEEK, DAY, LIST, SETTINGS }

data class HomeUiState(
    val viewMode: ViewMode = ViewMode.WEEK,
    val currentWeek: Int = 1,
    val totalWeeks: Int = 20,
    /** "今天"所在的教学周；超出学期范围（未开始/已结束）为 null。日视图始终用它，不受周视图的周切换影响。 */
    val todayWeek: Int? = null,
    val selectedDay: Int = LocalDate.now().dayOfWeek.value,
    val currentTimetable: Timetable? = null,
    val loading: Boolean = true
)
