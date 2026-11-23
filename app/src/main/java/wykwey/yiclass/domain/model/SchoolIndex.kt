package wykwey.yiclass.domain.model

/** 单条脚本信息，对齐官方索引仓库 script_entry 表结构。 */
data class ScriptEntry(
    val name: String,
    val scriptName: String,
    val url: String,
    val author: String,
    val description: String
)

/** 单个学校及其可用脚本列表，对齐官方索引仓库 school_entry 表结构。 */
data class SchoolEntry(
    val id: Long,
    val school: String,
    val letterIndex: String,
    val scripts: List<ScriptEntry>
)
