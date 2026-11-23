package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wykwey.yiclass.domain.model.Holiday
import wykwey.yiclass.domain.repository.TimetableRepository
import wykwey.yiclass.domain.util.MainlandHolidayCalendar
import wykwey.yiclass.domain.util.WeekCalculator
import wykwey.yiclass.presentation.navigation.AppRoute

@HiltViewModel
class AcademicCalendarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TimetableRepository
) : ViewModel() {
    private val timetableId = savedStateHandle.toRoute<AppRoute.AcademicCalendar>().timetableId
    val timetable = repository.observeTimetableDetails(timetableId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun importHolidays() {
        val settings = timetable.value?.settings ?: return
        val start = settings.startDate
        val end = WeekCalculator.dateInWeek(start, settings.totalWeeks, 7)
        val entries = MainlandHolidayCalendar.adjustmentsBetween(start, end)
        val uncoveredYears = (start.year..end.year).filterNot { it in MainlandHolidayCalendar.coveredYears }
        viewModelScope.launch {
            val added = repository.addCalendarEntries(timetableId, entries)
            val adjustedWorkdays = added.count { it.isMakeup }
            val summary = when {
                added.isNotEmpty() -> "已导入 ${added.size - adjustedWorkdays} 天停课、${adjustedWorkdays} 天调休"
                entries.isNotEmpty() -> "学期内的放假与调休日已导入"
                else -> "学期内暂无可导入的放假或调休日"
            }
            _message.value = if (uncoveredYears.isEmpty()) summary
            else "$summary。未收录 ${uncoveredYears.joinToString("、")} 年的放假安排。"
        }
    }

    fun save(date: LocalDate, makeupWeekday: Int?) {
        viewModelScope.launch {
            repository.putHoliday(timetableId, Holiday(date = date, isMakeup = makeupWeekday != null, makeupWeekday = makeupWeekday))
        }
    }

    fun delete(date: LocalDate) {
        viewModelScope.launch { repository.deleteHoliday(timetableId, date) }
    }
}
