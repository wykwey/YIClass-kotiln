package wykwey.yiclass.presentation.ui.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppButtonVariant
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar
import wykwey.yiclass.presentation.viewmodel.AiImportViewModel

/**
 * AI 智能导入页，对齐原版 ai_import_page.dart：
 * 图片/表格/文字/AI 教务导入 四种方式的入口列表 + 隐私提示。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiImportScreen(
    onBack: () -> Unit,
    onNavigateToAiEduImport: () -> Unit,
    viewModel: AiImportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current
    val context = LocalContext.current

    var showTableDialog by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.importImage(context.contentResolver, it) }
    }

    LaunchedEffect(state.done) {
        if (state.done) onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colors.pageBackground,
            topBar = {
                YIClassAppBar(
                    title = "AI智能导入",
                    centerTitle = true,
                    showBack = true,
                    onBack = onBack
                )
            }
        ) { innerPadding ->
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.pageBackground)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (state.config.hasImportOptions) {
                        Text(
                            text = "选择导入方式",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Column(
                            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (state.config.enableImageImport) {
                                ImportOption("图片导入", "上传课程表图片，AI 自动识别课程信息") {
                                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            }
                            if (state.config.enableTableImport) {
                                ImportOption("表格/CSV 导入", "粘贴表格或 CSV 格式的课程信息") { showTableDialog = true }
                            }
                            if (state.config.enableTextImport) {
                                ImportOption("文字导入", "输入课程描述，AI 解析课程安排") { showTextDialog = true }
                            }
                            if (state.config.enableWebImport) {
                                ImportOption("AI 教务导入", "打开教务网页，AI 分析课表内容", onNavigateToAiEduImport)
                            }
                        }
                        PrivacyNotice()
                    } else {
                        Text("请先在设置中启用 AI 导入方式", color = colors.textSecondary)
                    }
                }
            }
        }

        if (state.analyzing) {
            AnalyzingOverlay()
        }

        SonnerHost(
            message = state.message,
            onDismiss = viewModel::clearMessage,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showTableDialog) {
        TextInputDialog(
            title = "输入表格数据",
            description = "请粘贴表格数据或CSV格式的课程信息：",
            hint = "课程名称,时间,地点,教师\n高等数学,周一1-2节,教学楼A101,张老师",
            onConfirm = { text -> showTableDialog = false; if (text.isNotBlank()) viewModel.importTable(text) },
            onDismiss = { showTableDialog = false }
        )
    }

    if (showTextDialog) {
        TextInputDialog(
            title = "输入课程描述",
            description = "",
            hint = "例如：\n周一上午有高等数学课，在A101教室，老师是张老师\n周二下午有英语课，在B201教室，老师是李老师",
            onConfirm = { text -> showTextDialog = false; if (text.isNotBlank()) viewModel.importText(text) },
            onDismiss = { showTextDialog = false }
        )
    }
}

@Composable
private fun ImportOption(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceCard, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun PrivacyNotice() {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.dangerSurface, shape)
            .border(1.dp, colors.dangerBorder, shape)
            .padding(16.dp)
    ) {
        Text(
            text = "隐私注意事项",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.danger
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "上传的图片和数据将发送到您配置的AI服务进行分析，请确保不包含敏感个人信息\n" +
                "建议在上传前检查图片内容，避免泄露个人隐私\n" +
                "应用不会存储您上传的数据，但请确认您使用的AI服务提供商的隐私政策\n" +
                "如发现隐私泄露风险，请立即停止使用并检查AI服务配置",
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = colors.dangerSoft
        )
    }
}

@Composable
private fun AnalyzingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
                Text("正在分析...")
            }
        }
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    description: String,
    hint: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val colors = LocalAppColors.current
    Dialog(onDismissRequest = onDismiss) {
        AppCard {
            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                if (description.isNotEmpty()) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    Text(text = description, fontSize = 14.sp, color = colors.textSecondary)
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    placeholder = { Text(hint, fontSize = 13.sp) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppButton(text = "取消", variant = AppButtonVariant.Outlined, onClick = onDismiss)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    AppButton(text = "确定", variant = AppButtonVariant.Primary, onClick = { onConfirm(text) })
                }
            }
        }
    }
}
