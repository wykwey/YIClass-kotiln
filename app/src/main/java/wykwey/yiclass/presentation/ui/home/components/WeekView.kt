package wykwey.yiclass.presentation.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.util.TimetableQuery
import wykwey.yiclass.presentation.ui.components.layout.CourseCard
import wykwey.yiclass.presentation.ui.components.layout.PeriodLabel
import wykwey.yiclass.presentation.ui.components.layout.WeekHeader
import wykwey.yiclass.presentation.ui.components.layout.clickableNoRipple
import wykwey.yiclass.presentation.ui.components.layout.drawGridBorder
import wykwey.yiclass.presentation.theme.LocalAppColors

private val PERIOD_LABEL_WIDTH = 44.dp
private val HEADER_HEIGHT = 44.dp
private val CELL_HEIGHT = 92.dp

/**
 * 周视图：顶部星期表头 + 左侧节次时间轴 + 每天一列的课程格。
 * 连续节次合并成一个高格子（对齐原版 week_view.dart）。
 */
@Composable
fun WeekView(
    timetable: Timetable,
    currentWeek: Int,
    modifier: Modifier = Modifier,
    onCourseClick: (courseId: Long) -> Unit = {},
    onEmptyClick: (weekday: Int, period: Int) -> Unit = { _, _ -> }
) {
    val settings = timetable.settings
    val days = remember(timetable, currentWeek) { TimetableQuery.visibleDays(timetable, currentWeek) }
    val maxPeriods = settings.maxPeriods
    var conflictingCourses by remember(timetable.id, currentWeek) { mutableStateOf(emptyList<Course>()) }

    Column(modifier = modifier.background(LocalAppColors.current.surfaceCard)) {
        WeekHeader(
            startDate = settings.startDate,
            currentWeek = currentWeek,
            holidays = settings.holidays,
            days = days,
            monthLabelWidth = PERIOD_LABEL_WIDTH,
            height = HEADER_HEIGHT
        )

        // ── 课程网格（可纵向滚动）──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .height(CELL_HEIGHT * maxPeriods)
        ) {
            // 左侧节次时间轴
            Column(modifier = Modifier.width(PERIOD_LABEL_WIDTH)) {
                for (p in 1..maxPeriods) {
                    PeriodLabel(
                        period = p,
                        classTimes = settings.classTimes,
                        width = PERIOD_LABEL_WIDTH,
                        height = CELL_HEIGHT
                    )
                }
            }
            // 每天一列
            for (weekday in days) {
                val coursesByPeriod = remember(timetable, currentWeek, weekday) {
                    val courses = TimetableQuery.dayCourses(timetable, currentWeek, weekday)
                    (1..maxPeriods).associateWith { period ->
                        courses.filter { course -> course.schedules.any { period in it.periods } }
                    }
                }
                DayColumn(
                    coursesByPeriod = coursesByPeriod,
                    weekday = weekday,
                    maxPeriods = maxPeriods,
                    compact = days.size > 5,
                    modifier = Modifier.weight(1f),
                    onCoursesClick = { courses ->
                        if (courses.size == 1) onCourseClick(courses.single().id)
                        else conflictingCourses = courses
                    },
                    onEmptyClick = onEmptyClick
                )
            }
        }
    }
    if (conflictingCourses.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { conflictingCourses = emptyList() },
            title = { Text("选择课程") },
            text = {
                Column {
                    conflictingCourses.forEach { course ->
                        TextButton(onClick = {
                            conflictingCourses = emptyList()
                            onCourseClick(course.id)
                        }) { Text(listOf(course.name, course.location).filter(String::isNotBlank).joinToString(" · ")) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { conflictingCourses = emptyList() }) { Text("关闭") } }
        )
    }
}

/** 单天课程网格。连续节次拥有同一组课程时合并显示。 */
@Composable
private fun DayColumn(
    coursesByPeriod: Map<Int, List<Course>>,
    weekday: Int,
    maxPeriods: Int,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onCoursesClick: (List<Course>) -> Unit,
    onEmptyClick: (Int, Int) -> Unit
) {
    Column(modifier = modifier) {
        var period = 1
        while (period <= maxPeriods) {
            val courses = coursesByPeriod.getValue(period)
            if (courses.isEmpty()) {
                val p = period
                // 空节次可直接打开课程编辑页。
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CELL_HEIGHT)
                        .drawGridBorder(LocalAppColors.current.border)
                        .clickableNoRipple { onEmptyClick(weekday, p) }
                )
                period++
            } else {
                val span = (period..maxPeriods).takeWhile { coursesByPeriod.getValue(it) == courses }.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CELL_HEIGHT * span)
                        .drawGridBorder(LocalAppColors.current.border)
                ) {
                    CourseCard(
                        course = courses.first(),
                        compact = compact,
                        hasConflict = courses.size > 1,
                        onClick = { onCoursesClick(courses) }
                    )
                }
                period += span
            }
        }
    }
}
