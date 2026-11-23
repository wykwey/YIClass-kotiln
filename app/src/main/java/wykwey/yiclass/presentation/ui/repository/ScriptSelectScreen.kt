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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.domain.model.ScriptEntry
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar
import wykwey.yiclass.presentation.viewmodel.ScriptSelectViewModel

/**
 * 脚本选择页，对齐原版 script_select_page.dart：
 * 展示某学校的全部脚本，未下载置灰+下载按钮，已下载可点击进入教务导入页。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptSelectScreen(
    onBack: () -> Unit,
    onSelectScript: (ScriptEntry) -> Unit,
    viewModel: ScriptSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colors.pageBackground,
            topBar = {
                YIClassAppBar(
                    title = state.school?.school ?: "脚本选择",
                    centerTitle = true,
                    showBack = true,
                    onBack = onBack,
                    actions = {
                        AppIconButton(
                            icon = Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            showBorder = false,
                            enabled = !state.downloading,
                            onClick = viewModel::refresh
                        )
                        AppIconButton(
                            icon = Icons.Filled.Download,
                            contentDescription = "下载全部",
                            showBorder = false,
                            enabled = !state.downloading,
                            onClick = viewModel::downloadAllScripts
                        )
                    }
                )
            }
        ) { innerPadding ->
            val scripts = state.school?.scripts ?: emptyList()
            Box(
                modifier = Modifier.fillMaxSize().background(colors.pageBackground).padding(innerPadding)
            ) {
                if (scripts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("该学校暂无可用脚本", color = colors.textSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        items(scripts) { script ->
                            val isDownloaded = state.scriptExistsMap[script.scriptName] ?: false
                            ScriptCard(
                                script = script,
                                isDownloaded = isDownloaded,
                                onClick = { if (isDownloaded) onSelectScript(script) },
                                onDownload = { viewModel.downloadScript(script.scriptName) },
                                onDelete = { viewModel.deleteScript(script.scriptName) }
                            )
                        }
                    }
                }

                if (state.downloading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
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
private fun ScriptCard(
    script: ScriptEntry,
    isDownloaded: Boolean,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isDownloaded) 1f else 0.5f)
            .then(if (isDownloaded) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = script.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDownloaded) colors.textPrimary else colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                if (isDownloaded) {
                    AppIconButton(
                        icon = Icons.Filled.Delete,
                        contentDescription = "删除",
                        showBorder = true,
                        onClick = onDelete
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                }
                AppIconButton(
                    icon = if (isDownloaded) Icons.Filled.CheckCircle else Icons.Filled.Download,
                    contentDescription = if (isDownloaded) "已下载" else "下载",
                    showBorder = true,
                    enabled = !isDownloaded,
                    onClick = onDownload
                )
            }
            if (script.description.isNotEmpty()) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    script.description,
                    fontSize = 14.sp,
                    color = if (isDownloaded) colors.textPrimary.copy(alpha = 0.85f) else colors.textSecondary,
                    maxLines = 2
                )
            }
            if (script.author.isNotEmpty()) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "贡献者: ${script.author}",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}
