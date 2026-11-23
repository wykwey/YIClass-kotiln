package wykwey.yiclass.presentation.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wykwey.yiclass.data.ai.AiService
import wykwey.yiclass.domain.model.AiConfig
import wykwey.yiclass.domain.repository.AiConfigRepository
import wykwey.yiclass.domain.timetable.TimetableSelection
import wykwey.yiclass.domain.util.TimetableJson
import javax.inject.Inject

data class AiImportUiState(
    val config: AiConfig = AiConfig(),
    val loading: Boolean = true,
    val analyzing: Boolean = false,
    val message: String? = null,
    /** 导入成功后置 true，驱动界面返回上一页。 */
    val done: Boolean = false
)

/**
 * AI 智能导入页状态，对齐原版 AIImportPage：
 * 图片/表格/文字三种导入方式共用同一套“分析 -> 解析 -> importTimetable -> 切换当前课表”流程。
 */
@HiltViewModel
class AiImportViewModel @Inject constructor(
    private val aiConfigRepository: AiConfigRepository,
    private val selection: TimetableSelection
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiImportUiState())
    val uiState: StateFlow<AiImportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = aiConfigRepository.config.first()
            _uiState.update { it.copy(config = config, loading = false) }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun importImage(resolver: ContentResolver, uri: Uri) = runImport("图片") { config ->
        val imageUrl = withContext(Dispatchers.IO) {
            val mimeType = resolver.getType(uri) ?: "image/jpeg"
            val data = resolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IOException("无法读取图片")
            "data:$mimeType;base64,${Base64.encodeToString(data, Base64.NO_WRAP)}"
        }
        AiService.analyzeImage(config, imageUrl)
    }

    fun importTable(tableText: String) = runImport("表格") { config ->
        AiService.analyzeTable(config, tableText)
    }

    fun importText(text: String) = runImport("文字") { config ->
        AiService.analyzeText(config, text)
    }

    /** AI 教务导入：从教务网页提取的表格化文本走同一套表格解析 prompt，对齐原版 AiEduImportPage._analyze。 */
    fun importWebContent(pageText: String) = runImport("网页") { config ->
        require(pageText.isNotBlank()) { "页面没有可导入的课表内容" }
        AiService.analyzeTable(config, pageText)
    }

    private fun runImport(importType: String, analyzer: suspend (AiConfig) -> String) {
        val config = _uiState.value.config
        if (!config.isValid) {
            _uiState.update { it.copy(message = "AI 服务未配置完整，请先在设置中配置") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(analyzing = true) }
            try {
                val jsonText = analyzer(config)
                val timetable = TimetableJson.fromProviderJson(jsonText)
                selection.addAndSelect(timetable)
                _uiState.update { it.copy(analyzing = false, message = "导入成功", done = true) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _uiState.update { it.copy(analyzing = false, message = "分析${importType}失败: ${e.message}") }
            }
        }
    }
}
