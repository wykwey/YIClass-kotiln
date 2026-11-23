package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton

/**
 * 通用顶栏，对齐原版 Flutter YicoreAppBar：
 * 白底、标题（字符串或自定义内容）、可选返回按钮、可选右侧操作按钮。
 *
 * [centerTitle] = true 时标题居中（[CenterAlignedTopAppBar]），否则左对齐。
 * 嵌套场景（如设置页作为底部 tab 内联渲染）需传 [windowInsets] = [WindowInsets](0.dp)，
 * 避免与外层 Scaffold 的状态栏 inset 叠加导致双重 padding。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YIClassAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleContent: @Composable () -> Unit = { Text("") },
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = LocalAppColors.current.surfaceCard,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    centerTitle: Boolean = false
) {
    val titleSlot: @Composable () -> Unit = {
        if (title != null) Text(title) else titleContent()
    }
    val navigationIconSlot: @Composable () -> Unit = {
        if (showBack && onBack != null) {
            AppIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                showBorder = false,
                onClick = onBack
            )
        }
    }
    val colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
    if (centerTitle) {
        CenterAlignedTopAppBar(
            colors = colors,
            windowInsets = windowInsets,
            title = titleSlot,
            navigationIcon = navigationIconSlot,
            actions = { actions() },
            modifier = modifier
        )
    } else {
        TopAppBar(
            colors = colors,
            windowInsets = windowInsets,
            title = titleSlot,
            navigationIcon = navigationIconSlot,
            actions = { actions() },
            modifier = modifier
        )
    }
}
