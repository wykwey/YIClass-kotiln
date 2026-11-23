package wykwey.yiclass.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import wykwey.yiclass.data.repository.ScriptFileService
import wykwey.yiclass.domain.timetable.TimetableSelection
import wykwey.yiclass.domain.util.TimetableJson
import wykwey.yiclass.presentation.navigation.AppRoute

data class EduImportUiState(
    val scriptUrl: String,
    val importing: Boolean = false,
    val message: String? = null,
    val done: Boolean = false
)

@HiltViewModel
class EduImportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scriptFileService: ScriptFileService,
    private val selection: TimetableSelection
) : ViewModel() {
    private val route = savedStateHandle.toRoute<AppRoute.EduImport>()
    private val _uiState = MutableStateFlow(EduImportUiState(scriptUrl = route.scriptUrl))
    val uiState = _uiState.asStateFlow()

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun importWith(runScript: suspend (String) -> String) {
        if (_uiState.value.importing) return
        _uiState.update { it.copy(importing = true, message = null) }
        viewModelScope.launch {
            try {
                val script = scriptFileService.readScriptContent(route.scriptName) ?: throw IOException("请重新下载教务脚本")
                val payload = Json.parseToJsonElement(runScript(script)).jsonObject
                val error = (payload["error"] as? JsonPrimitive)?.contentOrNull
                if (error != null) throw IllegalArgumentException(error)
                selection.addAndSelect(TimetableJson.fromProviderJson(payload.toString()))
                _uiState.update { it.copy(importing = false, done = true) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _uiState.update { it.copy(importing = false, message = "导入失败：${e.message}") }
            }
        }
    }
}
