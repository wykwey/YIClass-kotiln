package wykwey.yiclass.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import wykwey.yiclass.domain.model.RepositoryConfig
import wykwey.yiclass.domain.model.RepositoryType
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private data class RepoInfo(val owner: String, val repo: String, val platform: String)

/**
 * 仓库下载服务，对齐原版 lib/services/repository/repository_download_service.dart：
 * 下载官方/自定义/私有仓库的索引文件（index.db，SQLite 格式）与 scripts 目录下的脚本文件。
 *
 * 与原版差异：官方索引改用 index-data-sqlite 分支的标准 SQLite index.db（原版是 index-data 分支的 index.isar），
 * Kotlin 项目不引入 Isar 依赖，直接用 [android.database.sqlite.SQLiteDatabase] 读取。
 */
@Singleton
class RepositoryDownloadService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    class DownloadException(message: String) : Exception(message)

    fun repositoryDir(): File = File(context.filesDir, "repository")
    fun scriptsDir(): File = File(repositoryDir(), "scripts")
    fun indexFile(): File = File(repositoryDir(), RepositoryConfig.OFFICIAL_INDEX_FILE_NAME)

    /** 下载索引文件（index.db），返回本地文件路径。 */
    suspend fun downloadIndex(config: RepositoryConfig): File = withContext(Dispatchers.IO) {
        val repoInfo = parseRepositoryUrl(config.effectiveRepositoryUrl)
            ?: throw DownloadException("无效的仓库URL格式")

        val bytes = downloadFile(
            repoInfo = repoInfo,
            branch = config.indexBranch,
            filePath = RepositoryConfig.OFFICIAL_INDEX_FILE_NAME,
            tokenKey = if (config.repositoryType == RepositoryType.PRIVATE) config.tokenKey else null,
            tokenValue = if (config.repositoryType == RepositoryType.PRIVATE) config.tokenValue else null
        ) ?: throw DownloadException("下载索引文件失败")

        val dir = repositoryDir()
        if (!dir.exists()) dir.mkdirs()
        val file = indexFile()
        file.writeBytes(bytes)
        file
    }

    /** 下载指定脚本文件列表到本地缓存目录，返回下载成功的数量。 */
    suspend fun downloadScripts(config: RepositoryConfig, scriptNames: List<String>): Int = withContext(Dispatchers.IO) {
        if (scriptNames.isEmpty()) return@withContext 0
        val repoInfo = parseRepositoryUrl(config.effectiveRepositoryUrl)
            ?: throw DownloadException("无效的仓库URL格式")

        val tokenKey = if (config.repositoryType == RepositoryType.PRIVATE) config.tokenKey else null
        val tokenValue = if (config.repositoryType == RepositoryType.PRIVATE) config.tokenValue else null

        val dir = scriptsDir()
        if (!dir.exists()) dir.mkdirs()

        var successCount = 0
        for (scriptName in scriptNames) {
            try {
                val bytes = downloadFile(
                    repoInfo = repoInfo,
                    branch = config.effectiveScriptsBranch,
                    filePath = "scripts/$scriptName",
                    tokenKey = tokenKey,
                    tokenValue = tokenValue
                )
                if (bytes != null) {
                    File(dir, scriptName).writeBytes(bytes)
                    successCount++
                }
            } catch (e: Exception) {
                // 单个文件失败不影响其余文件下载，对齐原版行为
            }
        }
        successCount
    }

    /** 下载单个文件字节内容：公开仓库走 raw URL，私有仓库走 API + base64 解码。 */
    private fun downloadFile(
        repoInfo: RepoInfo,
        branch: String,
        filePath: String,
        tokenKey: String?,
        tokenValue: String?
    ): ByteArray? {
        if (tokenKey.isNullOrEmpty() || tokenValue.isNullOrEmpty()) {
            val rawUrl = buildRawUrl(repoInfo, branch, filePath)
            val request = Request.Builder().url(rawUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw DownloadException("下载文件失败: ${response.code}")
                return response.body.bytes()
            }
        } else {
            val apiUrl = buildContentsApiUrl(repoInfo, filePath, branch)
            val requestBuilder = Request.Builder().url(apiUrl)
            buildHeaders(tokenKey, tokenValue, repoInfo.platform).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) throw DownloadException("获取文件失败: ${response.code}")
                val bodyText = response.body.string()
                val obj = json.parseToJsonElement(bodyText).jsonObject
                val encoding = obj["encoding"]?.jsonPrimitive?.content
                val content = obj["content"]?.jsonPrimitive?.content
                if (encoding == "base64" && content != null) {
                    return android.util.Base64.decode(content.replace("\n", ""), android.util.Base64.DEFAULT)
                }
                throw DownloadException("不支持的文件编码格式")
            }
        }
    }

    private fun buildRawUrl(repoInfo: RepoInfo, branch: String, filePath: String): String = when (repoInfo.platform) {
        "github" -> "https://raw.githubusercontent.com/${repoInfo.owner}/${repoInfo.repo}/$branch/$filePath"
        "gitee" -> "https://gitee.com/${repoInfo.owner}/${repoInfo.repo}/raw/$branch/$filePath"
        else -> throw DownloadException("不支持的平台: ${repoInfo.platform}")
    }

    private fun buildContentsApiUrl(repoInfo: RepoInfo, filePath: String, branch: String): String = when (repoInfo.platform) {
        "github" -> "https://api.github.com/repos/${repoInfo.owner}/${repoInfo.repo}/contents/$filePath?ref=$branch"
        "gitee" -> "https://gitee.com/api/v5/repos/${repoInfo.owner}/${repoInfo.repo}/contents/$filePath?ref=$branch"
        else -> throw DownloadException("不支持的平台: ${repoInfo.platform}")
    }

    private fun buildHeaders(tokenKey: String, tokenValue: String, platform: String): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        if (platform == "github") headers["Accept"] = "application/vnd.github.v3+json"
        if (tokenKey.lowercase() == "authorization" || tokenKey.lowercase() == "token") {
            headers["Authorization"] = if (platform == "gitee") "Bearer $tokenValue" else "token $tokenValue"
        } else {
            headers[tokenKey] = tokenValue
        }
        return headers
    }

    /** 解析 GitHub/Gitee 仓库 URL，提取 owner/repo/platform。 */
    private fun parseRepositoryUrl(url: String): RepoInfo? {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host ?: return null
            val platform = when {
                host == "github.com" -> "github"
                host == "gitee.com" -> "gitee"
                else -> return null
            }
            val segments = uri.path.trim('/').split("/")
            if (segments.size < 2) return null
            var repo = segments[1]
            if (repo.endsWith(".git")) repo = repo.removeSuffix(".git")
            RepoInfo(owner = segments[0], repo = repo, platform = platform)
        } catch (e: Exception) {
            null
        }
    }
}
