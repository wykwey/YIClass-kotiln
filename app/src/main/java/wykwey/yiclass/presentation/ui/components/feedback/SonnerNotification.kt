package wykwey.yiclass.presentation.ui.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import wykwey.yiclass.presentation.theme.LocalAppColors

/**
 * 对齐原 Flutter 版 Notifications.sonner：白底卡片 + 浅灰描边 + 底部浮层，
 * 不是 Material 的深色 SnackBar/Toast。
 */
@Composable
fun SonnerHost(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(3000)
            onDismiss()
        }
    }
    Box(modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalAppColors.current.surfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, LocalAppColors.current.outlineStrong, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = message.orEmpty(),
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textPrimary
                )
            }
        }
    }
}
