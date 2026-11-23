package wykwey.yiclass.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 关闭课程卡片字体自适应颜色（对齐原版 AppSettings.disableAdaptiveFontColor）。
 * 由 MainActivity 根组件读取 DataStore 后提供，周视图 CourseCard 消费；
 * 开启后课程文字恒用默认深色，不再按背景色自动计算对比色。
 */
val LocalDisableAdaptiveFontColor = staticCompositionLocalOf { false }
