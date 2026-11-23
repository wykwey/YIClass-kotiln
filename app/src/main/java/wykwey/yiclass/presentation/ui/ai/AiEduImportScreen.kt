package wykwey.yiclass.presentation.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar
import wykwey.yiclass.presentation.viewmodel.AiImportViewModel
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.web.rememberEduWebView

/**
 * AI 教务导入页，对齐原版 ai_edu_import_page.dart：
 * 地址栏 + WebView 浏览教务网站，点击下载按钮提取页面表格文本交给 AI 表格解析。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiEduImportScreen(
    onBack: () -> Unit,
    initialUrl: String = "",
    viewModel: AiImportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var urlText by remember { mutableStateOf(initialUrl) }
    var isDesktopMode by remember { mutableStateOf(false) }

    val controller = rememberEduWebView(initialUrl = initialUrl)
    val currentUrlState by controller.currentUrl.collectAsStateWithLifecycle()

    LaunchedEffect(currentUrlState) {
        if (currentUrlState != "about:blank") urlText = currentUrlState
    }

    LaunchedEffect(state.done) {
        if (state.done) onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                YIClassAppBar(
                    showBack = true,
                    onBack = onBack,
                    titleContent = {
                        TextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            placeholder = { Text("请输入教务网址", fontSize = 14.sp) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Go
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onGo = { controller.loadUrl(urlText) }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    actions = {
                        AppIconButton(
                            icon = Icons.Filled.Download,
                            contentDescription = "分析",
                            showBorder = false,
                            enabled = !state.analyzing,
                            onClick = { controller.extractPageText(viewModel::importWebContent) }
                        )
                        AppIconButton(
                            icon = if (isDesktopMode) Icons.Filled.PhoneAndroid else Icons.Filled.Computer,
                            contentDescription = "切换UA",
                            showBorder = false,
                            onClick = {
                                isDesktopMode = !isDesktopMode
                                controller.setDesktopMode(isDesktopMode)
                            }
                        )
                        AppIconButton(
                            icon = Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            showBorder = false,
                            onClick = { controller.reload() }
                        )
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalAppColors.current.pageBackground)
                    .padding(innerPadding)
            ) {
                AndroidView(
                    factory = { controller.webView },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { it.destroy() }
                )
                if (currentUrlState == "about:blank") {
                    Box(Modifier.fillMaxSize().background(LocalAppColors.current.pageBackground), contentAlignment = Alignment.Center) {
                        Text("请在上方输入教务网址", color = LocalAppColors.current.textSecondary)
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
