package wykwey.yiclass.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppButtonVariant
import wykwey.yiclass.presentation.ui.components.inputs.AppDatePicker
import wykwey.yiclass.presentation.ui.components.inputs.AppDropdown
import wykwey.yiclass.presentation.ui.components.inputs.AppDropdownItem
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.ui.components.layout.SettingsItem
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar
import wykwey.yiclass.presentation.viewmodel.AcademicCalendarViewModel

private val calendarOptions = listOf(
    AppDropdownItem(0, "停课"), AppDropdownItem(1, "按周一补课"), AppDropdownItem(2, "按周二补课"),
    AppDropdownItem(3, "按周三补课"), AppDropdownItem(4, "按周四补课"), AppDropdownItem(5, "按周五补课"),
    AppDropdownItem(6, "按周六补课"), AppDropdownItem(7, "按周日补课")
)

@Composable
fun AcademicCalendarScreen(onBack: () -> Unit, viewModel: AcademicCalendarViewModel = hiltViewModel()) {
    val timetable by viewModel.timetable.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val holidays = timetable?.settings?.holidays.orEmpty().sortedBy { it.date }
    var choosingDate by rememberSaveable { mutableStateOf(false) }
    var editingDate by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedOption by rememberSaveable { mutableStateOf<Int?>(0) }

    fun edit(date: LocalDate) {
        val holiday = holidays.firstOrNull { it.date == date }
        editingDate = date.toString()
        selectedOption = if (holiday?.isMakeup == true) holiday.makeupWeekday else 0
    }

    Scaffold(
        containerColor = LocalAppColors.current.pageBackground,
        topBar = { YIClassAppBar(title = "节假日与补课", centerTitle = true, showBack = true, onBack = onBack) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (timetable != null) {
                    item {
                        SettingsBlock {
                            SettingsItem(
                                title = "导入法定节假日",
                                description = "导入本学期的官方放假与调休日期",
                                showArrow = true,
                                onClick = viewModel::importHolidays
                            )
                            SettingsItem(
                                title = "添加停课或补课",
                                showArrow = true,
                                onClick = { choosingDate = true }
                            )
                        }
                    }
                }
                if (holidays.isEmpty()) {
                    item { Text("暂无停课或补课安排", color = LocalAppColors.current.textSecondary) }
                }
                items(holidays, key = { it.date.toString() }) { holiday ->
                    SettingsBlock {
                        SettingsItem(
                            title = holiday.date.toString(),
                            description = when {
                                !holiday.isMakeup -> "停课"
                                holiday.makeupWeekday == null -> "调休 · 待选补课星期"
                                else -> calendarOptions.first { it.value == holiday.makeupWeekday }.label
                            },
                            onClick = { edit(holiday.date) },
                            trailing = {
                                AppIconButton(Icons.Filled.DeleteOutline, "删除 ${holiday.date}", onClick = { viewModel.delete(holiday.date) })
                            }
                        )
                    }
                }
            }
            SonnerHost(message, viewModel::clearMessage, Modifier.align(Alignment.BottomCenter))
        }
    }

    if (choosingDate) {
        AppDatePicker(
            initialDate = LocalDate.now(),
            onConfirm = { date ->
                choosingDate = false
                edit(date)
            },
            onDismiss = { choosingDate = false }
        )
    }
    editingDate?.let { date ->
        CalendarEditDialog(
            date = date,
            option = selectedOption,
            onOptionChange = { selectedOption = it },
            onSave = {
                viewModel.save(LocalDate.parse(date), selectedOption?.takeUnless { it == 0 })
                editingDate = null
            },
            onDismiss = { editingDate = null }
        )
    }
}

@Composable
private fun CalendarEditDialog(
    date: String,
    option: Int?,
    onOptionChange: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    Dialog(onDismissRequest = onDismiss) {
        AppCard(contentPadding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(date, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                AppDropdown(
                    value = option,
                    items = calendarOptions,
                    onValueChange = onOptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    hint = "选择补课星期"
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    AppButton(text = "取消", variant = AppButtonVariant.Outlined, onClick = onDismiss)
                    AppButton(text = "保存", enabled = option != null, onClick = onSave)
                }
            }
        }
    }
}
