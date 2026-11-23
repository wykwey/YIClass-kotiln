package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.timetable.TimetableSelection
import wykwey.yiclass.domain.repository.TimetableRepository
import wykwey.yiclass.domain.util.TimetableDefaults
import javax.inject.Inject

data class TimetableManagementUiState(
    val timetables: List<Timetable> = emptyList(),
    val currentId: Long? = null,
    val loading: Boolean = true
)

/**
 * 课表管理对话框的状态与操作，对齐原版 TimetableManagementDialog + YicoreScheduleManager：
 * 新建 / 重命名 / 删除 / 切换当前课表。
 */
@HiltViewModel
class TimetableManagementViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val selection: TimetableSelection
) : ViewModel() {

    val uiState: StateFlow<TimetableManagementUiState> = combine(
        timetableRepository.observeTimetables(),
        selection.selectedId
    ) { timetables, currentId ->
        TimetableManagementUiState(
            timetables = timetables,
            currentId = currentId,
            loading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimetableManagementUiState()
    )

    fun create(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            selection.addAndSelect(Timetable(name = trimmed, settings = TimetableDefaults.defaultSettings()))
        }
    }

    fun rename(id: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { timetableRepository.renameTimetable(id, trimmed) }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            val state = uiState.value
            val wasCurrent = state.currentId == id
            val fallback = state.timetables.firstOrNull { it.id != id }
            timetableRepository.deleteTimetable(id)
            // 若删除的正是当前课表，切换到列表中剩余的第一个（若还有）
            if (wasCurrent) {
                selection.select(fallback?.id)
            }
        }
    }

    fun selectCurrent(id: Long) {
        viewModelScope.launch { selection.select(id) }
    }
}
