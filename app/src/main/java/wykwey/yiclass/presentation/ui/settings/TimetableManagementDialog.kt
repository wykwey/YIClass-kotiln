package wykwey.yiclass.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.presentation.ui.components.feedback.AppConfirmDialog
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppIconButton
import wykwey.yiclass.presentation.ui.components.inputs.AppTextField
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import wykwey.yiclass.presentation.viewmodel.TimetableManagementViewModel
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 课表管理对话框，对齐原版 TimetableManagementDialog + YicoreScheduleManager：
 * 列表展示所有课表，支持点击切换当前、重命名、删除（当前课表不可删除），底部新建输入框。
 */
@Composable
fun TimetableManagementDialog(
    onDismiss: () -> Unit,
    viewModel: TimetableManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var newName by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Timetable?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        AppCard(
            modifier = Modifier.widthIn(max = 480.dp),
            contentPadding = 0.dp
        ) {
            Column {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "课表管理",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    AppIconButton(icon = Icons.Filled.Close, contentDescription = "关闭", onClick = onDismiss)
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .height(320.dp)
                ) {
                    items(state.timetables, key = { it.id }) { timetable ->
                        TimetableRow(
                            timetable = timetable,
                            isCurrent = timetable.id == state.currentId,
                            onSelect = { viewModel.selectCurrent(timetable.id) },
                            onRename = { newTitle -> viewModel.rename(timetable.id, newTitle) },
                            onDeleteRequest = { pendingDelete = timetable }
                        )
                    }
                }

                // 底部新建输入框
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = "输入新课表名称",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AppButton(
                        text = "新建",
                        onClick = {
                            if (newName.isNotBlank()) {
                                viewModel.create(newName)
                                newName = ""
                            }
                        }
                    )
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AppConfirmDialog(
            title = "确认删除",
            message = "确定要删除课表\"${target.name}\"吗？此操作无法撤销。",
            confirmText = "删除",
            onConfirm = {
                viewModel.delete(target.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun TimetableRow(
    timetable: Timetable,
    isCurrent: Boolean,
    onSelect: () -> Unit,
    onRename: (String) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var editing by remember(timetable.id) { mutableStateOf(false) }
    var editingText by remember(timetable.id) { mutableStateOf(timetable.name) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (isCurrent) LocalAppColors.current.textPrimary.copy(alpha = 0.05f) else LocalAppColors.current.surfaceCard,
                RoundedCornerShape(8.dp)
            )
            .border(
                if (isCurrent) 2.dp else 1.dp,
                if (isCurrent) LocalAppColors.current.textPrimary else LocalAppColors.current.border,
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !editing) { if (!isCurrent) onSelect() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (editing) {
                AppTextField(
                    value = editingText,
                    onValueChange = { editingText = it },
                    modifier = Modifier.weight(1f)
                )
                AppIconButton(
                    icon = Icons.Filled.Check,
                    contentDescription = "确认",
                    tint = LocalAppColors.current.success,
                    showBorder = false,
                    onClick = {
                        onRename(editingText)
                        editing = false
                    }
                )
                AppIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "取消",
                    showBorder = false,
                    onClick = {
                        editingText = timetable.name
                        editing = false
                    }
                )
            } else {
                Text(
                    text = timetable.name,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                AppIconButton(
                    icon = Icons.Filled.Edit,
                    contentDescription = "重命名",
                    showBorder = false,
                    onClick = { editing = true }
                )
                if (!isCurrent) {
                    AppIconButton(
                        icon = Icons.Filled.Delete,
                        contentDescription = "删除",
                        tint = LocalAppColors.current.danger,
                        showBorder = false,
                        onClick = onDeleteRequest
                    )
                }
            }
        }
    }
}

