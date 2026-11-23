package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wykwey.yiclass.domain.repository.SettingsRepository
import wykwey.yiclass.domain.repository.ThemeMode
import javax.inject.Inject

data class ThemeSettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val disableAdaptiveFontColor: Boolean = false
)

/** 主题设置，对齐原版 ThemeSettingsPage：主题模式三选一 + 关闭课程卡片字体自适应颜色。 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<ThemeSettingsUiState> = combine(
        settingsRepository.themeMode,
        settingsRepository.disableAdaptiveFontColor
    ) { mode, disableAdaptive ->
        ThemeSettingsUiState(themeMode = mode, disableAdaptiveFontColor = disableAdaptive)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeSettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDisableAdaptiveFontColor(disabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDisableAdaptiveFontColor(disabled) }
    }

}
