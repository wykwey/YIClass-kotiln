package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.presentation.theme.CourseColors
import wykwey.yiclass.presentation.theme.LocalDisableAdaptiveFontColor
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 周视图里的单门课程卡片，对齐原版 week_course_card.dart：
 * 柔和背景 + 深色对比文字，圆角 4，展示课名 / 教师 / @地点，冲突时角标提示。
 */
@Composable
fun CourseCard(
    course: Course,
    compact: Boolean,
    hasConflict: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val baseColor = if (course.color != 0) Color(course.color) else CourseColors.colorForName(course.name)
    val disableAdaptive = LocalDisableAdaptiveFontColor.current
    val textColor = CourseColors.textColorFor(baseColor, disableAdaptive = disableAdaptive)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .background(baseColor, RoundedCornerShape(4.dp))
            .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Text(
            text = course.name,
            color = textColor,
            fontSize = if (compact) 12.sp else 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (course.teacher.isNotEmpty()) {
            Text(
                text = course.teacher,
                color = textColor,
                fontSize = if (compact) 11.sp else 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (course.location.isNotEmpty()) {
            Text(
                text = "@${course.location}",
                color = textColor,
                fontSize = if (compact) 11.sp else 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (hasConflict) {
            Text(
                text = "冲突",
                color = LocalAppColors.current.danger,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
