package wykwey.yiclass.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 课程卡片配色，对齐原 Flutter 版 color_utils.dart 的柔和色卡 + 深色文字。
 */
object CourseColors {

    /** 柔和背景色卡（与原版一致顺序）。 */
    val palette: List<Color> = listOf(
        Color(0xFFBBDEFB), // 柔和蓝
        Color(0xFFC8E6C9), // 柔和绿
        Color(0xFFFFF9C4), // 柔和黄
        Color(0xFFC5CAE9), // 柔和靛
        Color(0xFFF8BBD0), // 柔和粉
        Color(0xFFB2EBF2), // 柔和青
        Color(0xFFFFCCBC), // 柔和橙
        Color(0xFFE1BEE7), // 柔和紫
        Color(0xFFF0F4C3), // 柔和柠檬
        Color(0xFFB2DFDB), // 柔和蓝绿
    )

    /** 预设背景色 → 深色文字色（对齐原版 textColorMap）。 */
    private val textForBackground: Map<Color, Color> = mapOf(
        Color(0xFFBBDEFB) to Color(0xFF0D47A1),
        Color(0xFFC8E6C9) to Color(0xFF1B5E20),
        Color(0xFFFFF9C4) to Color(0xFFE65100),
        Color(0xFFC5CAE9) to Color(0xFF1A237E),
        Color(0xFFF8BBD0) to Color(0xFF880E4F),
        Color(0xFFB2EBF2) to Color(0xFF006064),
        Color(0xFFFFCCBC) to Color(0xFFBF360C),
        Color(0xFFE1BEE7) to Color(0xFF4A148C),
        Color(0xFFF0F4C3) to Color(0xFF827717),
        Color(0xFFB2DFDB) to Color(0xFF00695C),
    )

    private val defaultTextColor = Color(0xFF333333)

    /** 依课程名散列取一个稳定的背景色（color==0 时用）。 */
    fun colorForName(name: String): Color {
        if (name.isEmpty()) return palette.first()
        val index = (name.hashCode() % palette.size + palette.size) % palette.size
        return palette[index]
    }

    /** 卡片文字色：预设背景用配对深色，否则按亮度回退。disableAdaptive=true 时关闭自适应，恒用默认深色（对齐原版 getCourseTextColor）。 */
    fun textColorFor(background: Color, disableAdaptive: Boolean = false): Color {
        if (disableAdaptive) return defaultTextColor
        textForBackground[background]?.let { return it }
        return if (background.luminance() > 0.5f) defaultTextColor else Color.White
    }
}
