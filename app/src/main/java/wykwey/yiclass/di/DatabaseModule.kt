package wykwey.yiclass.di

import android.content.Context
import androidx.room3.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import wykwey.yiclass.data.local.AppDatabase
import wykwey.yiclass.data.local.dao.ClassTimeDao
import wykwey.yiclass.data.local.dao.CourseDao
import wykwey.yiclass.data.local.dao.CourseScheduleDao
import wykwey.yiclass.data.local.dao.HolidayDao
import wykwey.yiclass.data.local.dao.TimetableDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "yiclass.db"
        ).build()
    }

    @Provides
    fun provideTimetableDao(db: AppDatabase): TimetableDao = db.timetableDao()

    @Provides
    fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao()

    @Provides
    fun provideCourseScheduleDao(db: AppDatabase): CourseScheduleDao = db.courseScheduleDao()

    @Provides
    fun provideClassTimeDao(db: AppDatabase): ClassTimeDao = db.classTimeDao()

    @Provides
    fun provideHolidayDao(db: AppDatabase): HolidayDao = db.holidayDao()
}
