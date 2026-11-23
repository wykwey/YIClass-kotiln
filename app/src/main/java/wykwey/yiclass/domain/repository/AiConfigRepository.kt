package wykwey.yiclass.domain.repository

import kotlinx.coroutines.flow.Flow
import wykwey.yiclass.domain.model.AiConfig

/** AI 导入配置仓储，由 DataStore 持久化。 */
interface AiConfigRepository {

    val config: Flow<AiConfig>

    suspend fun save(config: AiConfig)
}
