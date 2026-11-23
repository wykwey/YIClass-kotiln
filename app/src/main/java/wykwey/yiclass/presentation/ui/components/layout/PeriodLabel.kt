package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.domain.model.ClassTime
import wykwey.yiclass.domain.util.DefaultPeriodTimes
import java.time.format.DateTimeFormatter
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 周视图左侧节次标签：节次序号 + 上下课时间，对齐原版 period_label.dart。
 */
@Composable
fun PeriodLabel(
    period: Int,
    classTimes: List<ClassTime>,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val ct = classTimes.firstOrNull { it.period == period }
    val (start, end) = if (ct != null) ct.startTime to ct.endTime else DefaultPeriodTimes.startEnd(period)
    val fmt = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = modifier
            .width(width)
            .height(height)
            .drawGridBorder(LocalAppColors.current.border),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "$period", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(text = start.format(fmt), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = end.format(fmt), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
