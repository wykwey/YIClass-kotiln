package wykwey.yiclass.domain.model

/** 仓库类型，对齐原版 repository_config_page 的 official/custom/private。 */
enum class RepositoryType { OFFICIAL, CUSTOM, PRIVATE }

/**
 * 教务脚本仓库配置，对齐原版 RepositoryConfigService：
 * 官方仓库使用固定 URL，自定义/私有仓库需要手动填写仓库与分支、Token。
 */
data class RepositoryConfig(
    val repositoryType: RepositoryType = RepositoryType.OFFICIAL,
    val repositoryUrl: String = "",
    val scriptsBranch: String = "main",
    val tokenKey: String = "",
    val tokenValue: String = "",
    val enabled: Boolean = false
) {

    /** 官方仓库 URL（Gitee），对齐原版 RepositoryConfigService.officialRepositoryUrl。 */
    val effectiveRepositoryUrl: String
        get() = if (repositoryType == RepositoryType.OFFICIAL) OFFICIAL_REPOSITORY_URL else repositoryUrl

    /** 索引文件所在分支，固定为 index-data-sqlite（存放标准 SQLite 格式的 index.db，
     * 与原版 Flutter 的 index-data(index.isar) 分支不同——Kotlin 版不引入 Isar，直接用 SQLiteDatabase 读取）。 */
    val indexBranch: String get() = OFFICIAL_INDEX_BRANCH

    /** 脚本分支：官方仓库固定 main，其余使用输入值。 */
    val effectiveScriptsBranch: String
        get() = if (repositoryType == RepositoryType.OFFICIAL) "main"
                else scriptsBranch.ifBlank { "main" }

    /** 配置是否完整，对齐原版 isValid。 */
    val isValid: Boolean
        get() = when (repositoryType) {
            RepositoryType.OFFICIAL -> true
            RepositoryType.CUSTOM -> repositoryUrl.isNotEmpty()
            RepositoryType.PRIVATE -> repositoryUrl.isNotEmpty() && tokenKey.isNotEmpty() && tokenValue.isNotEmpty()
        }

    companion object {
        const val OFFICIAL_REPOSITORY_URL = "https://gitee.com/ykkzs/YIScripts"
        const val OFFICIAL_INDEX_BRANCH = "index-data-sqlite"
        const val OFFICIAL_INDEX_FILE_NAME = "index.db"
    }
}
