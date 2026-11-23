package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.presentation.theme.CourseColors
import wykwey.yiclass.presentation.theme.LocalAppColors

private val WEEKDAY_NAMES = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

enum class CourseInfoCardMode { DayView, ListView }

/**
 * 课程信息卡片，对齐原版 course_card.dart：白底 + 课程色描边（日视图左边框 / 列表视图顶边框），
 * 与网格版 [CourseCard]（周视图专用）是不同的展示形态。
 */
@Composable
fun CourseInfoCard(
    course: Course,
    mode: CourseInfoCardMode,
    modifier: Modifier = Modifier,
    selectedDay: Int? = null,
    onClick: () -> Unit = {}
) {
    val color = if (course.color != 0) Color(course.color) else CourseColors.colorForName(course.name)
    val timeText = when (mode) {
        CourseInfoCardMode.DayView -> selectedDay?.let { day ->
            course.schedules.filter { it.weekday == day }.joinToString("\n") { s ->
                "${WEEKDAY_NAMES[s.weekday - 1]} 第${s.periods.joinToString(",")}节"
            }
        }.orEmpty()
        CourseInfoCardMode.ListView -> course.schedules.joinToString("\n") { s ->
            "${WEEKDAY_NAMES[s.weekday - 1]} 第${s.periods.joinToString(",")}节"
        }
    }
    val borderWidth = 4.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LocalAppColors.current.surfaceCard)
            .drawBehind {
                val stroke = borderWidth.toPx()
                when (mode) {
                    CourseInfoCardMode.DayView -> drawLine(
                        color = color,
                        start = Offset(stroke / 2, 0f),
                        end = Offset(stroke / 2, size.height),
                        strokeWidth = stroke
                    )
                    CourseInfoCardMode.ListView -> drawLine(
                        color = color,
                        start = Offset(0f, stroke / 2),
                        end = Offset(size.width, stroke / 2),
                        strokeWidth = stroke
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(
                start = if (mode == CourseInfoCardMode.DayView) borderWidth + 12.dp else 12.dp,
                top = if (mode == CourseInfoCardMode.ListView) borderWidth + 8.dp else 12.dp,
                end = 12.dp,
                bottom = 12.dp
            )
    ) {
        Column {
            Text(course.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            if (timeText.isNotEmpty()) {
                Text(timeText, fontSize = 13.sp, color = LocalAppColors.current.accent)
            }
            if (course.teacher.isNotEmpty()) {
                Text("教师: ${course.teacher}", fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
            }
            if (course.location.isNotEmpty()) {
                Text("地点: ${course.location}", fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
            }
        }
    }
}
