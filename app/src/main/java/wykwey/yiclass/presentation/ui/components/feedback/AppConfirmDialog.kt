package wykwey.yiclass.presentation.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import wykwey.yiclass.presentation.ui.components.inputs.AppButton
import wykwey.yiclass.presentation.ui.components.inputs.AppButtonVariant
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 通用确认对话框，对齐原 Flutter 版 YicoreConfirm：
 * 白底圆角卡片 + 标题 + 消息 + 取消/确认双按钮（取消为描边，确认为黑底）。
 */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "确定",
    cancelText: String = "取消"
) {
    Dialog(onDismissRequest = onDismiss) {
        AppCard {
            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, fontSize = 14.sp, color = LocalAppColors.current.textSecondary, lineHeight = 20.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppButton(
                        text = cancelText,
                        variant = AppButtonVariant.Outlined,
                        onClick = onDismiss
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppButton(
                        text = confirmText,
                        variant = AppButtonVariant.Primary,
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}
