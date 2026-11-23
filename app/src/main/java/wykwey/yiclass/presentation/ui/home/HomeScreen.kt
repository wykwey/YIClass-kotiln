package wykwey.yiclass.presentation.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.presentation.navigation.AppRoute
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.layout.BottomNavItem
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar
import wykwey.yiclass.presentation.ui.components.layout.YIClassBottomNavBar
import wykwey.yiclass.presentation.ui.home.components.CourseListView
import wykwey.yiclass.presentation.ui.home.components.DayView
import wykwey.yiclass.presentation.ui.home.components.WeekView
import wykwey.yiclass.presentation.ui.settings.SettingsScreen
import wykwey.yiclass.presentation.viewmodel.HomeViewModel
import wykwey.yiclass.presentation.viewmodel.ViewMode
import wykwey.yiclass.presentation.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (AppRoute) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDate() }
    val showSettings = state.viewMode == ViewMode.SETTINGS
    val pageBackground = when {
        showSettings -> LocalAppColors.current.pageBackground
        state.viewMode == ViewMode.WEEK -> LocalAppColors.current.surfaceCard
        else -> LocalAppColors.current.pageBackground
    }

    Scaffold(
        containerColor = pageBackground,
        // 设置 tab 时外层让出状态栏空间，由设置页顶栏自己处理（与其他 push 页面行为一致）
        contentWindowInsets = if (showSettings) WindowInsets(0.dp) else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (!showSettings) {
                YIClassAppBar(
                    centerTitle = true,
                    // 日视图/列表视图显示左箭头，统一跳转周视图（周视图自身保留上一周切换）
                    showBack = state.viewMode != ViewMode.WEEK,
                    onBack = { viewModel.switchView(ViewMode.WEEK) },
                    titleContent = {
                        if (state.viewMode == ViewMode.WEEK && state.currentTimetable != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIconButton(
                                    icon = Icons.Filled.ChevronLeft,
                                    contentDescription = "上一周",
                                    showBorder = false,
                                    enabled = state.currentWeek > 1,
                                    onClick = { viewModel.jumpToWeek((state.currentWeek - 1).coerceAtLeast(1)) }
                                )
                                Text(
                                    text = "第 ${state.currentWeek} 周",
                                    fontWeight = FontWeight.SemiBold
                                )
                                AppIconButton(
                                    icon = Icons.Filled.ChevronRight,
                                    contentDescription = "下一周",
                                    showBorder = false,
                                    enabled = state.currentWeek < state.totalWeeks,
                                    onClick = { viewModel.jumpToWeek((state.currentWeek + 1).coerceAtMost(state.totalWeeks)) }
                                )
                            }
                        } else {
                            Text(text = "YIClass", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    actions = {
                        AppIconButton(
                            icon = Icons.AutoMirrored.Filled.List,
                            contentDescription = "列表视图",
                            showBorder = false,
                            onClick = { viewModel.switchView(ViewMode.LIST) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
            }
        },
        bottomBar = {
            YIClassBottomNavBar(
                items = listOf(
                    BottomNavItem(Icons.Filled.CalendarViewWeek, "周视图"),
                    BottomNavItem(Icons.Filled.CalendarToday, "日视图"),
                    BottomNavItem(Icons.Filled.Settings, "设置")
                ),
                selectedIndex = when {
                    showSettings -> 2
                    state.viewMode == ViewMode.DAY -> 1
                    else -> 0
                },
                onItemSelected = { index ->
                    viewModel.switchView(when (index) {
                        0 -> ViewMode.WEEK
                        1 -> ViewMode.DAY
                        else -> ViewMode.SETTINGS
                    })
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val timetable = state.currentTimetable
            when {
                showSettings -> SettingsScreen(
                    onBack = { viewModel.switchView(ViewMode.WEEK) },
                    onNavigate = onNavigate
                )
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("加载中…")
                }
                timetable == null -> EmptyState(onManage = { viewModel.switchView(ViewMode.SETTINGS) })
                state.viewMode == ViewMode.WEEK -> WeekView(
                    timetable = timetable,
                    currentWeek = state.currentWeek,
                    modifier = Modifier.fillMaxSize(),
                    onCourseClick = { onNavigate(AppRoute.CourseEdit(timetable.id, it)) },
                    onEmptyClick = { weekday, period ->
                        onNavigate(AppRoute.CourseEdit(timetable.id, weekday = weekday, period = period, week = state.currentWeek))
                    }
                )
                state.viewMode == ViewMode.DAY -> DayView(
                    timetable = timetable,
                    todayWeek = state.todayWeek,
                    selectedDay = state.selectedDay,
                    onDaySelected = viewModel::selectDay,
                    modifier = Modifier.fillMaxSize(),
                    onCourseClick = { onNavigate(AppRoute.CourseEdit(timetable.id, it)) }
                )
                else -> CourseListView(
                    timetable = timetable,
                    modifier = Modifier.fillMaxSize(),
                    onCourseClick = { onNavigate(AppRoute.CourseEdit(timetable.id, it)) }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onManage: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
        Text("暂无课表")
        AppButton(text = "管理课表", onClick = onManage)
    }
}
