package wykwey.yiclass.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import wykwey.yiclass.data.local.entity.ClassTimeEntity

@Dao
interface ClassTimeDao {

    @Query("SELECT * FROM class_times WHERE timetableId = :timetableId ORDER BY period")
    fun observeByTimetable(timetableId: Long): Flow<List<ClassTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classTimes: List<ClassTimeEntity>): List<Long>

    @Update
    suspend fun update(classTime: ClassTimeEntity)

    @Delete
    suspend fun delete(classTime: ClassTimeEntity)

    @Query("DELETE FROM class_times WHERE timetableId = :timetableId")
    suspend fun deleteByTimetable(timetableId: Long)
}
