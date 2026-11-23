package wykwey.yiclass.presentation.ui.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 时间范围选择器，对齐原 Flutter 版 YicoreTimeRangePicker：底部弹窗，
 * 4 个滚轮（时:分-时:分）一次性同时选择开始和结束时间，非分步选择。
 * 确定时若结束时间早于开始时间，自动回退为开始时间（对齐原版行为）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimeRangePicker(
    initialStart: LocalTime,
    initialEnd: LocalTime,
    onConfirm: (LocalTime, LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var startHour by remember { mutableIntStateOf(initialStart.hour) }
    var startMinute by remember { mutableIntStateOf(initialStart.minute) }
    var endHour by remember { mutableIntStateOf(initialEnd.hour) }
    var endMinute by remember { mutableIntStateOf(initialEnd.minute) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LocalAppColors.current.surfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 20.dp)) {
            Text(
                text = "选择时间范围",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(itemCount = 24, selectedIndex = startHour, onSelectedChange = { startHour = it })
                Text(":", fontSize = 26.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 4.dp))
                WheelPicker(itemCount = 60, selectedIndex = startMinute, onSelectedChange = { startMinute = it })
                Spacer(modifier = Modifier.width(10.dp))
                Text("-", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(10.dp))
                WheelPicker(itemCount = 24, selectedIndex = endHour, onSelectedChange = { endHour = it })
                Text(":", fontSize = 26.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 4.dp))
                WheelPicker(itemCount = 60, selectedIndex = endMinute, onSelectedChange = { endMinute = it })
            }

            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AppButton(text = "取消", variant = AppButtonVariant.Outlined, onClick = onDismiss)
                AppButton(
                    text = "确定",
                    onClick = {
                        val startTotal = startHour * 60 + startMinute
                        val endTotal = endHour * 60 + endMinute
                        val (finalEndHour, finalEndMinute) = if (endTotal < startTotal) {
                            startHour to startMinute
                        } else {
                            endHour to endMinute
                        }
                        onConfirm(
                            LocalTime.of(startHour, startMinute),
                            LocalTime.of(finalEndHour, finalEndMinute)
                        )
                    }
                )
            }
        }
    }
}
