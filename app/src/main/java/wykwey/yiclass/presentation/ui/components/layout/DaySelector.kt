package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.layout.clickableNoRipple
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import wykwey.yiclass.domain.util.WeekCalculator

private val WEEKDAY_NAMES = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

/**
 * 日期选择条，对齐原版 day_view.dart 的 DaySelector：
 * 选中态白底黑描边，未选中灰底，每格显示星期 + MM/dd。
 */
@Composable
fun DaySelector(
    startDate: LocalDate,
    currentWeek: Int,
    days: List<Int>,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (day in days) {
            val selected = selectedDay == day
            val date = WeekCalculator.dateInWeek(startDate, currentWeek, day)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .background(if (selected) colors.surfaceCard else colors.border, RoundedCornerShape(8.dp))
                    .then(
                        if (selected) Modifier.border(1.dp, colors.textPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clickableNoRipple { onDaySelected(day) },
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(WEEKDAY_NAMES[day - 1], fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    date.format(DateTimeFormatter.ofPattern("MM/dd")),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
