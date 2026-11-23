package wykwey.yiclass.presentation.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.util.TimetableQuery
import wykwey.yiclass.presentation.ui.components.layout.CourseInfoCard
import wykwey.yiclass.presentation.ui.components.layout.CourseInfoCardMode
import wykwey.yiclass.presentation.ui.components.layout.DaySelector
import java.time.LocalDate

/**
 * 日视图：日期条 + 当天课程列表，对齐原版 day_view.dart。
 * 日视图始终使用"今天"所在的周，不受周视图的周切换影响；
 * [todayWeek] 为 null 表示今天在学期范围外（未开始/已结束）。
 */
@Composable
fun DayView(
    timetable: Timetable,
    todayWeek: Int?,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onCourseClick: (courseId: Long) -> Unit = {}
) {
    if (todayWeek == null) {
        val isNotStarted = LocalDate.now().isBefore(timetable.settings.startDate)
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isNotStarted) "学期尚未开始" else "学期已结束",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val days = TimetableQuery.visibleDays(timetable, todayWeek)
    val day = selectedDay.takeIf { it in days } ?: days.last()
    val dayCourses = TimetableQuery.dayCourses(timetable, todayWeek, day)

    Column(modifier = modifier.fillMaxSize()) {
        DaySelector(
            startDate = timetable.settings.startDate,
            currentWeek = todayWeek,
            days = days,
            selectedDay = day,
            onDaySelected = onDaySelected
        )
        if (dayCourses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("当天无课程", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dayCourses, key = { it.id }) { course ->
                    CourseInfoCard(
                        course = course,
                        mode = CourseInfoCardMode.DayView,
                        selectedDay = day,
                        onClick = { onCourseClick(course.id) }
                    )
                }
            }
        }
    }
}
