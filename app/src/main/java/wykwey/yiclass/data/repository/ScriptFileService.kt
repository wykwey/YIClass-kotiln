package wykwey.yiclass.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 脚本文件服务，对齐原版 lib/services/repository/script_file_service.dart：
 * 检查/读取已下载到本地的教务导入脚本文件。
 */
@Singleton
class ScriptFileService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadService: RepositoryDownloadService
) {

    suspend fun checkScriptExists(scriptName: String): Boolean = withContext(Dispatchers.IO) {
        File(downloadService.scriptsDir(), scriptName).exists()
    }

    suspend fun checkScriptsExist(scriptNames: List<String>): Map<String, Boolean> = withContext(Dispatchers.IO) {
        val dir = downloadService.scriptsDir()
        scriptNames.associateWith { name -> File(dir, name).exists() }
    }

    suspend fun readScriptContent(scriptName: String): String? = withContext(Dispatchers.IO) {
        val file = File(downloadService.scriptsDir(), scriptName)
        if (!file.exists()) null else file.readText()
    }

    /** 删除单个脚本文件 */
    suspend fun deleteScript(scriptName: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(downloadService.scriptsDir(), scriptName)
        if (file.exists()) file.delete() else false
    }

    /** 删除全部脚本文件 */
    suspend fun deleteAllScripts(): Int = withContext(Dispatchers.IO) {
        val dir = downloadService.scriptsDir()
        val files = dir.listFiles() ?: return@withContext 0
        var count = 0
        files.forEach { if (it.isFile && it.delete()) count++ }
        count
    }
}
