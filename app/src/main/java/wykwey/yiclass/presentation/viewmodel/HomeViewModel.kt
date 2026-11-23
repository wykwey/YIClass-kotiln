package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import wykwey.yiclass.domain.timetable.TimetableSelection
import wykwey.yiclass.domain.util.WeekCalculator

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    selection: TimetableSelection
) : ViewModel() {
    private data class WeekSelection(val timetableId: Long, val week: Int)

    private val viewMode = savedStateHandle.getMutableStateFlow("viewMode", ViewMode.WEEK)
    private val weekOverride = MutableStateFlow<WeekSelection?>(null)
    private val today = MutableStateFlow(LocalDate.now())
    private val selectedDay = savedStateHandle.getMutableStateFlow("selectedDay", today.value.dayOfWeek.value)

    val uiState = combine(viewMode, weekOverride, selection.timetable, selectedDay, today) { mode, override, current, day, date ->
        val settings = current?.settings
        val totalWeeks = settings?.totalWeeks ?: 20
        val week = override?.takeIf { it.timetableId == current?.id }?.week?.coerceIn(1, totalWeeks)
            ?: settings?.let { WeekCalculator.currentWeek(it.startDate, totalWeeks, date) } ?: 1
        HomeUiState(
            viewMode = mode,
            currentWeek = week,
            totalWeeks = totalWeeks,
            todayWeek = settings?.let { WeekCalculator.currentWeekOrNull(it.startDate, totalWeeks, date) },
            selectedDay = day,
            currentTimetable = current,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(viewMode = viewMode.value))

    fun refreshDate() {
        val date = LocalDate.now()
        if (today.value == date) return
        today.value = date
        selectedDay.value = date.dayOfWeek.value
    }

    fun switchView(mode: ViewMode) {
        if (mode == ViewMode.DAY && viewMode.value != mode) selectedDay.value = today.value.dayOfWeek.value
        viewMode.value = mode
    }

    fun selectDay(weekday: Int) {
        selectedDay.value = weekday
    }

    fun jumpToWeek(week: Int) {
        val timetableId = uiState.value.currentTimetable?.id ?: return
        weekOverride.value = WeekSelection(timetableId, week)
    }
}
