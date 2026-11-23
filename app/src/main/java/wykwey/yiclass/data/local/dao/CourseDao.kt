package wykwey.yiclass.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import wykwey.yiclass.data.local.entity.CourseEntity
import wykwey.yiclass.data.local.relation.CourseWithSchedules

@Dao
interface CourseDao {

    @Transaction
    @Query("SELECT * FROM courses WHERE timetableId = :timetableId ORDER BY id")
    fun observeByTimetable(timetableId: Long): Flow<List<CourseWithSchedules>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getWithSchedules(id: Long): CourseWithSchedules?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM courses WHERE timetableId = :timetableId")
    suspend fun deleteByTimetable(timetableId: Long)
}
