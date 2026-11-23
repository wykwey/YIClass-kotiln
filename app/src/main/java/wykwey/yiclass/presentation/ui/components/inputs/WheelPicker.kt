package wykwey.yiclass.presentation.ui.components.inputs

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.presentation.theme.LocalAppColors

private val ItemHeight = 44.dp
private const val VisibleCount = 3
private const val Padding = VisibleCount / 2 // 上下各补 1 个空位，让中间项能滚到视觉中心

/**
 * 单列数字滚轮，对齐原 Flutter 版 YicoreTimeRangePicker 内部的 ListWheelScrollView：
 * 用 LazyColumn + 吸附滚动模拟，两位数补零显示，中间项加粗放大，其余项浅灰缩小。
 */
@Composable
fun WheelPicker(
    itemCount: Int,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 68.dp
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
            }?.index ?: selectedIndex
        }
    }

    LaunchedEffect(centerIndex) {
        val actual = (centerIndex - Padding).coerceIn(0, itemCount - 1)
        if (actual != selectedIndex) onSelectedChange(actual)
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .width(width)
            .height(ItemHeight * VisibleCount)
    ) {
        items(Padding) { Box(Modifier.height(ItemHeight)) }
        items(itemCount) { index ->
            val isSelected = index == (centerIndex - Padding).coerceIn(0, itemCount - 1)
            Box(
                modifier = Modifier
                    .height(ItemHeight)
                    .fillParentMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString().padStart(2, '0'),
                    fontSize = if (isSelected) 22.sp else 17.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) LocalAppColors.current.textPrimary else LocalAppColors.current.textDisabled
                )
            }
        }
        items(Padding) { Box(Modifier.height(ItemHeight)) }
    }
}
