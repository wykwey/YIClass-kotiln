package wykwey.yiclass.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import wykwey.yiclass.presentation.ui.ai.AiConfigScreen
import wykwey.yiclass.presentation.ui.ai.AiEduImportScreen
import wykwey.yiclass.presentation.ui.ai.AiImportScreen
import wykwey.yiclass.presentation.ui.course.CourseEditScreen
import wykwey.yiclass.presentation.ui.home.HomeScreen
import wykwey.yiclass.presentation.ui.repository.EduImportScreen
import wykwey.yiclass.presentation.ui.repository.RepositoryConfigScreen
import wykwey.yiclass.presentation.ui.repository.SchoolSelectScreen
import wykwey.yiclass.presentation.ui.repository.ScriptSelectScreen
import wykwey.yiclass.presentation.ui.settings.AboutScreen
import wykwey.yiclass.presentation.ui.settings.AcademicCalendarScreen
import wykwey.yiclass.presentation.ui.settings.CourseReminderScreen
import wykwey.yiclass.presentation.ui.settings.HelpScreen
import wykwey.yiclass.presentation.ui.settings.ThemeSettingsScreen
import wykwey.yiclass.presentation.ui.settings.TimeSettingsScreen

private const val NAV_ANIM_DURATION = 300

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }
    val goTo: (AppRoute) -> Unit = { route -> navController.navigate(route) }
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home,
        enterTransition = { slideInHorizontally(tween(NAV_ANIM_DURATION)) { it } + fadeIn(tween(NAV_ANIM_DURATION)) },
        exitTransition = { slideOutHorizontally(tween(NAV_ANIM_DURATION)) { -it / 4 } + fadeOut(tween(NAV_ANIM_DURATION)) },
        popEnterTransition = { slideInHorizontally(tween(NAV_ANIM_DURATION)) { -it / 4 } + fadeIn(tween(NAV_ANIM_DURATION)) },
        popExitTransition = { slideOutHorizontally(tween(NAV_ANIM_DURATION)) { it } + fadeOut(tween(NAV_ANIM_DURATION)) }
    ) {
        composable<AppRoute.Home> { HomeScreen(onNavigate = goTo) }
        composable<AppRoute.CourseEdit> { CourseEditScreen(onDone = back) }
        composable<AppRoute.TimeSettings> { TimeSettingsScreen(onDone = back) }
        composable<AppRoute.AcademicCalendar> { AcademicCalendarScreen(onBack = back) }
        composable<AppRoute.ThemeSettings> { ThemeSettingsScreen(onBack = back) }
        composable<AppRoute.CourseReminder> { CourseReminderScreen(onBack = back) }
        composable<AppRoute.About> { AboutScreen(onBack = back) }
        composable<AppRoute.Help> { HelpScreen(onBack = back) }
        composable<AppRoute.RepositoryConfig> { RepositoryConfigScreen(onBack = back) }
        composable<AppRoute.AiConfig> { AiConfigScreen(onBack = back) }
        composable<AppRoute.AiImport> {
            AiImportScreen(onBack = back, onNavigateToAiEduImport = { goTo(AppRoute.AiEduImport) })
        }
        composable<AppRoute.AiEduImport> { AiEduImportScreen(onBack = back) }
        composable<AppRoute.SchoolSelect> {
            SchoolSelectScreen(
                onBack = back,
                onNavigateToRepositoryConfig = { goTo(AppRoute.RepositoryConfig) },
                onSelectSchool = { goTo(AppRoute.ScriptSelect(it.id)) }
            )
        }
        composable<AppRoute.ScriptSelect> {
            ScriptSelectScreen(onBack = back, onSelectScript = { goTo(AppRoute.EduImport(it.url, it.scriptName)) })
        }
        composable<AppRoute.EduImport> { EduImportScreen(onBack = back) }
    }
}
