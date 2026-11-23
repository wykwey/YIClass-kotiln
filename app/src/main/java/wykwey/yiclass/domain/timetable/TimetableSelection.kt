package wykwey.yiclass.domain.timetable

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.repository.SettingsRepository
import wykwey.yiclass.domain.repository.TimetableRepository
import wykwey.yiclass.domain.util.TimetableDefaults

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class TimetableSelection @Inject constructor(
    private val repository: TimetableRepository,
    private val settings: SettingsRepository
) {
    private val initialized = CompletableDeferred<Unit>()

    val selectedId = settings.currentTimetableId.distinctUntilChanged()
    val timetable = selectedId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeTimetableDetails(id)
    }.onStart { initialized.await() }

    suspend fun initialize() {
        val timetables = repository.observeTimetables().first()
        val current = selectedId.first()
        when {
            timetables.isEmpty() -> addAndSelect(Timetable(name = "默认课表", settings = TimetableDefaults.defaultSettings()))
            timetables.none { it.id == current } -> select(timetables.first().id)
        }
        initialized.complete(Unit)
    }

    suspend fun load(): Timetable? {
        initialized.await()
        return selectedId.first()?.let { repository.getTimetableDetails(it) }
    }

    suspend fun select(id: Long?) = settings.setCurrentTimetableId(id)

    suspend fun addAndSelect(timetable: Timetable) {
        select(repository.addTimetable(timetable))
    }
}
