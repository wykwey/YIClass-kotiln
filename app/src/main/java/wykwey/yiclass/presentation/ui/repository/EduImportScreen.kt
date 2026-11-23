package wykwey.yiclass.presentation.ui.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar
import wykwey.yiclass.presentation.viewmodel.EduImportViewModel
import wykwey.yiclass.presentation.web.rememberEduWebView

/**
 * 教务系统脚本导入页，对齐原版 edu_import_page.dart：
 * 打开脚本对应的教务网址，点击下载按钮执行本地脚本，脚本通过 YiClassChannel.postMessage 回调课表 JSON。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduImportScreen(
    onBack: () -> Unit,
    viewModel: EduImportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isDesktopMode by remember { mutableStateOf(false) }

    val controller = rememberEduWebView(initialUrl = state.scriptUrl, scriptBridge = true)

    LaunchedEffect(state.done) {
        if (state.done) onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = LocalAppColors.current.pageBackground,
            topBar = {
                YIClassAppBar(
                    title = "教务导入",
                    centerTitle = true,
                    showBack = true,
                    onBack = onBack,
                    actions = {
                        AppIconButton(
                            icon = Icons.Filled.Download,
                            contentDescription = "执行脚本",
                            showBorder = false,
                            enabled = !state.importing,
                            onClick = { viewModel.importWith(controller::runScript) }
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
                AndroidView(factory = { controller.webView }, modifier = Modifier.fillMaxSize(), onRelease = { it.destroy() })
            }
        }

        SonnerHost(
            message = state.message,
            onDismiss = viewModel::clearMessage,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
