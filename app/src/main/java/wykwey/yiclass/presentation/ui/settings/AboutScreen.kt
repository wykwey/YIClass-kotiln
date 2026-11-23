package wykwey.yiclass.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.BuildConfig
import wykwey.yiclass.presentation.ui.components.layout.AppCard
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.ui.components.layout.SettingsItem
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar

private val PageBackground: Color
    @Composable get() = LocalAppColors.current.pageBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = PageBackground,
        topBar = {
            YIClassAppBar(
                title = "关于应用",
                centerTitle = true,
                showBack = true,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppCard(contentPadding = 24.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("YIClass", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "智能课程表应用",
                            fontSize = 14.sp,
                            color = LocalAppColors.current.textSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item {
                SettingsBlock(title = "版本信息") {
                    SettingsItem(title = "当前版本", value = "v${BuildConfig.VERSION_NAME}")
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("© 2026 wykwe", fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
                    Text(
                        "Android 原生版",
                        fontSize = 13.sp,
                        color = LocalAppColors.current.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
