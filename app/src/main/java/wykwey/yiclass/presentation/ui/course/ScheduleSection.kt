package wykwey.yiclass.presentation.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppButtonVariant
import wykwey.yiclass.presentation.ui.components.inputs.AppDropdown
import wykwey.yiclass.presentation.ui.components.inputs.AppDropdownItem
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.inputs.AppTextField
import wykwey.yiclass.presentation.ui.components.layout.SectionCard
import wykwey.yiclass.presentation.viewmodel.ScheduleInput
import wykwey.yiclass.presentation.theme.LocalAppColors

private val weekdayOptions = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    .mapIndexed { index, label -> AppDropdownItem(index + 1, label) }

@Composable
fun ScheduleSection(
    schedules: List<ScheduleInput>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onUpdate: (Int, (ScheduleInput) -> ScheduleInput) -> Unit
) {
    SectionCard("时间安排") {
        Column {
            schedules.forEachIndexed { index, input ->
                ScheduleCard(
                    index = index,
                    input = input,
                    canDelete = schedules.size > 1,
                    onRemove = { onRemove(index) },
                    onWeekdayChange = { wd -> onUpdate(index) { it.copy(weekday = wd) } },
                    onWeekPatternChange = { txt -> onUpdate(index) { it.copy(weekPatternText = txt) } },
                    onPeriodsChange = { txt -> onUpdate(index) { it.copy(periodsText = txt) } }
                )
            }
            AppButton(
                text = "添加时间段",
                variant = AppButtonVariant.Outlined,
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ScheduleCard(
    index: Int,
    input: ScheduleInput,
    canDelete: Boolean,
    onRemove: () -> Unit,
    onWeekdayChange: (Int) -> Unit,
    onWeekPatternChange: (String) -> Unit,
    onPeriodsChange: (String) -> Unit
) {
    // 时间段卡片背灰、白卡片描边，仅此处灰色（对齐原版 grey[50] 填充 + grey[200] 描边）
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(LocalAppColors.current.surfaceCardSub, RoundedCornerShape(12.dp))
            .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("时间段 ${index + 1}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (canDelete) {
                AppIconButton(
                    icon = Icons.Filled.DeleteOutline,
                    contentDescription = "删除时间段",
                    tint = LocalAppColors.current.dangerLight,
                    showBorder = false,
                    onClick = onRemove
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeekdayDropdown(
                weekday = input.weekday,
                onWeekdayChange = onWeekdayChange,
                modifier = Modifier.width(104.dp)
            )
            AppTextField(
                value = input.weekPatternText,
                onValueChange = onWeekPatternChange,
                label = "周次",
                placeholder = "如: 1-16, 1,3,5",
                modifier = Modifier.weight(1f)
            )
        }
        AppTextField(
            value = input.periodsText,
            onValueChange = onPeriodsChange,
            label = "节次",
            placeholder = "如: 1-2, 3, 5-6",
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )
    }
}

@Composable
private fun WeekdayDropdown(
    weekday: Int,
    onWeekdayChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = "星期",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = LocalAppColors.current.textPrimary.copy(alpha = 0.85f)
        )
        Spacer(Modifier.height(8.dp))
        AppDropdown(weekday, weekdayOptions, onWeekdayChange, Modifier.fillMaxWidth())
    }
}
