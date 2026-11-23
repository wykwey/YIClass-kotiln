package wykwey.yiclass.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "class_times",
    foreignKeys = [ForeignKey(
        entity = TimetableEntity::class,
        parentColumns = ["id"],
        childColumns = ["timetableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("timetableId")]
)
data class ClassTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timetableId: Long,
    val period: Int,
    val startTime: String,
    val endTime: String
)
