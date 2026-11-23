package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wykwey.yiclass.domain.model.ClassTime
import wykwey.yiclass.domain.repository.TimetableRepository
import wykwey.yiclass.domain.util.DefaultPeriodTimes
import wykwey.yiclass.presentation.navigation.AppRoute
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TimeSettingsUiState(
    val startDate: LocalDate = LocalDate.now(),
    val totalWeeks: Int = 20,
    val maxPeriods: Int = 16,
    val classTimes: Map<Int, ClassTime> = emptyMap(),
    val useSameDuration: Boolean = false,
    val durationMinutes: String = "45",
    val loading: Boolean = true,
    val saved: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class TimeSettingsViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val timetableId = savedStateHandle.toRoute<AppRoute.TimeSettings>().timetableId

    private val _uiState = MutableStateFlow(TimeSettingsUiState())
    val uiState: StateFlow<TimeSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val timetable = timetableRepository.getTimetableDetails(timetableId) ?: return@launch
            val maxPeriods = timetable.settings.maxPeriods.takeIf { it > 0 } ?: 16
            val loaded = timetable.settings.classTimes.associateBy { it.period }
            val filled = (1..maxPeriods).associateWith { p ->
                loaded[p] ?: run {
                    val (s, e) = DefaultPeriodTimes.startEnd(p)
                    ClassTime(period = p, startTime = s, endTime = e)
                }
            }
            _uiState.value = TimeSettingsUiState(
                startDate = timetable.settings.startDate,
                totalWeeks = timetable.settings.totalWeeks,
                maxPeriods = maxPeriods,
                classTimes = filled,
                loading = false
            )
        }
    }

    fun setTotalWeeks(weeks: Int) = _uiState.update { it.copy(totalWeeks = weeks) }

    fun setMaxPeriods(value: Int) = _uiState.update { state ->
        val newMap = (1..value).associateWith { p ->
            state.classTimes[p] ?: run {
                val (s, e) = DefaultPeriodTimes.startEnd(p)
                ClassTime(period = p, startTime = s, endTime = e)
            }
        }
        state.copy(maxPeriods = value, classTimes = newMap)
    }

    fun setUseSameDuration(value: Boolean) = _uiState.update { it.copy(useSameDuration = value) }

    fun setDurationMinutes(value: String) = _uiState.update { it.copy(durationMinutes = value) }

    /** 应用统一时长：从 08:00 开始逐节推算，对齐原版 _applySameDuration。 */
    fun applySameDuration() {
        val minutes = _uiState.value.durationMinutes.toIntOrNull()
        val maxDuration = (16 * 60 - 1) / _uiState.value.maxPeriods
        if (minutes == null || minutes !in 1..maxDuration) {
            _uiState.update { it.copy(message = "请输入 1 至 $maxDuration 分钟") }
            return
        }
        _uiState.update { state ->
            var start = LocalTime.of(8, 0)
            val newMap = (1..state.maxPeriods).associateWith { p ->
                val end = start.plusMinutes(minutes.toLong())
                val ct = ClassTime(period = p, startTime = start, endTime = end)
                start = end
                ct
            }
            state.copy(classTimes = newMap, message = "已应用统一时长")
        }
    }

    fun updateClassTime(period: Int, start: LocalTime, end: LocalTime) = _uiState.update { state ->
        state.copy(classTimes = state.classTimes + (period to ClassTime(period = period, startTime = start, endTime = end)))
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun save() {
        val state = _uiState.value
        if (state.loading) return
        viewModelScope.launch {
            val saved = timetableRepository.updateTimeSettings(
                timetableId, state.totalWeeks, state.maxPeriods,
                (1..state.maxPeriods).map(state.classTimes::getValue)
            )
            _uiState.update { it.copy(saved = saved, message = if (saved) null else "这个课表已被删除") }
        }
    }
}
