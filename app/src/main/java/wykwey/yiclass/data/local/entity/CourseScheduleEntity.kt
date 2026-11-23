package wykwey.yiclass.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "course_schedules",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("courseId")]
)
data class CourseScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val weekday: Int,
    val periods: String,
    val weekPattern: String,
    val reminder: String = ""
)
