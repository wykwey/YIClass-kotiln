package wykwey.yiclass.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.inputs.AppSlider
import wykwey.yiclass.presentation.ui.components.inputs.AppTextField
import wykwey.yiclass.presentation.ui.components.inputs.AppTimeRangePicker
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.ui.components.layout.SettingsItem
import wykwey.yiclass.presentation.ui.components.layout.SettingsSwitchItem
import wykwey.yiclass.domain.util.WeekCalculator
import wykwey.yiclass.presentation.viewmodel.TimeSettingsUiState
import wykwey.yiclass.presentation.viewmodel.TimeSettingsViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar

private val PageBackground: Color
    @Composable get() = LocalAppColors.current.pageBackground
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val shortDateFormatter = DateTimeFormatter.ofPattern("MM/dd")
private val chineseNumbers = listOf(
    "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
    "十一", "十二", "十三", "十四", "十五", "十六"
)

private fun chineseNumber(n: Int) = if (n in 1..16) chineseNumbers[n - 1] else n.toString()

private fun formatDateRange(startDate: java.time.LocalDate, weeks: Int): String {
    val endDate = WeekCalculator.dateInWeek(startDate, weeks, 7)
    return "${startDate.format(shortDateFormatter)} - ${endDate.format(shortDateFormatter)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSettingsScreen(
    onDone: () -> Unit,
    viewModel: TimeSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingPeriod by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = PageBackground,
            topBar = {
                YIClassAppBar(
                    title = "课程时间设置",
                    centerTitle = true,
                    showBack = true,
                    onBack = onDone,
                    actions = {
                        AppIconButton(
                            icon = Icons.Filled.Save,
                            contentDescription = "保存",
                            showBorder = false,
                            onClick = viewModel::save,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
            }
        ) { innerPadding ->
            if (state.loading) {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("加载中…")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PageBackground)
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SettingsBlock(title = "基础设置") {
                            SettingsItem(
                                title = "总周数 (${state.totalWeeks}周)",
                                description = formatDateRange(state.startDate, state.totalWeeks),
                                trailing = {
                                    AppSlider(
                                        value = state.totalWeeks.toFloat(),
                                        onValueChange = { viewModel.setTotalWeeks(it.roundToInt()) },
                                        valueRange = 1f..20f,
                                        width = 140.dp
                                    )
                                }
                            )
                            SettingsItem(
                                title = "最大节数",
                                description = "共 ${state.maxPeriods} 节",
                                trailing = {
                                    AppSlider(
                                        value = state.maxPeriods.toFloat(),
                                        onValueChange = { viewModel.setMaxPeriods(it.roundToInt()) },
                                        valueRange = 1f..16f,
                                        width = 140.dp
                                    )
                                }
                            )
                        }
                    }

                    item {
                        SettingsBlock(title = "快捷设置") {
                            SettingsSwitchItem(
                                title = "每节课时长相同",
                                description = "自动推算每节课的开始和结束时间",
                                checked = state.useSameDuration,
                                onCheckedChange = viewModel::setUseSameDuration
                            )
                        }
                    }

                    if (state.useSameDuration) {
                        item {
                            SettingsBlock {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppTextField(
                                        value = state.durationMinutes,
                                        onValueChange = viewModel::setDurationMinutes,
                                        label = "统一时长（分钟）",
                                        placeholder = "45",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    AppButton(text = "应用", onClick = viewModel::applySameDuration)
                                }
                            }
                        }
                    }

                    item {
                        SettingsBlock(title = "时间段设置") {
                            TimeSlotSection("上午", (1..4), state, editingPeriod = state.useSameDuration) { p ->
                                editingPeriod = p
                            }
                            TimeSlotSection("下午", (5..9), state, editingPeriod = state.useSameDuration) { p ->
                                editingPeriod = p
                            }
                            TimeSlotSection("晚上", (10..16), state, editingPeriod = state.useSameDuration) { p ->
                                editingPeriod = p
                            }
                        }
                    }
                }
            }
        }

        SonnerHost(
            message = state.message,
            onDismiss = viewModel::clearMessage,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    editingPeriod?.let { period ->
        val current = state.classTimes[period]
        if (current != null) {
            AppTimeRangePicker(
                initialStart = current.startTime,
                initialEnd = current.endTime,
                onConfirm = { start, end ->
                    viewModel.updateClassTime(period, start, end)
                    editingPeriod = null
                },
                onDismiss = { editingPeriod = null }
            )
        }
    }
}

@Composable
private fun TimeSlotSection(
    label: String,
    periods: IntRange,
    state: TimeSettingsUiState,
    editingPeriod: Boolean,
    onEditPeriod: (Int) -> Unit
) {
    val visible = periods.filter { it <= state.maxPeriods }
    if (visible.isEmpty()) return

    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = LocalAppColors.current.textSecondary,
        modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
    )
    visible.forEach { period ->
        val ct = state.classTimes[period]
        val timeStr = if (ct != null) "${ct.startTime.format(timeFormatter)}-${ct.endTime.format(timeFormatter)}" else "未设置"
        SettingsItem(
            title = "第${chineseNumber(period)}节",
            value = timeStr,
            showArrow = !editingPeriod,
            enabled = !editingPeriod,
            onClick = { onEditPeriod(period) }
        )
    }
}
