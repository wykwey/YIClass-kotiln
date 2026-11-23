package wykwey.yiclass.domain.model

data class Timetable(
    val id: Long = 0,
    val name: String,
    val settings: TimetableSettings,
    val courses: List<Course> = emptyList()
)
