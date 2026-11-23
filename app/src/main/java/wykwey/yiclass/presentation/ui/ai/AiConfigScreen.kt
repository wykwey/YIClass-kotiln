package wykwey.yiclass.presentation.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import wykwey.yiclass.domain.model.AiConfig
import wykwey.yiclass.domain.model.AiProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppButtonVariant
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.inputs.AppTextField
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.ui.components.layout.SettingsSwitchItem
import wykwey.yiclass.presentation.viewmodel.AiConfigViewModel
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar


/**
 * AI 导入设置页，对齐原版 ai_config_page.dart：
 * 基础配置（API Key/接口/模型）+ 功能开关 + 预设配置 + 测试/保存。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiConfigScreen(
    onBack: () -> Unit,
    viewModel: AiConfigViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colors.pageBackground,
            topBar = {
                YIClassAppBar(
                    title = "AI导入设置",
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
                        AppTextField(
                            value = state.apiKey,
                            onValueChange = viewModel::setApiKey,
                            label = "API Key",
                            placeholder = "输入 API Key",
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        AppTextField(
                            value = state.endpoint,
                            onValueChange = viewModel::setEndpoint,
                            label = "接口地址",
                            placeholder = "输入API接口地址",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        AppTextField(
                            value = state.visionModel,
                            onValueChange = viewModel::setVisionModel,
                            label = "视觉模型",
                            placeholder = AiConfig.DEFAULT_MODEL,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        AppTextField(
                            value = state.textModel,
                            onValueChange = viewModel::setTextModel,
                            label = "文本模型",
                            placeholder = AiConfig.DEFAULT_MODEL,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                // —— 功能开关 ——
                item {
                    SettingsBlock(title = "功能开关") {
                        SettingsSwitchItem(
                            title = "图片导入",
                            description = "支持课程表图片识别",
                            checked = state.enableImageImport,
                            onCheckedChange = viewModel::setImageImport
                        )
                        SettingsSwitchItem(
                            title = "表格导入",
                            description = "支持表格数据解析",
                            checked = state.enableTableImport,
                            onCheckedChange = viewModel::setTableImport
                        )
                        SettingsSwitchItem(
                            title = "文字导入",
                            description = "支持文字描述解析",
                            checked = state.enableTextImport,
                            onCheckedChange = viewModel::setTextImport
                        )
                        SettingsSwitchItem(
                            title = "AI 教务导入",
                            description = "支持教务网页 AI 分析导入",
                            checked = state.enableWebImport,
                            onCheckedChange = viewModel::setWebImport
                        )
                    }
                }

                // —— 预设配置 ——
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceCard, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "预设配置",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            letterSpacing = 0.5.sp
                        )
                        FlowRow(
                            modifier = Modifier.padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AiProvider.entries.forEach { preset ->
                                AppButton(
                                    text = preset.label,
                                    onClick = { viewModel.applyPreset(preset) },
                                    variant = AppButtonVariant.Outlined,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 16.dp, vertical = 10.dp
                                    )
                                )
                            }
                        }
                    }
                }

                // —— 操作按钮 ——
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppButton(
                            text = if (state.testing) "测试中..." else "测试配置",
                            onClick = viewModel::testConfig,
                            isLoading = state.testing,
                            variant = AppButtonVariant.Primary,
                            modifier = Modifier.weight(1f)
                        )
                        AppButton(
                            text = "保存配置",
                            onClick = viewModel::save,
                            isLoading = state.saving,
                            variant = AppButtonVariant.Outlined,
                            modifier = Modifier.weight(1f)
                        )
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
