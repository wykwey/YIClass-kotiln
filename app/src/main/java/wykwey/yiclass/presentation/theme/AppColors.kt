package wykwey.yiclass.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 应用语义化颜色集：页面结构 / 文字 / 边框 / 警示。
 * 浅色值对齐原版 Flutter 的灰底白卡视觉；深色值为 Material 深色惯例。
 * 使用时从 [LocalAppColors] 读取，禁止在组件里继续硬编码颜色。
 */
data class AppColors(
    // —— 页面结构 ——
    /** 页面底色（原版 #F7F7F7） */
    val pageBackground: Color = Color(0xFFF7F7F7),
    /** 卡片白底 */
    val surfaceCard: Color = Color(0xFFFFFFFF),
    /** 卡片内次级区块（时间段卡片内部，原版 #FAFAFA） */
    val surfaceCardSub: Color = Color(0xFFFAFAFA),
    /** 浅灰底（选中/悬浮等，原版 #F5F5F5） */
    val surfaceTint: Color = Color(0xFFF5F5F5),
    /** 卡片/输入框细边框（原版 #EEEEEE） */
    val border: Color = Color(0xFFEEEEEE),
    /** 分隔线（原版 #E0E0E0） */
    val divider: Color = Color(0xFFE0E0E0),
    /** 描边/未选中态（原版 #BDBDBD） */
    val outline: Color = Color(0xFFBDBDBD),
    /** 强描边（原版 #D1D1D1） */
    val outlineStrong: Color = Color(0xFFD1D1D1),
    // —— 文字 ——
    /** 主文字（近黑） */
    val textPrimary: Color = Color(0xFF212121),
    /** 默认文字（原版 #333333） */
    val textDefault: Color = Color(0xFF333333),
    /** 次级文字（原版 #616161） */
    val textSecondary: Color = Color(0xFF616161),
    /** 提示/弱化文字（原版 #757575） */
    val textHint: Color = Color(0xFF757575),
    /** 禁用文字（原版 #9E9E9E） */
    val textDisabled: Color = Color(0xFF9E9E9E),
    /** 反色文字（深色底上的白色） */
    val textInverse: Color = Color(0xFFFFFFFF),
    /** 强调色（周几/时间紫，原版 #673AB7） */
    val accent: Color = Color(0xFF673AB7),
    // —— 警示 ——
    /** 冲突/错误红（原版 #C62828） */
    val danger: Color = Color(0xFFC62828),
    /** 次警示红（原版 #D32F2F） */
    val dangerSoft: Color = Color(0xFFD32F2F),
    /** 亮警示红（原版 #EF5350） */
    val dangerLight: Color = Color(0xFFEF5350),
    val dangerSurface: Color = Color(0xFFFFEBEE),
    val dangerBorder: Color = Color(0xFFEF9A9A),
    /** 成功绿（当前课表标记等，原版 #2E7D32） */
    val success: Color = Color(0xFF2E7D32),
)

/** 浅色主题（对齐原版灰底白卡）。 */
val LightAppColors = AppColors()

/** 深色主题。 */
val DarkAppColors = AppColors(
    pageBackground = Color(0xFF121212),
    surfaceCard = Color(0xFF1E1E1E),
    surfaceCardSub = Color(0xFF262626),
    surfaceTint = Color(0xFF2A2A2A),
    border = Color(0xFF333333),
    divider = Color(0xFF3A3A3A),
    outline = Color(0xFF555555),
    outlineStrong = Color(0xFF666666),
    textPrimary = Color(0xFFECECEC),
    textDefault = Color(0xFFDDDDDD),
    textSecondary = Color(0xFFB0B0B0),
    textHint = Color(0xFF9E9E9E),
    textDisabled = Color(0xFF777777),
    textInverse = Color(0xFF1E1E1E),
    accent = Color(0xFFB39DDB),
    danger = Color(0xFFEF5350),
    dangerSoft = Color(0xFFE57373),
    dangerLight = Color(0xFFFF8A80),
    dangerSurface = Color(0xFF321C20),
    dangerBorder = Color(0xFF7F4A50),
    success = Color(0xFF81C784),
)

/** 当前主题颜色，由 [YIClassTheme] 在根部提供。 */
val LocalAppColors = staticCompositionLocalOf { LightAppColors }
