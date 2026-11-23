package wykwey.yiclass.presentation.ui.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
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
import wykwey.yiclass.domain.model.RepositoryType
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppButtonVariant
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.inputs.AppTextField
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.viewmodel.RepositoryConfigViewModel
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar

private val REPOSITORY_TYPES = listOf(
    RepositoryType.OFFICIAL to "官方",
    RepositoryType.CUSTOM to "自定义",
    RepositoryType.PRIVATE to "私有"
)

/**
 * 教务仓库管理页，对齐原版 repository_config_page.dart：
 * 仓库类型三选一 + 仓库 URL/分支/Token 表单 + 下载索引/完整仓库按钮。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryConfigScreen(
    onBack: () -> Unit,
    viewModel: RepositoryConfigViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colors.pageBackground,
            topBar = {
                YIClassAppBar(
                    title = "教务仓库管理",
                    centerTitle = true,
                    showBack = true,
                    onBack = onBack,
                    actions = {
                        AppIconButton(
                            icon = Icons.Filled.Save,
                            contentDescription = "保存",
                            showBorder = false,
                            enabled = !state.loading && !state.saving,
                            onClick = viewModel::save
                        )
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.pageBackground)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // —— 基础配置 ——
                item {
                    SettingsBlock(title = "基础配置") {
                        RepositoryTypeSelector(
                            selected = state.repositoryType,
                            onSelected = viewModel::setRepositoryType,
                            enabled = !state.loading
                        )
                        if (state.repositoryType != RepositoryType.OFFICIAL) {
                            AppTextField(
                                value = state.repositoryUrl,
                                onValueChange = viewModel::setRepositoryUrl,
                                label = "GitHub仓库URL",
                                placeholder = "https://github.com/xxx/xxx",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                            AppTextField(
                                value = state.scriptsBranch,
                                onValueChange = viewModel::setScriptsBranch,
                                label = "脚本分支名称",
                                placeholder = "如: main, master",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                        if (state.repositoryType == RepositoryType.PRIVATE) {
                            AppTextField(
                                value = state.tokenKey,
                                onValueChange = viewModel::setTokenKey,
                                label = "Token Key",
                                placeholder = "输入Token Key",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                            AppTextField(
                                value = state.tokenValue,
                                onValueChange = viewModel::setTokenValue,
                                label = "Token Value",
                                placeholder = "输入Token Value",
                                singleLine = true,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // —— 操作按钮 ——
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppButton(
                            text = "下载索引",
                            onClick = viewModel::downloadIndex,
                            variant = AppButtonVariant.Primary,
                            modifier = Modifier.weight(1f)
                        )
                        AppButton(
                            text = "下载完整仓库",
                            onClick = viewModel::downloadFullRepository,
                            variant = AppButtonVariant.Outlined,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // —— 删除操作 ——
                item {
                    SettingsBlock(title = "危险操作") {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AppButton(
                                text = "删除索引文件",
                                onClick = viewModel::deleteIndex,
                                variant = AppButtonVariant.Outlined,
                                modifier = Modifier.fillMaxWidth()
                            )
                            AppButton(
                                text = "删除全部已下载脚本",
                                onClick = viewModel::deleteAllScripts,
                                variant = AppButtonVariant.Outlined,
                                modifier = Modifier.fillMaxWidth()
                            )
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

/** 仓库类型三选一，对齐原版 YicoreSegmentedControl 的官方/自定义/私有。 */
@Composable
private fun RepositoryTypeSelector(
    selected: RepositoryType,
    onSelected: (RepositoryType) -> Unit,
    enabled: Boolean
) {
    val colors = LocalAppColors.current
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            text = "仓库类型",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary.copy(alpha = 0.85f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            REPOSITORY_TYPES.forEach { (type, label) ->
                val isSelected = type == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) colors.textPrimary else colors.surfaceCard,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.textPrimary else colors.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = enabled) { onSelected(type) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) colors.textInverse else colors.textPrimary
                    )
                }
            }
        }
    }
}
