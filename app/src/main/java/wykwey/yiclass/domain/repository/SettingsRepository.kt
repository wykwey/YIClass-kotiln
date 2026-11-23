package wykwey.yiclass.domain.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * 应用级偏好（替代原版 SharedPreferences / AppSettings），由 DataStore 持久化。
 */
interface SettingsRepository {

    /** 当前选中的课表 id；未选择时为 null。 */
    val currentTimetableId: Flow<Long?>

    val advancedFeaturesEnabled: Flow<Boolean>

    /** 课程提醒总开关，对齐原版 AppSettings.courseReminder。 */
    val courseReminderEnabled: Flow<Boolean>

    /** 关闭课程卡片字体自适应颜色，对齐原版 AppSettings.disableAdaptiveFontColor。 */
    val disableAdaptiveFontColor: Flow<Boolean>

    /** 主题模式，对齐原版 ThemeSettingsPage 的 _selectedTheme。 */
    val themeMode: Flow<ThemeMode>

    suspend fun setCurrentTimetableId(id: Long?)

    suspend fun setAdvancedFeaturesEnabled(enabled: Boolean)

    suspend fun setCourseReminderEnabled(enabled: Boolean)

    suspend fun setDisableAdaptiveFontColor(disabled: Boolean)

    suspend fun setThemeMode(mode: ThemeMode)
}
