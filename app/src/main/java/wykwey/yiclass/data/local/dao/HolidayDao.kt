package wykwey.yiclass.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import wykwey.yiclass.data.local.entity.HolidayEntity

@Dao
interface HolidayDao {

    @Query("SELECT date FROM holidays WHERE timetableId = :timetableId")
    suspend fun dates(timetableId: Long): List<String>

    @Insert
    suspend fun insert(holiday: HolidayEntity)

    @Insert
    suspend fun insertAll(holidays: List<HolidayEntity>)

    @Query("DELETE FROM holidays WHERE timetableId = :timetableId AND date = :date")
    suspend fun deleteByDate(timetableId: Long, date: String)
}
