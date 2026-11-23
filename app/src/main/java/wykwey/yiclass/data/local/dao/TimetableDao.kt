package wykwey.yiclass.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import wykwey.yiclass.data.local.entity.TimetableEntity
import wykwey.yiclass.data.local.relation.TimetableWithDetails

@Dao
interface TimetableDao {

    @Query("SELECT * FROM timetables ORDER BY id")
    fun observeAll(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetables WHERE id = :id")
    suspend fun getById(id: Long): TimetableEntity?

    /** 课表及其全部课程/节次/节假日，随数据变化持续发射。 */
    @Transaction
    @Query("SELECT * FROM timetables WHERE id = :id")
    fun observeDetailsById(id: Long): Flow<TimetableWithDetails?>

    @Transaction
    @Query("SELECT * FROM timetables WHERE id = :id")
    suspend fun getDetailsById(id: Long): TimetableWithDetails?

    @Insert
    suspend fun insert(timetable: TimetableEntity): Long

    @Query("DELETE FROM timetables WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE timetables SET name = :name WHERE id = :id")
    suspend fun renameById(id: Long, name: String)

    @Query("UPDATE timetables SET reminderMinutes = :minutes WHERE id = :id")
    suspend fun updateReminderMinutes(id: Long, minutes: Int)

    @Query("UPDATE timetables SET showWeekend = :show WHERE id = :id")
    suspend fun updateShowWeekend(id: Long, show: Boolean)

    @Query("UPDATE timetables SET startDate = :date WHERE id = :id")
    suspend fun updateStartDate(id: Long, date: String)

    @Query("UPDATE timetables SET totalWeeks = :weeks, maxPeriods = :periods WHERE id = :id")
    suspend fun updateTimeSettings(id: Long, weeks: Int, periods: Int): Int
}
