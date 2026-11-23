package wykwey.yiclass.presentation.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.presentation.ui.components.feedback.AppConfirmDialog
import wykwey.yiclass.presentation.ui.components.feedback.SonnerHost
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.viewmodel.CourseEditUiState
import wykwey.yiclass.presentation.viewmodel.CourseEditViewModel
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar

/** 页面背景灰，对齐原版 course_edit_page.dart 的 LocalAppColors.current.pageBackground。 */
private val PageBackground: Color
    @Composable get() = LocalAppColors.current.pageBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditScreen(
    onDone: () -> Unit,
    viewModel: CourseEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // 保存/删除完成后返回
    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = PageBackground,
            topBar = {
                YIClassAppBar(
                    titleContent = { Text(if (state.isNew) "添加课程" else "编辑课程") },
                    centerTitle = true,
                    showBack = true,
                    onBack = onDone,
                    actions = {
                        if (!state.isNew) {
                            AppIconButton(
                                icon = Icons.Filled.Delete,
                                contentDescription = "删除",
                                tint = LocalAppColors.current.danger,
                                showBorder = false,
                                onClick = { showDeleteConfirm = true }
                            )
                        }
                        AppIconButton(
                            icon = Icons.Filled.Save,
                            contentDescription = "保存",
                            showBorder = false,
                            onClick = viewModel::save,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
            }
        ) { innerPadding ->
            CourseEditContent(state = state, viewModel = viewModel, innerPadding = innerPadding)
        }

        // sonner 风格浮层：解析/保存失败提示，非 Material SnackBar
        SonnerHost(
            message = state.error,
            onDismiss = viewModel::clearError,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // 删除前二次确认，对齐原版 YicoreConfirm 的删除确认弹窗
    if (showDeleteConfirm) {
        AppConfirmDialog(
            title = "删除课程",
            message = "确定要删除这门课程吗？此操作无法撤销。",
            confirmText = "删除",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.delete()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun CourseEditContent(
    state: CourseEditUiState,
    viewModel: CourseEditViewModel,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (state.loading) {
            Text("加载中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }

        BasicInfoSection(
            name = state.name,
            location = state.location,
            teacher = state.teacher,
            nameError = state.nameError,
            onNameChange = viewModel::setName,
            onLocationChange = viewModel::setLocation,
            onTeacherChange = viewModel::setTeacher
        )

        ScheduleSection(
            schedules = state.schedules,
            onAdd = viewModel::addSchedule,
            onRemove = viewModel::removeSchedule,
            onUpdate = viewModel::updateSchedule
        )

        // 颜色选择放在最下面
        ColorSection(
            selectedColor = state.color,
            onColorSelected = viewModel::setColor
        )
    }
}
