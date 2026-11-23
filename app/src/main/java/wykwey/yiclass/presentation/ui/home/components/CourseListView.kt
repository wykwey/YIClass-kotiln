package wykwey.yiclass.presentation.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wykwey.yiclass.domain.model.Course
import wykwey.yiclass.domain.model.Timetable
import wykwey.yiclass.domain.util.TimetableQuery
import wykwey.yiclass.presentation.ui.components.layout.CourseInfoCard
import wykwey.yiclass.presentation.ui.components.layout.CourseInfoCardMode

/**
 * 列表视图：按周次分组展示全部课程，自适应网格排布，对齐原版 list_view.dart。
 */
@Composable
fun CourseListView(
    timetable: Timetable,
    modifier: Modifier = Modifier,
    onCourseClick: (courseId: Long) -> Unit = {}
) {
    val groupedCourses: List<Pair<Int, List<Course>>> = remember(timetable) {
        (1..timetable.settings.totalWeeks).mapNotNull { week ->
            val courses = TimetableQuery.weekCourses(timetable, week)
            if (courses.isEmpty()) null else week to courses
        }
    }

    if (groupedCourses.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无课程", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        groupedCourses.forEach { (week, courses) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "第${week}周",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(courses, key = { "$week-${it.id}" }) { course ->
                CourseInfoCard(
                    course = course,
                    mode = CourseInfoCardMode.ListView,
                    onClick = { onCourseClick(course.id) }
                )
            }
        }
    }
}
