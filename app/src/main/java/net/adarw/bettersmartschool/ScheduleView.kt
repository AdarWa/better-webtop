package net.adarw.bettersmartschool

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.adarw.bettersmartschool.api.Event
import net.adarw.bettersmartschool.api.HourData
import net.adarw.bettersmartschool.api.Lesson
import net.adarw.bettersmartschool.api.ShotefScheduleResponse
import net.adarw.bettersmartschool.settings.AppPreferences
import java.time.LocalDate

// Presentation model to hold either a single hour or a merged block of identical hours
data class MergedHourBlock(
    val startHour: Int,
    val endHour: Int,
    val startHourName: String?,
    val endHourName: String?,
    val startTime: String?,
    val endTime: String?,
    val scheduale: List<Lesson>,
    val events: List<Event>
) {
    val displayTime: String
        get() = if (startHour == endHour) {
            startHourName ?: startHour.toString()
        } else {
            "${endHourName ?: endHour} - ${startHourName ?: startHour}"
        }

    val displayClockTime: String
        get() = if (!startTime.isNullOrBlank() && !endTime.isNullOrBlank()) {
            "$endTime - $startTime"
        } else {
            ""
        }

    val isEmptyBlock: Boolean
        get() = scheduale.isEmpty() && events.isEmpty()
}

// Groups consecutive hours that have identical lessons and events
fun List<HourData>.mergeConsecutive(collapse: Boolean, appPreferences: AppPreferences): List<MergedHourBlock> {
    if (!collapse || this.isEmpty()) {
        return this.map {
            val startEndTime = appPreferences.getStartEndTime(it.hour)
            MergedHourBlock(
                startHour = it.hour,
                endHour = it.hour,
                startHourName = it.hourName,
                endHourName = it.hourName,
                startTime = startEndTime.startTime,
                endTime = startEndTime.endTime,
                scheduale = it.scheduale,
                events = it.events
            )
        }
    }

    val result = mutableListOf<MergedHourBlock>()
    var currentBlock: MergedHourBlock? = null

    for (hour in this) {
        if (currentBlock == null) {
            val startEndTime = appPreferences.getStartEndTime(hour.hour)
            currentBlock = MergedHourBlock(
                startHour = hour.hour,
                endHour = hour.hour,
                startHourName = hour.hourName,
                endHourName = hour.hourName,
                startTime = startEndTime.startTime,
                endTime = startEndTime.endTime,
                scheduale = hour.scheduale,
                events = hour.events
            )
        } else {
            val isIdentical =
                currentBlock.scheduale == hour.scheduale && currentBlock.events == hour.events
            val startEndTime = appPreferences.getStartEndTime(hour.hour)
            if (isIdentical) {
                // Extend the current block to include this hour's end time
                currentBlock = currentBlock.copy(
                    endHour = hour.hour,
                    endHourName = hour.hourName,
                    endTime = startEndTime.endTime
                )
            } else {
                result.add(currentBlock)
                currentBlock = MergedHourBlock(
                    startHour = hour.hour,
                    endHour = hour.hour,
                    startHourName = hour.hourName,
                    endHourName = hour.hourName,
                    startTime = startEndTime.startTime,
                    endTime = startEndTime.endTime,
                    scheduale = hour.scheduale,
                    events = hour.events
                )
            }
        }
    }

    currentBlock?.let { result.add(it) }
    return result
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleScreen(response: ShotefScheduleResponse, appPreferences: AppPreferences) {
    // Forces the entire screen context to Right-To-Left for proper Hebrew rendering
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!response.status || response.data.isNullOrEmpty()) {
                ErrorDisplay(
                    message = response.message ?: response.errorDescription ?: "שגיאה בטעינת המערכת"
                )
                return@Surface
            }

            val days = response.data
            var selectedTabIndex by remember { mutableIntStateOf(LocalDate.now().dayOfWeek.value % 6) }

            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    days.forEachIndexed { index, daySchedule ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = getHebrewDayName(daySchedule.dayIndex),
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                val selectedDay = days[selectedTabIndex]
                DayScheduleList(
                    hoursData = selectedDay.hoursData,
                    collapseIdenticalLessons = appPreferences.collapseSameTwoClasses.value,
                    appPreferences
                )
            }
        }
    }
}

@Composable
fun DayScheduleList(hoursData: List<HourData>, collapseIdenticalLessons: Boolean, appPreferences: AppPreferences) {
    // Recalculate merged blocks only when the underlying data or the toggle flag changes
    val mergedBlocks = remember(hoursData, collapseIdenticalLessons) {
        hoursData.mergeConsecutive(collapse = collapseIdenticalLessons, appPreferences)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mergedBlocks) { block ->
            HourCard(block = block)
        }
    }
}

@Composable
fun HourCard(block: MergedHourBlock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = 64.dp, // Widened slightly to fit the clock time text
                        minHeight = 48.dp
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = block.displayTime,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )

                if (block.displayClockTime.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = block.displayClockTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (block.isEmptyBlock) {
                    Text(
                        text = "שעה חופשית",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    block.scheduale.forEach { lesson ->
                        LessonItem(lesson = lesson)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    block.events.forEach { event ->
                        EventItem(event = event)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LessonItem(lesson: Lesson) {
    Column {
        Text(
            text = lesson.subject ?: "מקצוע לא ידוע",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Assemble teacher name properly handling nulls
        val teacherName = listOfNotNull(lesson.teacherPrivateName, lesson.teacherLastName)
            .joinToString(" ")
            .takeIf { it.isNotBlank() } ?: "מורה לא שובץ"

        Text(
            text = "מורה: $teacherName",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        lesson.room?.let {
            Text(
                text = "חדר: $it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (lesson.isPartani == true) {
            Badge(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "פרטני",
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()) {
            Text(
                text = event.title ?: "אירוע",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            event.textualType?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun ErrorDisplay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleContent(appPreferences: AppPreferences) {
    val schedule by ScheduleData.schedule.collectAsState()
    LaunchedEffect(Unit) {
        if(System.currentTimeMillis() - appPreferences.lastUpdated.value < appPreferences.cacheInvalidationInterval.value.toInt()*60*1000) {
            ScheduleData.loadFromCache(appPreferences)
        }else {
            ScheduleData.refresh(appPreferences)
        }
    }
    if (schedule != null) {
        ScheduleScreen(response = schedule!!, appPreferences)
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

private fun getHebrewDayName(dayIndex: Int): String {
    return when (dayIndex) {
        1 -> "א"
        2 -> "ב"
        3 -> "ג"
        4 -> "ד"
        5 -> "ה"
        6 -> "ו"
        7 -> "ש"
        else -> "יום $dayIndex"
    }
}