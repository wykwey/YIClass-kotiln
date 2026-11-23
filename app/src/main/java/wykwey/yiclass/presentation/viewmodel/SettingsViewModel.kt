package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.repository.AiConfigRepository
import wykwey.yiclass.domain.repository.SettingsRepository
import wykwey.yiclass.domain.repository.TimetableRepository
import wykwey.yiclass.domain.timetable.TimetableSelection
import java.time.LocalDate
import javax.inject.Inject

/**
 * 设置主页状态：当前课表（用于展示开始日期/总周数/显示周末）+ 高级功能开关。
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val settingsRepository: SettingsRepository,
    selection: TimetableSelection,
    aiConfigRepository: AiConfigRepository
) : ViewModel() {

    val currentTimetable: StateFlow<Timetable?> =
        selection.timetable.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val advancedFeaturesEnabled: StateFlow<Boolean> =
        settingsRepository.advancedFeaturesEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val aiImportEnabled = aiConfigRepository.config.map { it.hasImportOptions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setStartDate(date: LocalDate) {
        val t = currentTimetable.value ?: return
        viewModelScope.launch { timetableRepository.setStartDate(t.id, date) }
    }

    fun setShowWeekend(show: Boolean) {
        val t = currentTimetable.value ?: return
        viewModelScope.launch { timetableRepository.setShowWeekend(t.id, show) }
    }

    fun setAdvancedFeaturesEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAdvancedFeaturesEnabled(enabled) }
    }
}
