package wykwey.yiclass.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import wykwey.yiclass.data.local.entity.CourseScheduleEntity

@Dao
interface CourseScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: CourseScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<CourseScheduleEntity>): List<Long>

    @Update
    suspend fun update(schedule: CourseScheduleEntity)

    @Delete
    suspend fun delete(schedule: CourseScheduleEntity)

    @Query("DELETE FROM course_schedules WHERE courseId = :courseId")
    suspend fun deleteByCourse(courseId: Long)
}
