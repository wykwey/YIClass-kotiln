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
import wykwey.yiclass.domain.model.AiConfig
import wykwey.yiclass.domain.repository.AiConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aiDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_config")

@Singleton
class AiConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AiConfigRepository {

    private object Keys {
        val API_KEY = stringPreferencesKey("ai_api_key")
        val ENDPOINT = stringPreferencesKey("ai_endpoint")
        val VISION_MODEL = stringPreferencesKey("ai_vision_model")
        val TEXT_MODEL = stringPreferencesKey("ai_text_model")
        val IMAGE_IMPORT = booleanPreferencesKey("ai_image_import")
        val TABLE_IMPORT = booleanPreferencesKey("ai_table_import")
        val TEXT_IMPORT = booleanPreferencesKey("ai_text_import")
        val WEB_IMPORT = booleanPreferencesKey("ai_web_import")
    }

    override val config: Flow<AiConfig> =
        context.aiDataStore.data.map { prefs ->
            AiConfig(
                apiKey = prefs[Keys.API_KEY] ?: "",
                endpoint = prefs[Keys.ENDPOINT] ?: "",
                visionModel = prefs[Keys.VISION_MODEL] ?: AiConfig.DEFAULT_MODEL,
                textModel = prefs[Keys.TEXT_MODEL] ?: AiConfig.DEFAULT_MODEL,
                enableImageImport = prefs[Keys.IMAGE_IMPORT] ?: false,
                enableTableImport = prefs[Keys.TABLE_IMPORT] ?: false,
                enableTextImport = prefs[Keys.TEXT_IMPORT] ?: false,
                enableWebImport = prefs[Keys.WEB_IMPORT] ?: false
            )
        }

    override suspend fun save(config: AiConfig) {
        context.aiDataStore.edit { prefs ->
            prefs[Keys.API_KEY] = config.apiKey
            prefs[Keys.ENDPOINT] = config.endpoint
            prefs[Keys.VISION_MODEL] = config.visionModel
            prefs[Keys.TEXT_MODEL] = config.textModel
            prefs[Keys.IMAGE_IMPORT] = config.enableImageImport
            prefs[Keys.TABLE_IMPORT] = config.enableTableImport
            prefs[Keys.TEXT_IMPORT] = config.enableTextImport
            prefs[Keys.WEB_IMPORT] = config.enableWebImport
        }
    }
}
