package wykwey.yiclass.presentation.ui.components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 通用输入框，对齐原 Flutter 版 YicoreTextField：
 * label 是框外上方静态文字（可选），hint 是框内灰色提示，白底圆角边框，
 * 错误态描边/提示文字变红。
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val colors = LocalAppColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it, color = colors.textDisabled) } },
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            enabled = enabled,
            isError = isError,
            supportingText = errorText?.let { { Text(it) } },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceCard,
                unfocusedContainerColor = colors.surfaceCard,
                disabledContainerColor = colors.surfaceTint,
                focusedBorderColor = if (isError) colors.dangerSoft else colors.textPrimary,
                unfocusedBorderColor = if (isError) colors.dangerSoft else colors.divider
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
