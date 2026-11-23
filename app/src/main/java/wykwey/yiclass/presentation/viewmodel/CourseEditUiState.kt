package wykwey.yiclass.presentation.viewmodel

/** 一条可编辑的时间段（周次/节次以原始文本保存，保存时再解析）。新建时空文本，输入框显示 placeholder 提示。 */
data class ScheduleInput(
    val weekday: Int = 1,
    val weekPatternText: String = "",
    val periodsText: String = ""
)

data class CourseEditUiState(
    val isNew: Boolean = true,
    val name: String = "",
    val location: String = "",
    val teacher: String = "",
    val color: Int = 0,
    val schedules: List<ScheduleInput> = listOf(ScheduleInput()),
    val loading: Boolean = true,
    val saved: Boolean = false,       // 保存/删除完成，界面据此返回
    val nameError: String? = null,    // 课程名称必填校验，内嵌在输入框下方
    val error: String? = null         // 解析/保存失败，走 sonner 风格浮层提示
)
