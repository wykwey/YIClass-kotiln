package wykwey.yiclass.data.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import wykwey.yiclass.MainActivity
import wykwey.yiclass.R
import wykwey.yiclass.domain.repository.SettingsRepository
import wykwey.yiclass.domain.timetable.TimetableSelection
import wykwey.yiclass.domain.util.ReminderPlanner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val selection: TimetableSelection
) {
    private val alarms = context.getSystemService(AlarmManager::class.java)
    private val alarmState = context.getSharedPreferences("course_reminder_alarms", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeIn(scope: CoroutineScope) = scope.launch {
        settings.courseReminderEnabled.distinctUntilChanged().flatMapLatest { enabled ->
            updatePeriodicRefresh(enabled)
            if (enabled) selection.timetable else flowOf(null)
        }.distinctUntilChanged().collect {
            try {
                refresh()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.e("CourseReminder", "Could not update course alarms", failure)
                enqueueRefresh()
            }
        }
    }

    fun enqueueRefresh() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            REFRESH_WORK,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ReminderRefreshWorker>().build()
        )
    }

    private fun updatePeriodicRefresh(enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            workManager.enqueueUniquePeriodicWork(
                PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<ReminderRefreshWorker>(Duration.ofDays(1)).build()
            )
        } else {
            workManager.cancelUniqueWork(PERIODIC_WORK)
        }
    }

    suspend fun refresh() = mutex.withLock {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val timetable = activeTimetable()
        // 提前提醒可能跨午夜。预排四天，允许 WorkManager 因设备休眠延后执行。
        val upcoming = timetable?.let { current ->
            (0L..3L).flatMap { ReminderPlanner.forDate(current, today.plusDays(it), zone) }
                .map { it.remindsAt }.filter { it > now }
                .map { it.toEpochMilli().toString() }.toSet()
        }.orEmpty()
        val previous = alarmState.getStringSet(KEY_ACTIVE_ALARMS, emptySet()).orEmpty()
        (previous - upcoming).forEach { timestamp ->
            alarmIntent(timestamp, PendingIntent.FLAG_NO_CREATE)?.let { alarm ->
                alarms.cancel(alarm)
                alarm.cancel()
            }
        }
        alarmState.edit().putStringSet(KEY_ACTIVE_ALARMS, upcoming).apply()
        upcoming.forEach { timestamp ->
            val alarm = checkNotNull(alarmIntent(timestamp, PendingIntent.FLAG_UPDATE_CURRENT))
            schedule(timestamp.toLong(), alarm)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun notifyAt(timestamp: Long) = mutex.withLock {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
        val timetable = activeTimetable() ?: return@withLock
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return@withLock
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.course_reminder_channel), NotificationManager.IMPORTANCE_HIGH)
        )
        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val due = (0L..2L).flatMap { ReminderPlanner.forDate(timetable, date.plusDays(it), zone) }
            .filter { it.remindsAt.toEpochMilli() == timestamp && now <= it.startsAt.plusSeconds(60) }
        due.forEach { reminder ->
            val time = reminder.startsAt.atZone(zone).toLocalTime()
            val body = listOf(time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), reminder.location)
                .filter(String::isNotBlank).joinToString(" · ")
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_course_reminder)
                .setContentTitle(reminder.courseName)
                .setContentText(body)
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            manager.notify(reminder.tag, 0, notification)
        }
    }

    private suspend fun activeTimetable() = if (settings.courseReminderEnabled.first()) {
        selection.load()
    } else {
        null
    }

    private fun schedule(at: Long, alarm: PendingIntent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()) {
            try {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, alarm)
                return
            } catch (_: SecurityException) {
                // 权限可能在检查之后被撤回。
            }
        }
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, alarm)
    }

    private fun alarmIntent(timestamp: String, flags: Int): PendingIntent? = PendingIntent.getBroadcast(
        context, 0,
        Intent(context, CourseReminderReceiver::class.java).apply {
            action = ACTION_REMIND
            data = Uri.parse("yiclass://course-reminder/$timestamp")
        },
        flags or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val ACTION_REMIND = "wykwey.yiclass.action.COURSE_REMINDER"
        private const val KEY_ACTIVE_ALARMS = "active_alarms"
        private const val CHANNEL_ID = "course_reminders"
        private const val REFRESH_WORK = "course-reminder-refresh"
        private const val PERIODIC_WORK = "course-reminder-refresh-daily"
    }
}
