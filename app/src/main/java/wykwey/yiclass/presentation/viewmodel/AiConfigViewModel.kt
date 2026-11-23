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
import wykwey.yiclass.data.ai.AiService
import wykwey.yiclass.domain.model.AiConfig
import wykwey.yiclass.domain.model.AiProvider
import kotlinx.coroutines.CancellationException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import wykwey.yiclass.domain.repository.AiConfigRepository
import javax.inject.Inject

data class AiConfigUiState(
    val apiKey: String = "",
    val endpoint: String = "",
    val visionModel: String = AiConfig.DEFAULT_MODEL,
    val textModel: String = AiConfig.DEFAULT_MODEL,
    val enableImageImport: Boolean = false,
    val enableTableImport: Boolean = false,
    val enableTextImport: Boolean = false,
    val enableWebImport: Boolean = false,
    val loading: Boolean = true,
    val testing: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null
)

/** AI 导入设置页状态，对齐原版 AIConfigPage。 */
@HiltViewModel
class AiConfigViewModel @Inject constructor(
    private val repository: AiConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiConfigUiState())
    val uiState: StateFlow<AiConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val c = repository.config.first()
            _uiState.value = AiConfigUiState(
                apiKey = c.apiKey,
                endpoint = c.endpoint,
                visionModel = c.visionModel,
                textModel = c.textModel,
                enableImageImport = c.enableImageImport,
                enableTableImport = c.enableTableImport,
                enableTextImport = c.enableTextImport,
                enableWebImport = c.enableWebImport,
                loading = false
            )
        }
    }

    fun setApiKey(v: String) = _uiState.update { it.copy(apiKey = v) }
    fun setEndpoint(v: String) = _uiState.update { it.copy(endpoint = v) }
    fun setVisionModel(v: String) = _uiState.update { it.copy(visionModel = v) }
    fun setTextModel(v: String) = _uiState.update { it.copy(textModel = v) }
    fun setImageImport(v: Boolean) = _uiState.update { it.copy(enableImageImport = v) }
    fun setTableImport(v: Boolean) = _uiState.update { it.copy(enableTableImport = v) }
    fun setTextImport(v: Boolean) = _uiState.update { it.copy(enableTextImport = v) }
    fun setWebImport(v: Boolean) = _uiState.update { it.copy(enableWebImport = v) }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun applyPreset(provider: AiProvider) {
        _uiState.update { it.copy(endpoint = provider.endpoint, visionModel = provider.model, textModel = provider.model) }
    }

    /** 保存配置（含校验），对齐原版 _saveConfig。 */
    fun save() {
        val state = _uiState.value
        val config = AiConfig(
            apiKey = state.apiKey.trim(),
            endpoint = state.endpoint.trim(),
            visionModel = state.visionModel.trim(),
            textModel = state.textModel.trim(),
            enableImageImport = state.enableImageImport,
            enableTableImport = state.enableTableImport,
            enableTextImport = state.enableTextImport,
            enableWebImport = state.enableWebImport
        )
        if (!config.isValid || config.endpoint.toHttpUrlOrNull() == null) {
            _uiState.update { it.copy(message = "请检查 API Key、HTTP 接口地址和模型设置") }
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

    /** 测试配置：调用真实 AI 接口验证能否连通。 */
    fun testConfig() {
        val state = _uiState.value
        if (state.apiKey.isBlank() || state.endpoint.isBlank()) {
            _uiState.update { it.copy(message = "请先填写API Key和接口地址") }
            return
        }
        if (state.textModel.isBlank()) {
            _uiState.update { it.copy(message = "请先填写文本模型") }
            return
        }
        val config = AiConfig(
            apiKey = state.apiKey.trim(),
            endpoint = state.endpoint.trim(),
            visionModel = state.visionModel.trim(),
            textModel = state.textModel.trim()
        )
        viewModelScope.launch {
            _uiState.update { it.copy(testing = true) }
            val message = try {
                AiService.testConfig(config)
                "测试成功，文本模型可用"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                "测试失败：${e.message}"
            }
            _uiState.update { it.copy(testing = false, message = message) }
        }
    }
}
