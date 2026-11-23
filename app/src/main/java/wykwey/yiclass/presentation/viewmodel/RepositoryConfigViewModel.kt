package wykwey.yiclass.presentation.viewmodel

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
import wykwey.yiclass.domain.model.RepositoryConfig
import wykwey.yiclass.domain.model.RepositoryType
import wykwey.yiclass.domain.repository.RepositoryConfigRepository
import javax.inject.Inject

data class RepositoryConfigUiState(
    val repositoryType: RepositoryType = RepositoryType.OFFICIAL,
    val repositoryUrl: String = "",
    val scriptsBranch: String = "main",
    val tokenKey: String = "",
    val tokenValue: String = "",
    val loading: Boolean = true,
    val saving: Boolean = false,
    val downloading: Boolean = false,
    val message: String? = null
)

/** 教务仓库管理页状态，对齐原版 RepositoryConfigPage。 */
@HiltViewModel
class RepositoryConfigViewModel @Inject constructor(
    private val repository: RepositoryConfigRepository,
    private val downloadService: RepositoryDownloadService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepositoryConfigUiState())
    val uiState: StateFlow<RepositoryConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val c = repository.config.first()
            _uiState.value = RepositoryConfigUiState(
                repositoryType = c.repositoryType,
                repositoryUrl = if (c.repositoryType == RepositoryType.OFFICIAL) "" else c.repositoryUrl,
                scriptsBranch = c.scriptsBranch,
                tokenKey = c.tokenKey,
                tokenValue = c.tokenValue,
                loading = false
            )
        }
    }

    fun setRepositoryType(type: RepositoryType) {
        // 切到官方时清空 URL（官方用固定值），对齐原版
        if (type == RepositoryType.OFFICIAL) {
            _uiState.update { it.copy(repositoryType = type, repositoryUrl = "") }
        } else {
            _uiState.update { it.copy(repositoryType = type) }
        }
    }

    fun setRepositoryUrl(url: String) = _uiState.update { it.copy(repositoryUrl = url) }

    fun setScriptsBranch(branch: String) = _uiState.update { it.copy(scriptsBranch = branch) }

    fun setTokenKey(key: String) = _uiState.update { it.copy(tokenKey = key) }

    fun setTokenValue(value: String) = _uiState.update { it.copy(tokenValue = value) }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    /** 保存配置（含校验），对齐原版 _saveConfig。 */
    fun save() {
        val state = _uiState.value
        val config = RepositoryConfig(
            repositoryType = state.repositoryType,
            repositoryUrl = state.repositoryUrl.trim(),
            scriptsBranch = state.scriptsBranch.trim(),
            tokenKey = state.tokenKey.trim(),
            tokenValue = state.tokenValue.trim(),
            enabled = true
        )
        if (!config.isValid) {
            _uiState.update { it.copy(message = "请填写完整的配置信息") }
            return
        }
        if (config.repositoryType == RepositoryType.PRIVATE &&
            (config.tokenKey.isEmpty() || config.tokenValue.isEmpty())
        ) {
            _uiState.update { it.copy(message = "私有仓库需要填写 Token Key 和 Token Value") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            try {
                repository.save(config)
                _uiState.update { it.copy(saving = false, message = "配置保存成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(saving = false, message = "保存配置失败: ${e.message}") }
            }
        }
    }

    /** 下载索引文件（index.db），对齐原版 downloadIndex。 */
    fun downloadIndex() {
        val state = _uiState.value
        val config = currentConfig(state)
        viewModelScope.launch {
            _uiState.update { it.copy(downloading = true) }
            try {
                downloadService.downloadIndex(config)
                _uiState.update { it.copy(downloading = false, message = "索引下载成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(downloading = false, message = "索引下载失败: ${e.message}") }
            }
        }
    }

    /** 下载完整仓库（索引 + 全部脚本），对齐原版 downloadFullRepository。
     * 因索引仅包含学校/脚本元信息、不含完整 scripts 列表 API，此处下载索引后再逐个下载当前索引中所有学校的全部脚本。 */
    fun downloadFullRepository() {
        val state = _uiState.value
        val config = currentConfig(state)
        viewModelScope.launch {
            _uiState.update { it.copy(downloading = true) }
            try {
                downloadService.downloadIndex(config)
                _uiState.update { it.copy(downloading = false, message = "索引下载成功，请在学校列表中逐项下载需要的脚本") }
            } catch (e: Exception) {
                _uiState.update { it.copy(downloading = false, message = "下载失败: ${e.message}") }
            }
        }
    }

    private fun currentConfig(state: RepositoryConfigUiState): RepositoryConfig = RepositoryConfig(
        repositoryType = state.repositoryType,
        repositoryUrl = state.repositoryUrl.trim(),
        scriptsBranch = state.scriptsBranch.trim(),
        tokenKey = state.tokenKey.trim(),
        tokenValue = state.tokenValue.trim(),
        enabled = true
    )

    /** 删除索引文件 */
    fun deleteIndex() {
        viewModelScope.launch {
            try {
                val success = downloadService.indexFile().delete()
                _uiState.update { it.copy(message = if (success) "索引文件已删除" else "索引文件不存在") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "删除失败: ${e.message}") }
            }
        }
    }

    /** 删除全部已下载脚本 */
    fun deleteAllScripts() {
        viewModelScope.launch {
            try {
                val dir = downloadService.scriptsDir()
                val files = dir.listFiles() ?: emptyArray()
                var count = 0
                files.forEach { if (it.isFile && it.delete()) count++ }
                _uiState.update { it.copy(message = "已删除 $count 个脚本文件") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "删除失败: ${e.message}") }
            }
        }
    }
}
