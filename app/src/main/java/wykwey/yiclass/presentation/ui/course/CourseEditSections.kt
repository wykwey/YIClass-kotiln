package wykwey.yiclass.presentation.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wykwey.yiclass.presentation.theme.CourseColors
import wykwey.yiclass.presentation.ui.components.inputs.AppTextField
import wykwey.yiclass.presentation.ui.components.layout.SectionCard

@Composable
fun BasicInfoSection(
    name: String,
    location: String,
    teacher: String,
    nameError: String?,
    onNameChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onTeacherChange: (String) -> Unit
) {
    SectionCard("基本信息") {
        Column {
            AppTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = "如: 高等数学",
                isError = nameError != null,
                errorText = nameError
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    placeholder = "如: 教学楼A101",
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = teacher,
                    onValueChange = onTeacherChange,
                    placeholder = "如: 张老师",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorSection(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    SectionCard("显示设置") {
        Column {
            Text("选择课程颜色", fontWeight = FontWeight.SemiBold)
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CourseColors.palette.forEach { color ->
                    val selected = selectedColor == color.toArgbInt()
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color, CircleShape)
                            .then(
                                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { onColorSelected(color.toArgbInt()) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = CourseColors.textColorFor(color)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Compose Color → ARGB Int（与 course.color 存储一致）。 */
private fun Color.toArgbInt(): Int = this.toArgb()
