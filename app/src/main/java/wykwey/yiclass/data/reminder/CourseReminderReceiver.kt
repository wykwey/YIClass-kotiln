package wykwey.yiclass.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@AndroidEntryPoint
class CourseReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduler: CourseReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != CourseReminderScheduler.ACTION_REMIND) {
            scheduler.enqueueRefresh()
            return
        }
        val timestamp = intent.data?.lastPathSegment?.toLongOrNull() ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(8_000) { scheduler.notifyAt(timestamp) }
            } catch (failure: Exception) {
                Log.e("CourseReminder", "Could not show course reminder", failure)
            } finally {
                pending.finish()
            }
        }
    }
}
