package wykwey.yiclass.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wykwey.yiclass.domain.repository.SettingsRepository
import wykwey.yiclass.domain.repository.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val CURRENT_TIMETABLE_ID = longPreferencesKey("current_timetable_id")
        val ADVANCED_FEATURES = booleanPreferencesKey("advanced_features_enabled")
        val COURSE_REMINDER = booleanPreferencesKey("course_reminder_enabled")
        val DISABLE_ADAPTIVE_FONT_COLOR = booleanPreferencesKey("disable_adaptive_font_color")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    override val currentTimetableId: Flow<Long?> =
        context.dataStore.data.map { prefs ->
            // -1 视为"未选择"
            prefs[Keys.CURRENT_TIMETABLE_ID]?.takeIf { it > 0 }
        }

    override val advancedFeaturesEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.ADVANCED_FEATURES] ?: false }

    override val courseReminderEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.COURSE_REMINDER] ?: false }

    override val disableAdaptiveFontColor: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.DISABLE_ADAPTIVE_FONT_COLOR] ?: false }

    override val themeMode: Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            when (prefs[Keys.THEME_MODE]) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    override suspend fun setCurrentTimetableId(id: Long?) {
        context.dataStore.edit { preferences ->
            if (id == null) preferences.remove(Keys.CURRENT_TIMETABLE_ID)
            else preferences[Keys.CURRENT_TIMETABLE_ID] = id
        }
    }

    override suspend fun setAdvancedFeaturesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ADVANCED_FEATURES] = enabled }
    }

    override suspend fun setCourseReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.COURSE_REMINDER] = enabled }
    }

    override suspend fun setDisableAdaptiveFontColor(disabled: Boolean) {
        context.dataStore.edit { it[Keys.DISABLE_ADAPTIVE_FONT_COLOR] = disabled }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit {
            it[Keys.THEME_MODE] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }

}
