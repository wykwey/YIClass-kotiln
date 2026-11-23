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
import wykwey.yiclass.domain.repository.TimetableRepository
import wykwey.yiclass.domain.timetable.TimetableSelection
import javax.inject.Inject

data class CourseReminderUiState(
    val reminderEnabled: Boolean = false,
    val advanceMinutes: Int = 5,
    val timetableId: Long? = null,
    val loading: Boolean = true
)

@HiltViewModel
class CourseReminderViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val settingsRepository: SettingsRepository,
    selection: TimetableSelection
) : ViewModel() {

    val uiState: StateFlow<CourseReminderUiState> = combine(
        settingsRepository.courseReminderEnabled,
        selection.timetable
    ) { enabled, timetable ->
        CourseReminderUiState(
            reminderEnabled = enabled,
            advanceMinutes = timetable?.settings?.reminderMinutes ?: 5,
            timetableId = timetable?.id,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseReminderUiState())

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCourseReminderEnabled(enabled) }
    }

    fun setAdvanceMinutes(minutes: Int) {
        val timetableId = uiState.value.timetableId ?: return
        viewModelScope.launch { timetableRepository.setReminderMinutes(timetableId, minutes) }
    }
}
