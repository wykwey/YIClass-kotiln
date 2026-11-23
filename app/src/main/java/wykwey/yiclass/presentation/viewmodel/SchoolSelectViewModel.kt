package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wykwey.yiclass.data.repository.SchoolIndexService
import wykwey.yiclass.domain.model.SchoolEntry
import javax.inject.Inject

data class SchoolSelectUiState(
    val loading: Boolean = true,
    val groupedSchools: Map<String, List<SchoolEntry>> = emptyMap(),
    val searchQuery: String = "",
    val searchResults: List<SchoolEntry> = emptyList(),
    val isSearching: Boolean = false,
    val indexMissing: Boolean = false,
    val message: String? = null
)

/** 学校选择页状态，对齐原版 SchoolSelectPage。 */
@HiltViewModel
class SchoolSelectViewModel @Inject constructor(
    private val schoolIndexService: SchoolIndexService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolSelectUiState())
    val uiState: StateFlow<SchoolSelectUiState> = _uiState.asStateFlow()

    init {
        loadSchools()
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun loadSchools() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            if (!schoolIndexService.isIndexDownloaded()) {
                _uiState.update { it.copy(loading = false, indexMissing = true) }
                return@launch
            }
            try {
                val grouped = schoolIndexService.getSchoolsGroupedByLetter()
                _uiState.update { it.copy(loading = false, indexMissing = false, groupedSchools = grouped) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, message = "加载学校列表失败: ${e.message}") }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(isSearching = false, searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val results = schoolIndexService.searchSchools(query)
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun refresh() {
        loadSchools()
    }
}
