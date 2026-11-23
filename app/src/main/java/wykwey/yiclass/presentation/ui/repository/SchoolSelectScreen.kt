package wykwey.yiclass.presentation.ui.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.domain.model.SchoolEntry
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppButtonVariant
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.inputs.AppTextField
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar
import wykwey.yiclass.presentation.viewmodel.SchoolSelectViewModel

/**
 * 学校选择页，对齐原版 school_select_page.dart：
 * 按字母分组的学校列表 + 搜索框；索引未下载时提示前往仓库配置页下载。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolSelectScreen(
    onBack: () -> Unit,
    onNavigateToRepositoryConfig: () -> Unit,
    onSelectSchool: (SchoolEntry) -> Unit,
    viewModel: SchoolSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colors.pageBackground,
            topBar = {
                YIClassAppBar(
                    title = "选择学校",
                    centerTitle = true,
                    showBack = true,
                    onBack = onBack,
                    actions = {
                        AppIconButton(
                            icon = Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            showBorder = false,
                            enabled = !state.loading,
                            onClick = viewModel::refresh
                        )
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.pageBackground)
                    .padding(innerPadding)
            ) {
                when {
                    state.loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.indexMissing -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            Text("索引文件未找到", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "请先在\"教务仓库管理\"页面下载索引文件",
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                            AppButton(
                                text = "前往配置",
                                variant = AppButtonVariant.Outlined,
                                onClick = onNavigateToRepositoryConfig
                            )
                        }
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            AppTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchQueryChanged,
                                placeholder = "搜索学校名称",
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                            if (state.isSearching) {
                                SchoolFlatList(state.searchResults, onSelectSchool)
                            } else {
                                SchoolGroupedList(state.groupedSchools, onSelectSchool, onNavigateToRepositoryConfig)
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
}

@Composable
private fun SchoolFlatList(schools: List<SchoolEntry>, onSelectSchool: (SchoolEntry) -> Unit) {
    if (schools.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到匹配的学校", color = LocalAppColors.current.textSecondary)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(schools) { school -> SchoolTile(school, onSelectSchool) }
    }
}

@Composable
private fun SchoolGroupedList(
    grouped: Map<String, List<SchoolEntry>>,
    onSelectSchool: (SchoolEntry) -> Unit,
    onNavigateToRepositoryConfig: () -> Unit
) {
    if (grouped.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text("暂无学校数据", color = LocalAppColors.current.textSecondary)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            AppButton(text = "前往配置", variant = AppButtonVariant.Outlined, onClick = onNavigateToRepositoryConfig)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        grouped.forEach { (letter, schools) ->
            item {
                Text(
                    letter,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(schools) { school -> SchoolTile(school, onSelectSchool) }
        }
    }
}

@Composable
private fun SchoolTile(school: SchoolEntry, onSelectSchool: (SchoolEntry) -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .androidx_clickable { onSelectSchool(school) }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(school.school, fontSize = 16.sp, color = colors.textPrimary)
            Text("${school.scripts.size} 个脚本", fontSize = 13.sp, color = colors.textSecondary)
        }
    }
}

private fun Modifier.androidx_clickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))
