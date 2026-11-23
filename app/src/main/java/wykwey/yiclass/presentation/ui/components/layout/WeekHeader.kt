package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import wykwey.yiclass.domain.model.Holiday
import wykwey.yiclass.domain.util.WeekCalculator
import wykwey.yiclass.presentation.theme.LocalAppColors

private val WEEKDAY_NAMES = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

/**
 * 周视图表头：月份格 + 每天（星期 + MM/dd 日期），对齐原版 week_header.dart。
 */
@Composable
fun WeekHeader(
    startDate: LocalDate,
    currentWeek: Int,
    holidays: List<Holiday>,
    days: List<Int>,
    monthLabelWidth: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val monthDate = WeekCalculator.dateInWeek(startDate, currentWeek, 1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawBottomBorder(LocalAppColors.current.border)
    ) {
        Box(
            modifier = Modifier
                .width(monthLabelWidth)
                .height(height)
                .drawRightBorder(LocalAppColors.current.border),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${monthDate.monthValue}\n月",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        for (day in days) {
            val date = monthDate.plusDays(day - 1L)
            val holiday = holidays.firstOrNull { it.date == date }
            val weekday = WEEKDAY_NAMES[day - 1] + when {
                holiday == null -> ""
                holiday.isMakeup && holiday.makeupWeekday == null -> " 调"
                holiday.isMakeup -> " 补"
                else -> " 休"
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(height),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = weekday,
                    maxLines = 1,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MM/dd")),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
