package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 通用白色卡片，对齐原 Flutter 版 YicoreCard：白底 + 圆角 + 浅阴影。
 * 页面背景通常是浅灰，卡片本身固定白色，不随主题/动态取色变化。
 * [SectionCard] 等带标题的分区卡片均基于此实现，不单独重写卡片样式。
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(contentPadding)) { content() }
    }
}

/** 带标题的白色卡片，用于表单分区（基本信息、时间安排等）。 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AppCard(
        modifier = modifier.padding(vertical = 8.dp),
        contentPadding = 16.dp
    ) {
        Column {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.padding(top = 12.dp)) { content() }
        }
    }
}
