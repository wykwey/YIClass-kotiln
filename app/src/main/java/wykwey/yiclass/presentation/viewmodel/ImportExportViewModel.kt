package wykwey.yiclass.presentation.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wykwey.yiclass.domain.timetable.TimetableSelection
import wykwey.yiclass.domain.repository.TimetableRepository
import wykwey.yiclass.domain.util.TimetableJson
import javax.inject.Inject

data class ImportExportUiState(
    val message: String? = null
)

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val selection: TimetableSelection
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState: StateFlow<ImportExportUiState> = _uiState.asStateFlow()

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    /** 从用户选择的 JSON 文件导入课表；contentResolver 由调用方（Activity/Compose）传入。 */
    fun importFromUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                } ?: throw IllegalStateException("无法读取文件")

                val timetable = TimetableJson.fromJson(text)
                selection.addAndSelect(timetable)
                _uiState.update { it.copy(message = "导入成功：${timetable.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "导入失败：${e.message}") }
            }
        }
    }

    /** 导出当前课表到用户通过 SAF 选择的目标 Uri。 */
    fun exportToUri(contentResolver: ContentResolver, uri: Uri, timetableId: Long) {
        viewModelScope.launch {
            try {
                val timetable = timetableRepository.getTimetableDetails(timetableId)
                    ?: throw IllegalStateException("课表不存在")
                val jsonText = TimetableJson.toJson(timetable)
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { it.write(jsonText.toByteArray()) }
                        ?: throw IllegalStateException("无法写入文件")
                }
                _uiState.update { it.copy(message = "导出成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "导出失败：${e.message}") }
            }
        }
    }
}
