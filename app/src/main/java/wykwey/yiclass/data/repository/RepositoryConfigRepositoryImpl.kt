package wykwey.yiclass.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wykwey.yiclass.domain.model.RepositoryConfig
import wykwey.yiclass.domain.model.RepositoryType
import wykwey.yiclass.domain.repository.RepositoryConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

private val Context.repositoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "repository_config")

@Singleton
class RepositoryConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RepositoryConfigRepository {

    private object Keys {
        val REPOSITORY_TYPE = stringPreferencesKey("repository_type")
        val REPOSITORY_URL = stringPreferencesKey("repository_url")
        val SCRIPTS_BRANCH = stringPreferencesKey("scripts_branch")
        val TOKEN_KEY = stringPreferencesKey("token_key")
        val TOKEN_VALUE = stringPreferencesKey("token_value")
        val ENABLED = booleanPreferencesKey("repository_enabled")
    }

    override val config: Flow<RepositoryConfig> =
        context.repositoryDataStore.data.map { prefs ->
            RepositoryConfig(
                repositoryType = when (prefs[Keys.REPOSITORY_TYPE]) {
                    "custom" -> RepositoryType.CUSTOM
                    "private" -> RepositoryType.PRIVATE
                    else -> RepositoryType.OFFICIAL
                },
                repositoryUrl = prefs[Keys.REPOSITORY_URL] ?: "",
                scriptsBranch = prefs[Keys.SCRIPTS_BRANCH] ?: "main",
                tokenKey = prefs[Keys.TOKEN_KEY] ?: "",
                tokenValue = prefs[Keys.TOKEN_VALUE] ?: "",
                enabled = prefs[Keys.ENABLED] ?: false
            )
        }

    override suspend fun save(config: RepositoryConfig) {
        context.repositoryDataStore.edit { prefs ->
            prefs[Keys.REPOSITORY_TYPE] = when (config.repositoryType) {
                RepositoryType.OFFICIAL -> "official"
                RepositoryType.CUSTOM -> "custom"
                RepositoryType.PRIVATE -> "private"
            }
            // 官方仓库不保存 URL（使用固定值），对齐原版
            if (config.repositoryType != RepositoryType.OFFICIAL) {
                prefs[Keys.REPOSITORY_URL] = config.repositoryUrl
            }
            prefs[Keys.SCRIPTS_BRANCH] = config.scriptsBranch
            prefs[Keys.TOKEN_KEY] = config.tokenKey
            prefs[Keys.TOKEN_VALUE] = config.tokenValue
            prefs[Keys.ENABLED] = config.enabled
        }
    }
}
