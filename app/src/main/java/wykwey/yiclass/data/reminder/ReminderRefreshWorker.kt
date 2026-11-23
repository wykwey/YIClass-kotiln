package wykwey.yiclass.data.reminder

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class ReminderRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val scheduler: CourseReminderScheduler
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        scheduler.refresh()
        Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        Log.e("CourseReminder", "Could not refresh course alarms", failure)
        Result.retry()
    }
}
