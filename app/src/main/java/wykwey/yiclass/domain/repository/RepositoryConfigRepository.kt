package wykwey.yiclass.domain.repository

import kotlinx.coroutines.flow.Flow
import wykwey.yiclass.domain.model.RepositoryConfig

/** 教务脚本仓库配置仓储，由 DataStore 持久化。 */
interface RepositoryConfigRepository {

    val config: Flow<RepositoryConfig>

    suspend fun save(config: RepositoryConfig)
}
