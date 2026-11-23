package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 单元格右、下描边，拼成网格线。颜色由调用方从 [wykwey.yiclass.presentation.theme.LocalAppColors] 传入。 */
fun Modifier.drawGridBorder(borderColor: Color): Modifier = this.drawBehind {
    val stroke = 1.dp.toPx()
    drawLine(
        color = borderColor,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = stroke
    )
    drawLine(
        color = borderColor,
        start = Offset(size.width, 0f),
        end = Offset(size.width, size.height),
        strokeWidth = stroke
    )
}

/** 仅底边描边（表头下方分隔线）。 */
fun Modifier.drawBottomBorder(borderColor: Color): Modifier = this.drawBehind {
    val stroke = 1.dp.toPx()
    drawLine(
        color = borderColor,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = stroke
    )
}

/** 仅右边描边（节次时间轴右侧竖线）。 */
fun Modifier.drawRightBorder(borderColor: Color): Modifier = this.drawBehind {
    val stroke = 1.dp.toPx()
    drawLine(
        color = borderColor,
        start = Offset(size.width, 0f),
        end = Offset(size.width, size.height),
        strokeWidth = stroke
    )
}

/** 无水波纹点击，用于空格占位。 */
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = rememberMutableInteractionSource(),
        indication = null,
        onClick = onClick
    )
}

@Composable
private fun rememberMutableInteractionSource(): MutableInteractionSource =
    remember { MutableInteractionSource() }
