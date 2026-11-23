package wykwey.yiclass.presentation.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.BuildConfig
import wykwey.yiclass.presentation.navigation.AppRoute
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppDatePicker
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.ui.components.layout.SettingsItem
import wykwey.yiclass.presentation.ui.components.layout.SettingsSwitchItem
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar
import wykwey.yiclass.presentation.viewmodel.ImportExportViewModel
import wykwey.yiclass.presentation.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import wykwey.yiclass.presentation.theme.LocalAppColors

private val PageBackground: Color
    @Composable get() = LocalAppColors.current.pageBackground
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    importExportViewModel: ImportExportViewModel = hiltViewModel()
) {
    val timetable by viewModel.currentTimetable.collectAsStateWithLifecycle()
    val advancedEnabled by viewModel.advancedFeaturesEnabled.collectAsStateWithLifecycle()
    val aiImportEnabled by viewModel.aiImportEnabled.collectAsStateWithLifecycle()
    val importExportState by importExportViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showManagementDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) importExportViewModel.importFromUri(context.contentResolver, uri)
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val currentId = timetable?.id
        if (uri != null && currentId != null) {
            importExportViewModel.exportToUri(context.contentResolver, uri, currentId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = PageBackground,
            // 嵌套在 HomeScreen 的 Scaffold 内容区内，状态栏 inset 已由外层提供，这里禁用避免双重 padding
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                YIClassAppBar(
                    title = "设置",
                    centerTitle = true,
                    showBack = true,
                    onBack = onBack
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PageBackground)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SettingsBlock(title = "基础功能") {
                        SettingsItem(
                            title = "开始上课日期",
                            value = timetable?.settings?.startDate?.format(dateFormatter) ?: "未设置",
                            showArrow = true,
                            enabled = timetable != null,
                            onClick = { showDatePicker = true }
                        )
                        SettingsSwitchItem(
                            title = "显示周末",
                            description = "在课程表中显示周六和周日",
                            checked = timetable?.settings?.showWeekend ?: false,
                            onCheckedChange = viewModel::setShowWeekend,
                            enabled = timetable != null
                        )
                        SettingsItem(
                            title = "上课时间",
                            description = "总周数、每节课的开始和结束时间",
                            showArrow = true,
                            enabled = timetable != null,
                            onClick = { timetable?.let { onNavigate(AppRoute.TimeSettings(it.id)) } }
                        )
                        SettingsItem(
                            title = "节假日与补课",
                            description = "设置停课日期或调整当天的课程",
                            showArrow = true,
                            enabled = timetable != null,
                            onClick = { timetable?.let { onNavigate(AppRoute.AcademicCalendar(it.id)) } }
                        )
                    }
                }

                item {
                    SettingsBlock(title = "课表管理") {
                        SettingsItem(
                            title = "课表管理",
                            description = "创建、切换、删除课表",
                            value = timetable?.name ?: "无",
                            showArrow = true,
                            onClick = { showManagementDialog = true }
                        )
                        SettingsItem(
                            title = "导入课表",
                            description = "从 JSON 文件导入课表数据",
                            showArrow = true,
                            onClick = { importLauncher.launch(arrayOf("application/json")) }
                        )
                        SettingsItem(
                            title = "导出课表",
                            description = "将当前课表导出为 JSON 文件",
                            showArrow = true,
                            enabled = timetable != null,
                            onClick = {
                                val name = timetable?.name ?: "Timetable"
                                exportLauncher.launch("YIClass_$name.json")
                            }
                        )
                        if (advancedEnabled) {
                            SettingsItem(
                                title = "教务导入",
                                description = "通过脚本从教务系统获取课表",
                                showArrow = true,
                                onClick = { onNavigate(AppRoute.SchoolSelect) }
                            )
                            if (aiImportEnabled) {
                                SettingsItem(
                                    title = "AI 导入",
                                    description = "通过已启用的方式创建课表",
                                    showArrow = true,
                                    onClick = { onNavigate(AppRoute.AiImport) }
                                )
                            }
                        }
                    }
                }

                item {
                    SettingsBlock(title = "扩展功能") {
                        SettingsItem(
                            title = "主题设置",
                            description = "自定义应用外观和颜色",
                            showArrow = true,
                            onClick = { onNavigate(AppRoute.ThemeSettings) }
                        )
                        SettingsItem(
                            title = "课程提醒",
                            description = "课程提醒和通知管理",
                            showArrow = true,
                            onClick = { onNavigate(AppRoute.CourseReminder) }
                        )
                    }
                }

                item {
                    SettingsBlock(title = "高级功能") {
                        SettingsSwitchItem(
                            title = "启用高级功能",
                            description = if (advancedEnabled) "已启用教务导入等功能" else "启用后可使用教务导入等功能",
                            checked = advancedEnabled,
                            onCheckedChange = viewModel::setAdvancedFeaturesEnabled
                        )
                        if (advancedEnabled) {
                            SettingsItem(
                                title = "教务仓库管理",
                                description = "配置教务导入脚本仓库",
                                showArrow = true,
                                onClick = { onNavigate(AppRoute.RepositoryConfig) }
                            )
                            SettingsItem(
                                title = "AI 导入配置",
                                description = "设置接口、模型和导入方式",
                                showArrow = true,
                                onClick = { onNavigate(AppRoute.AiConfig) }
                            )
                        }
                    }
                }

                item {
                    SettingsBlock(title = "关于与帮助") {
                        SettingsItem(title = "使用帮助", showArrow = true, onClick = { onNavigate(AppRoute.Help) })
                        SettingsItem(
                            title = "关于应用",
                            description = "版本信息和应用详情",
                            value = "v${BuildConfig.VERSION_NAME}",
                            showArrow = true,
                            onClick = { onNavigate(AppRoute.About) }
                        )
                    }
                }
            }
        }

        SonnerHost(
            message = importExportState.message,
            onDismiss = importExportViewModel::clearMessage,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showManagementDialog) {
        TimetableManagementDialog(onDismiss = { showManagementDialog = false })
    }

    if (showDatePicker) {
        AppDatePicker(
            initialDate = timetable?.settings?.startDate ?: LocalDate.now(),
            onConfirm = { date ->
                viewModel.setStartDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

}
