package wykwey.yiclass.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.domain.repository.ThemeMode
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.ui.components.layout.SettingsItem
import wykwey.yiclass.presentation.ui.components.layout.SettingsSwitchItem
import wykwey.yiclass.presentation.viewmodel.ThemeSettingsViewModel
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar

private val PageBackground: Color
    @Composable get() = LocalAppColors.current.pageBackground

private data class ThemeOption(val mode: ThemeMode, val title: String, val description: String)

private val themeOptions = listOf(
    ThemeOption(ThemeMode.SYSTEM, "跟随系统", "自动跟随系统深浅色设置"),
    ThemeOption(ThemeMode.LIGHT, "浅色模式", "始终使用浅色主题"),
    ThemeOption(ThemeMode.DARK, "深色模式", "始终使用深色主题")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            YIClassAppBar(
                title = "主题设置",
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
                SettingsBlock(title = "主题模式") {
                    themeOptions.forEach { option ->
                        SettingsItem(
                            title = option.title,
                            description = option.description,
                            onClick = { viewModel.setThemeMode(option.mode) },
                            trailing = {
                                if (state.themeMode == option.mode) {
                                    Icon(Icons.Filled.Check, contentDescription = "当前主题", tint = LocalAppColors.current.textPrimary)
                                }
                            }
                        )
                    }
                }
            }

            item {
                SettingsBlock(title = "课程显示") {
                    SettingsSwitchItem(
                        title = "关闭字体自适应颜色",
                        description = "关闭后课程卡片文字将使用固定颜色",
                        checked = state.disableAdaptiveFontColor,
                        onCheckedChange = viewModel::setDisableAdaptiveFontColor
                    )
                }
            }
        }
    }
}
