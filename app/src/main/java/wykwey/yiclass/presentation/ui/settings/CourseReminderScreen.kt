package wykwey.yiclass.presentation.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.presentation.ui.components.inputs.AppDropdown
import wykwey.yiclass.presentation.ui.components.inputs.AppDropdownItem
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.ui.components.layout.SettingsItem
import wykwey.yiclass.presentation.ui.components.layout.SettingsSwitchItem
import wykwey.yiclass.presentation.viewmodel.CourseReminderViewModel
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar

private val PageBackground: Color
    @Composable get() = LocalAppColors.current.pageBackground

private val advanceOptions = listOf(
    AppDropdownItem(5, "5分钟"),
    AppDropdownItem(10, "10分钟"),
    AppDropdownItem(15, "15分钟"),
    AppDropdownItem(30, "30分钟")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseReminderScreen(
    onBack: () -> Unit,
    viewModel: CourseReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationManager = remember(context) { NotificationManagerCompat.from(context) }
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    var notificationsAllowed by remember { mutableStateOf(notificationManager.areNotificationsEnabled()) }
    var exactAlarmsAllowed by remember { mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsAllowed = notificationManager.areNotificationsEnabled()
        exactAlarmsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notificationsAllowed = notificationManager.areNotificationsEnabled() }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            YIClassAppBar(
                title = "课程提醒",
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
                SettingsBlock(title = "提醒设置") {
                    SettingsSwitchItem(
                        title = "课程提醒",
                        description = "在课程开始前提醒您",
                        checked = state.reminderEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setReminderEnabled(enabled)
                            if (enabled && !notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                    SettingsItem(
                        title = "提前提醒时间",
                        description = "设置课程开始前多久提醒",
                        enabled = state.reminderEnabled,
                        trailing = {
                            AppDropdown(
                                value = state.advanceMinutes,
                                items = advanceOptions,
                                onValueChange = viewModel::setAdvanceMinutes,
                                enabled = state.reminderEnabled,
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    )
                }
            }

            item {
                SettingsBlock(title = "权限设置") {
                    SettingsItem(
                        title = "通知权限",
                        value = if (notificationsAllowed) "已允许" else "未允许",
                        showArrow = true,
                        enabled = state.reminderEnabled,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                        }
                    )
                    SettingsItem(
                        title = "精确闹钟权限",
                        description = if (exactAlarmsAllowed) "已允许" else "未允许，提醒可能延迟",
                        showArrow = !exactAlarmsAllowed,
                        enabled = state.reminderEnabled,
                        onClick = if (exactAlarmsAllowed) null else ({ requestExactAlarmPermission(context) })
                    )
                }
            }

            item {
                ExplanationCard()
            }
        }
    }
}

private fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}

private data class ExplanationItem(val title: String, val description: String)

private val explanationItems = listOf(
    ExplanationItem("课程提醒", "开启后，系统会在课程开始前根据您设置的时间提前提醒您。修改当前课表或提醒设置后会自动更新。"),
    ExplanationItem("提前提醒时间", "您可以设置提前5分钟、10分钟、15分钟或30分钟提醒，确保您有足够的时间准备。"),
    ExplanationItem("连续课程", "对于连续多节的课程（如第1-3节），只会在第一节课开始前提醒一次。"),
    ExplanationItem("通知权限", "需要授予通知权限，系统才能发送课程提醒通知。"),
    ExplanationItem("精确闹钟权限", "Android 12 及以上可允许精确闹钟，让提醒准时触发。")
)

@Composable
private fun ExplanationCard() {
    AppCard {
        Column {
            Text(
                "功能说明",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalAppColors.current.textPrimary
            )
            explanationItems.forEach { item ->
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textPrimary.copy(alpha = 0.87f))
                    Text(
                        item.description,
                        fontSize = 14.sp,
                        color = LocalAppColors.current.textSecondary,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
