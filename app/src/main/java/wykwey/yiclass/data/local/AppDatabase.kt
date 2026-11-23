package wykwey.yiclass.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import wykwey.yiclass.data.local.dao.ClassTimeDao
import wykwey.yiclass.data.local.dao.CourseDao
import wykwey.yiclass.data.local.dao.CourseScheduleDao
import wykwey.yiclass.data.local.dao.HolidayDao
import wykwey.yiclass.data.local.dao.TimetableDao
import wykwey.yiclass.data.local.entity.ClassTimeEntity
import wykwey.yiclass.data.local.entity.CourseEntity
import wykwey.yiclass.data.local.entity.CourseScheduleEntity
import wykwey.yiclass.data.local.entity.HolidayEntity
import wykwey.yiclass.data.local.entity.TimetableEntity

@Database(
    entities = [
        TimetableEntity::class,
        CourseEntity::class,
        CourseScheduleEntity::class,
        ClassTimeEntity::class,
        HolidayEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun courseDao(): CourseDao
    abstract fun courseScheduleDao(): CourseScheduleDao
    abstract fun classTimeDao(): ClassTimeDao
    abstract fun holidayDao(): HolidayDao
}
