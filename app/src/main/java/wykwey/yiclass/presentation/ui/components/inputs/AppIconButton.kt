package wykwey.yiclass.presentation.ui.components.inputs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 通用图标按钮，对齐原 Flutter 版 YicoreIconButton：
 * 透明背景 + 可选圆角描边方块，按下轻微缩放，禁用态图标变浅。
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    tint: Color = LocalAppColors.current.textPrimary,
    showBorder: Boolean = true,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed && enabled) 0.95f else 1f)
    val alpha = if (enabled) 0.85f else 0.3f

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .then(
                if (showBorder) Modifier.border(
                    1.dp,
                    tint.copy(alpha = if (enabled) 0.4f else 0.2f),
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint.copy(alpha = alpha),
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
