package wykwey.yiclass.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "courses",
    foreignKeys = [ForeignKey(
        entity = TimetableEntity::class,
        parentColumns = ["id"],
        childColumns = ["timetableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("timetableId")]
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timetableId: Long,
    val name: String,
    val location: String,
    val teacher: String,
    val color: Int
)
