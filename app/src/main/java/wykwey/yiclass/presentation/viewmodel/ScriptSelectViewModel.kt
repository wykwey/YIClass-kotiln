package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import wykwey.yiclass.presentation.navigation.AppRoute
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wykwey.yiclass.data.repository.RepositoryDownloadService
import wykwey.yiclass.data.repository.SchoolIndexService
import wykwey.yiclass.data.repository.ScriptFileService
import wykwey.yiclass.domain.model.SchoolEntry
import wykwey.yiclass.domain.repository.RepositoryConfigRepository
import javax.inject.Inject

data class ScriptSelectUiState(
    val loading: Boolean = true,
    val school: SchoolEntry? = null,
    val scriptExistsMap: Map<String, Boolean> = emptyMap(),
    val downloading: Boolean = false,
    val message: String? = null
)

/** 脚本选择页状态，对齐原版 ScriptSelectPage。 */
@HiltViewModel
class ScriptSelectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val schoolIndexService: SchoolIndexService,
    private val scriptFileService: ScriptFileService,
    private val repositoryConfigRepository: RepositoryConfigRepository,
    private val downloadService: RepositoryDownloadService
) : ViewModel() {

    private val schoolId: Long = savedStateHandle.toRoute<AppRoute.ScriptSelect>().schoolId

    private val _uiState = MutableStateFlow(ScriptSelectUiState())
    val uiState: StateFlow<ScriptSelectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val school = schoolIndexService.getSchoolById(schoolId)
            _uiState.update { it.copy(loading = false, school = school) }
            checkScriptsExist()
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun checkScriptsExist() {
        val school = _uiState.value.school ?: return
        viewModelScope.launch {
            val names = school.scripts.map { it.scriptName }
            val map = scriptFileService.checkScriptsExist(names)
            _uiState.update { it.copy(scriptExistsMap = map) }
        }
    }

    fun downloadAllScripts() {
        val school = _uiState.value.school ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(downloading = true) }
            try {
                val config = repositoryConfigRepository.config.first()
                if (!config.isValid) {
                    _uiState.update { it.copy(downloading = false, message = "请先配置仓库信息") }
                    return@launch
                }
                val names = school.scripts.map { it.scriptName }
                val successCount = downloadService.downloadScripts(config, names)
                _uiState.update {
                    it.copy(
                        downloading = false,
                        message = if (successCount > 0) "成功下载 $successCount/${names.size} 个脚本"
                        else "下载失败，请检查网络连接或仓库配置"
                    )
                }
                checkScriptsExist()
            } catch (e: Exception) {
                _uiState.update { it.copy(downloading = false, message = "下载失败: ${e.message}") }
            }
        }
    }

    fun downloadScript(scriptName: String) {
        viewModelScope.launch {
            try {
                val config = repositoryConfigRepository.config.first()
                if (!config.isValid) {
                    _uiState.update { it.copy(message = "请先配置仓库信息") }
                    return@launch
                }
                val successCount = downloadService.downloadScripts(config, listOf(scriptName))
                _uiState.update {
                    it.copy(
                        message = if (successCount > 0) "脚本下载成功" else "下载失败，请检查网络连接或仓库配置"
                    )
                }
                checkScriptsExist()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "下载失败: ${e.message}") }
            }
        }
    }

    fun deleteScript(scriptName: String) {
        viewModelScope.launch {
            try {
                val success = scriptFileService.deleteScript(scriptName)
                _uiState.update {
                    it.copy(message = if (success) "脚本删除成功" else "脚本不存在")
                }
                checkScriptsExist()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "删除失败: ${e.message}") }
            }
        }
    }

    fun refresh() {
        checkScriptsExist()
    }
}
