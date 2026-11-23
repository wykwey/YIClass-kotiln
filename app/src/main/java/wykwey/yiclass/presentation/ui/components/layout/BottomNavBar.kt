package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.presentation.theme.LocalAppColors

data class BottomNavItem(
    val icon: ImageVector,
    val label: String
)

/**
 * 底部导航栏，对齐原 Flutter 版 YicoreBottomNavigationBar：
 * 白底 + 顶部描边，选中项图标黑色圆角方块背景 + 白色图标，未选中透明背景 + 灰色图标，
 * 文字始终黑色，选中时加粗放大，点击无水波纹。
 */
@Composable
fun YIClassBottomNavBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceCard)
            .drawBehind {
                drawLine(
                    color = colors.border,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .padding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()),
    ) {
        items.forEachIndexed { index, item ->
            NavItem(
                item = item,
                isSelected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val iconBgColor by animateColorAsState(if (isSelected) colors.textPrimary else Color.Transparent)
    val iconTintColor by animateColorAsState(if (isSelected) colors.textInverse else colors.textHint)
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .background(iconBgColor, RoundedCornerShape(8.dp))
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconTintColor,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.size(3.dp))
        Text(
            text = item.label,
            fontSize = if (isSelected) 13.sp else 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = colors.textPrimary
        )
    }
}
