package wykwey.yiclass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import wykwey.yiclass.presentation.navigation.AppNavGraph
import wykwey.yiclass.presentation.theme.LocalDisableAdaptiveFontColor
import wykwey.yiclass.presentation.theme.YIClassTheme
import wykwey.yiclass.presentation.viewmodel.ThemeSettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeSettingsViewModel = hiltViewModel()
            val theme by themeViewModel.uiState.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalDisableAdaptiveFontColor provides theme.disableAdaptiveFontColor
            ) {
                YIClassTheme(themeMode = theme.themeMode) {
                    AppNavGraph()
                }
            }
        }
    }
}
