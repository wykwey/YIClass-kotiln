package wykwey.yiclass.presentation.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import wykwey.yiclass.presentation.theme.LocalAppColors

private val TrackHeight = 8.dp
private val BallSize = 20.dp

/**
 * 通用滑块，对齐原 Flutter 版 YicoreSlider：黑色轨道背景灰色底 + 黑色进度条 + 白色圆球（带阴影），
 * 拖动手势自行实现，不使用 Material3 默认紫色系主题滑块。
 */
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier,
    width: Dp = 180.dp
) {
    val min = valueRange.start
    val max = valueRange.endInclusive

    Box(
        modifier = modifier
            .width(width)
            .height(BallSize)
            .pointerInput(min, max, width) {
                detectHorizontalDragGestures { change, _ ->
                    val totalWidthPx = width.toPx()
                    val ballSizePx = BallSize.toPx()
                    val localX = change.position.x.coerceIn(ballSizePx / 2, totalWidthPx - ballSizePx / 2)
                    val newValue = ((localX - ballSizePx / 2) / (totalWidthPx - ballSizePx)) * (max - min) + min
                    onValueChange(newValue.coerceIn(min, max))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val normalized = if (max > min) ((value - min) / (max - min)).coerceIn(0f, 1f) else 0f

        // 背景轨道（浅灰）
        Box(
            modifier = Modifier
                .width(width)
                .height(TrackHeight)
                .background(LocalAppColors.current.border, RoundedCornerShape(TrackHeight / 2))
        )
        // 黑色进度条：宽度到圆球中心
        val ballCenterFraction = normalized * (width.value - BallSize.value) + BallSize.value / 2
        Box(
            modifier = Modifier
                .width(ballCenterFraction.dp)
                .height(TrackHeight)
                .background(LocalAppColors.current.textPrimary, RoundedCornerShape(TrackHeight / 2))
        )
        // 滑块小球
        Box(
            modifier = Modifier
                .offset(x = (normalized * (width.value - BallSize.value)).dp)
                .size(BallSize)
                .shadow(2.dp, CircleShape)
                .background(LocalAppColors.current.surfaceCard, CircleShape)
        )
    }
}
