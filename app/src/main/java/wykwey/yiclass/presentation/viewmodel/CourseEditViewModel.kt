package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import wykwey.yiclass.presentation.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.CourseSchedule
import wykwey.yiclass.domain.repository.TimetableRepository
import wykwey.yiclass.domain.util.NumberPatternParser
import javax.inject.Inject

@HiltViewModel
class CourseEditViewModel @Inject constructor(
    private val repository: TimetableRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AppRoute.CourseEdit>()
    private val timetableId = route.timetableId
    private val courseId = route.courseId

    private val _uiState = MutableStateFlow(CourseEditUiState())
    val uiState: StateFlow<CourseEditUiState> = _uiState.asStateFlow()

    init {
        loadCourse()
    }

    private fun loadCourse() {
        viewModelScope.launch {
            val existing = if (courseId > 0) repository.getCourse(courseId) else null
            if (existing != null) {
                _uiState.value = CourseEditUiState(
                    isNew = false,
                    name = existing.name,
                    location = existing.location,
                    teacher = existing.teacher,
                    color = existing.color,
                    schedules = existing.schedules.map { s ->
                        ScheduleInput(
                            weekday = s.weekday,
                            weekPatternText = NumberPatternParser.format(s.weekPattern),
                            periodsText = NumberPatternParser.format(s.periods)
                        )
                    }.ifEmpty { listOf(ScheduleInput()) },
                    loading = false
                )
            } else {
                _uiState.update {
                    it.copy(
                        loading = false,
                        schedules = listOf(ScheduleInput(
                            weekday = route.weekday,
                            weekPatternText = route.week.takeIf { week -> week > 0 }?.toString().orEmpty(),
                            periodsText = route.period.takeIf { period -> period > 0 }?.toString().orEmpty()
                        ))
                    )
                }
            }
        }
    }

    fun setName(v: String) = _uiState.update { it.copy(name = v, nameError = null) }
    fun setLocation(v: String) = _uiState.update { it.copy(location = v) }
    fun setTeacher(v: String) = _uiState.update { it.copy(teacher = v) }
    fun setColor(argb: Int) = _uiState.update { it.copy(color = argb) }

    fun addSchedule() = _uiState.update { it.copy(schedules = it.schedules + ScheduleInput()) }

    fun removeSchedule(index: Int) = _uiState.update {
        if (it.schedules.size <= 1) it
        else it.copy(schedules = it.schedules.filterIndexed { i, _ -> i != index })
    }

    fun updateSchedule(index: Int, transform: (ScheduleInput) -> ScheduleInput) = _uiState.update {
        it.copy(schedules = it.schedules.mapIndexed { i, s -> if (i == index) transform(s) else s })
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "请输入课程名称") }
            return
        }
        val schedules: List<CourseSchedule>
        try {
            schedules = state.schedules.map { input ->
                CourseSchedule(
                    weekday = input.weekday,
                    periods = NumberPatternParser.parse(input.periodsText.trim(), defaultMax = 20),
                    weekPattern = NumberPatternParser.parse(input.weekPatternText.trim(), defaultMax = 30)
                )
            }
        } catch (e: NumberPatternParser.PatternFormatException) {
            _uiState.update { it.copy(error = "周次/节次解析错误：${e.message}") }
            return
        }

        viewModelScope.launch {
            try {
                repository.upsertCourse(
                    Course(
                        id = if (courseId > 0) courseId else 0,
                        timetableId = timetableId,
                        name = state.name.trim(),
                        location = state.location.trim(),
                        teacher = state.teacher.trim(),
                        color = state.color,
                        schedules = schedules
                    )
                )
                _uiState.update { it.copy(saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存失败：${e.message}") }
            }
        }
    }

    fun delete() {
        if (courseId <= 0) return
        viewModelScope.launch {
            try {
                repository.deleteCourse(courseId)
                _uiState.update { it.copy(saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除失败：${e.message}") }
            }
        }
    }
}
