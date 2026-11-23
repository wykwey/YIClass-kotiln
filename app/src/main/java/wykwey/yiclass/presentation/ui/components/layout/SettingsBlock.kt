package wykwey.yiclass.presentation.ui.components.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 设置分区容器，对齐原版 SettingsBlock：白底圆角卡片 + 可选分区标题，
 * 内部条目由调用方逐个传入 [SettingsItem]。
 */
@Composable
fun SettingsBlock(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceCard)
    ) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 12.dp)
            )
        }
        content()
    }
}

/**
 * 单条设置项，对齐原版 SettingsItem：标题 + 可选描述在左，值文本/开关/箭头在右。
 * 提供 text（纯文本，可跳转）/ value（带右侧值文本）/ switch（开关）三种便捷用法。
 */
@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    value: String? = null,
    showArrow: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = LocalAppColors.current
    val pressed by interactionSource.collectIsPressedAsState()
    val pressColor by animateColorAsState(
        targetValue = if (pressed) colors.textPrimary.copy(alpha = 0.06f) else Color.Transparent,
        animationSpec = tween(110),
        label = "Settings row press"
    )
    val titleColor = if (enabled) colors.textPrimary.copy(alpha = 0.85f) else colors.textDisabled
    val descColor = if (enabled) colors.textSecondary else colors.textDisabled
    val valueColor = if (enabled) colors.textSecondary else colors.textDisabled

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(pressColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && onClick != null,
                onClick = { onClick?.invoke() }
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = titleColor)
            if (description != null) {
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = descColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        if (value != null) {
            Text(text = value, fontSize = 14.sp, color = valueColor)
            Spacer(modifier = Modifier.width(8.dp))
        }
        trailing?.invoke()
        if (showArrow) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (enabled) colors.textDisabled else colors.divider,
                modifier = Modifier.width(20.dp)
            )
        }
    }
}

/** 便捷用法：开关型设置项。 */
@Composable
fun SettingsSwitchItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val colors = LocalAppColors.current
    SettingsItem(
        title = title,
        description = description,
        enabled = enabled,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.textInverse,
                    checkedTrackColor = colors.textPrimary,
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = colors.textInverse,
                    uncheckedTrackColor = colors.textDisabled,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    )
}

/** 分隔线，用于分区内条目之间。 */
@Composable
fun SettingsDivider() {
    val colors = LocalAppColors.current
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 20.dp),
        color = colors.surfaceTint
    )
}
