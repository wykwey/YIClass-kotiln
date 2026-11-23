package wykwey.yiclass.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wykwey.yiclass.domain.model.SchoolEntry
import wykwey.yiclass.domain.model.ScriptEntry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 学校索引服务，对齐原版 lib/services/repository/school_index_service.dart：
 * 读取下载好的 index.db（标准 SQLite，school_index/school_entry/script_entry 三张表）。
 *
 * 与原版差异：原版用 Isar 打开 index.isar；Kotlin 版索引改为官方仓库 index-data-sqlite 分支的
 * index.db，直接用 [SQLiteDatabase] 只读打开，无需额外 ORM 依赖。
 */
@Singleton
class SchoolIndexService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadService: RepositoryDownloadService
) {

    /** 打开索引数据库并读取全部学校（含各自脚本列表）；索引文件不存在时返回空列表。 */
    suspend fun getAllSchools(): List<SchoolEntry> = withContext(Dispatchers.IO) {
        val file = downloadService.indexFile()
        if (!file.exists()) return@withContext emptyList()

        val db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
        try {
            val schools = mutableListOf<SchoolEntry>()
            db.rawQuery("SELECT id, school, letter_index FROM school_entry ORDER BY letter_index, school", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val school = cursor.getString(1)
                    val letterIndex = cursor.getString(2)
                    val scripts = getScriptsForSchool(db, id)
                    schools.add(SchoolEntry(id = id, school = school, letterIndex = letterIndex, scripts = scripts))
                }
            }
            schools
        } finally {
            db.close()
        }
    }

    private fun getScriptsForSchool(db: SQLiteDatabase, schoolEntryId: Long): List<ScriptEntry> {
        val scripts = mutableListOf<ScriptEntry>()
        db.rawQuery(
            "SELECT name, script_name, url, author, description FROM script_entry WHERE school_entry_id = ?",
            arrayOf(schoolEntryId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                scripts.add(
                    ScriptEntry(
                        name = cursor.getString(0),
                        scriptName = cursor.getString(1),
                        url = cursor.getString(2),
                        author = cursor.getString(3),
                        description = cursor.getString(4)
                    )
                )
            }
        }
        return scripts
    }

    /** 按字母分组，对齐原版 getSchoolsGroupedByLetter。 */
    suspend fun getSchoolsGroupedByLetter(): Map<String, List<SchoolEntry>> {
        val schools = getAllSchools()
        return schools.groupBy { it.letterIndex.uppercase() }
            .toSortedMap()
    }

    /** 按名称模糊搜索学校，对齐原版 searchSchools。 */
    suspend fun searchSchools(query: String): List<SchoolEntry> {
        val lower = query.lowercase()
        return getAllSchools().filter { it.school.lowercase().contains(lower) }
    }

    fun isIndexDownloaded(): Boolean = downloadService.indexFile().exists()

    /** 删除索引文件 */
    suspend fun deleteIndex(): Boolean = withContext(Dispatchers.IO) {
        val file = downloadService.indexFile()
        if (file.exists()) file.delete() else false
    }

    /** 根据 id 取单个学校（含脚本列表），用于 ScriptSelectScreen 按 id 定位（Compose 导航参数仅传递基本类型）。 */
    suspend fun getSchoolById(id: Long): SchoolEntry? = getAllSchools().find { it.id == id }
}
