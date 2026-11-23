package wykwey.yiclass.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wykwey.yiclass.presentation.theme.LocalAppColors
import wykwey.yiclass.presentation.ui.components.layout.SettingsBlock
import wykwey.yiclass.presentation.ui.components.layout.SettingsItem
import wykwey.yiclass.presentation.ui.components.layout.YIClassAppBar

@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = LocalAppColors.current.pageBackground,
        topBar = { YIClassAppBar(title = "使用帮助", centerTitle = true, showBack = true, onBack = onBack) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SettingsBlock(title = "设置课表") {
                    SettingsItem("添加和编辑课程", "点周视图的空白节次添加课程。点课程卡片可编辑。")
                    SettingsItem("设置学期和调休", "在设置中选择开学日、总周数和节次时间。停课与补课日期在「节假日与补课」中设置。")
                }
            }
            item {
                SettingsBlock(title = "导入和备份") {
                    SettingsItem("备份课表", "在课表管理中导出当前课表。通过导入课表把备份添加到课表列表。")
                    SettingsItem("导入教务或 AI 课表", "启用高级功能后，可配置教务脚本仓库或 AI 接口，再从课表管理开始导入。")
                }
            }
            item {
                SettingsBlock(title = "课程提醒") {
                    SettingsItem("开启提醒", "在课程提醒中选择提前时间，并允许通知。精确闹钟权限影响提醒的准时程度。")
                }
            }
        }
    }
}
