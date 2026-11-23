package wykwey.yiclass.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object About : AppRoute
    @Serializable data object Help : AppRoute
    @Serializable data object ThemeSettings : AppRoute
    @Serializable data object CourseReminder : AppRoute
    @Serializable data object RepositoryConfig : AppRoute
    @Serializable data object SchoolSelect : AppRoute
    @Serializable data object AiConfig : AppRoute
    @Serializable data object AiImport : AppRoute
    @Serializable data object AiEduImport : AppRoute

    @Serializable data class TimeSettings(val timetableId: Long) : AppRoute
    @Serializable data class AcademicCalendar(val timetableId: Long) : AppRoute
    @Serializable data class ScriptSelect(val schoolId: Long) : AppRoute
    @Serializable data class EduImport(val scriptUrl: String, val scriptName: String) : AppRoute

    @Serializable
    data class CourseEdit(
        val timetableId: Long,
        val courseId: Long = 0,
        val weekday: Int = 1,
        val period: Int = 0,
        val week: Int = 0
    ) : AppRoute
}
