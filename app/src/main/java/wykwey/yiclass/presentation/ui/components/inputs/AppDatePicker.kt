package wykwey.yiclass.presentation.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import java.time.LocalDate
import java.time.YearMonth
import wykwey.yiclass.presentation.theme.LocalAppColors

@Composable
fun AppDatePicker(
    initialDate: LocalDate,
    firstDate: LocalDate = LocalDate.of(1900, 1, 1),
    lastDate: LocalDate = LocalDate.of(2100, 12, 31),
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    Dialog(onDismissRequest = onDismiss) {
        AppCard(modifier = Modifier.widthIn(max = 380.dp)) {
            Column {
                DatePickerHeader(
                    currentMonth = currentMonth,
                    onPrevMonth = {
                        val newMonth = currentMonth.minusMonths(1)
                        if (!newMonth.isBefore(YearMonth.from(firstDate))) currentMonth = newMonth
                    },
                    onNextMonth = {
                        val newMonth = currentMonth.plusMonths(1)
                        if (!newMonth.isAfter(YearMonth.from(lastDate))) currentMonth = newMonth
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                DatePickerCalendar(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    firstDate = firstDate,
                    lastDate = lastDate,
                    onDateSelected = { selectedDate = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppButton(text = "取消", variant = AppButtonVariant.Outlined, onClick = onDismiss)
                    Spacer(modifier = Modifier.width(12.dp))
                    AppButton(
                        text = "确定",
                        onClick = { onConfirm(selectedDate) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DatePickerHeader(
    currentMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIconButton(
            icon = Icons.Filled.ChevronLeft,
            contentDescription = "上个月",
            showBorder = false,
            onClick = onPrevMonth
        )
        Text(
            text = "${currentMonth.year}年${currentMonth.monthValue}月",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        AppIconButton(
            icon = Icons.Filled.ChevronRight,
            contentDescription = "下个月",
            showBorder = false,
            onClick = onNextMonth
        )
    }
}

private val weekDayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

@Composable
private fun DatePickerCalendar(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    firstDate: LocalDate,
    lastDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstDayOfMonth = currentMonth.atDay(1)
    // weekday: 1=Monday..7=Sunday -> 0-indexed Monday start
    val weekdayOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    Column {
        Row {
            weekDayLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f).height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LocalAppColors.current.textPrimary.copy(alpha = 0.6f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        for (week in 0 until 6) {
            Row {
                for (dayIndex in 0 until 7) {
                    val cellIndex = week * 7 + dayIndex
                    Box(modifier = Modifier.weight(1f)) {
                        if (cellIndex < weekdayOffset || cellIndex >= weekdayOffset + daysInMonth) {
                            Box(modifier = Modifier.fillMaxWidth().height(40.dp))
                        } else {
                            val day = cellIndex - weekdayOffset + 1
                            val date = currentMonth.atDay(day)
                            DateCell(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                inRange = !date.isBefore(firstDate) && !date.isAfter(lastDate),
                                onClick = { onDateSelected(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    inRange: Boolean,
    onClick: () -> Unit
) {
    val textColor = when {
        !inRange -> LocalAppColors.current.divider
        isSelected -> LocalAppColors.current.surfaceCard
        else -> LocalAppColors.current.textPrimary
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(2.dp)
            .background(
                if (isSelected) LocalAppColors.current.textPrimary else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .then(
                if (isToday && !isSelected) Modifier.border(1.dp, LocalAppColors.current.textPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(enabled = inRange, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${date.dayOfMonth}",
            fontSize = 15.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
