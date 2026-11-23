package wykwey.yiclass.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "timetables")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startDate: String,
    val totalWeeks: Int = 20,
    val showWeekend: Boolean = false,
    val maxPeriods: Int = 12,
    val reminderMinutes: Int = 15
)
