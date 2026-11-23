package wykwey.yiclass.presentation.ui.components.inputs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.presentation.theme.LocalAppColors

/** 按钮外观变体，对齐原版 YicoreButton 的 primary/outlined/secondary。 */
enum class AppButtonVariant { Primary, Outlined, Secondary }

/**
 * 通用按钮，对齐原 Flutter 版 YicoreButton：黑色主按钮 / 描边按钮 / 灰色次要按钮，
 * 按下时轻微缩放，加载态显示转圈并禁用点击。
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
) {
    val colors = LocalAppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed && enabled && !isLoading) 0.95f else 1f)

    val disabledOrLoading = !enabled || isLoading
    val (bgColor, textColor, borderColor) = when {
        isLoading -> Triple(colors.divider, colors.textHint, Color.Transparent)
        variant == AppButtonVariant.Outlined ->
            Triple(Color.Transparent, colors.textPrimary.copy(alpha = 0.85f), colors.textPrimary.copy(alpha = 0.4f))
        variant == AppButtonVariant.Secondary ->
            Triple(colors.border, colors.textPrimary.copy(alpha = 0.85f), Color.Transparent)
        else -> Triple(colors.textPrimary.copy(alpha = 0.85f), colors.textInverse, Color.Transparent)
    }
    val effectiveTextColor = if (disabledOrLoading) textColor.copy(alpha = 0.6f) else textColor

    Row(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = effectiveTextColor
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = effectiveTextColor
        )
    }
}
