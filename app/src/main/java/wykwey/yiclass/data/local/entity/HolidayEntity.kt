package wykwey.yiclass.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "holidays",
    foreignKeys = [ForeignKey(
        entity = TimetableEntity::class,
        parentColumns = ["id"],
        childColumns = ["timetableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("timetableId")]
)
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timetableId: Long,
    val date: String,
    val isMakeup: Boolean = false,
    val makeupWeekday: Int? = null
)
