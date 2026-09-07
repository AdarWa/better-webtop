package net.adarw.bettersmartschool

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import net.adarw.bettersmartschool.api.ShotefScheduleRequest
import net.adarw.bettersmartschool.api.ShotefScheduleResponse
import net.adarw.bettersmartschool.api.SmartSchoolApiClient
import net.adarw.bettersmartschool.settings.AppPreferences

// Presentation model to hold either a single hour or a merged block of identical hours
data class MergedHourBlock(
    val startHour: Int,
    val endHour: Int,
    val startHourName: String?,
    val endHourName: String?,
    val scheduale: List<Lesson>,
    val events: List<Event>
) {
    // Generates the label for the hour badge (e.g., "1" or "1 - 2")
    val displayTime: String
        get() = if (startHour == endHour) {
            startHourName ?: startHour.toString()
        } else {
            "${endHourName ?: endHour} - ${startHourName ?: startHour}"
        }

    val isEmptyBlock: Boolean
        get() = scheduale.isEmpty() && events.isEmpty()
}

// Groups consecutive hours that have identical lessons and events
fun List<HourData>.mergeConsecutive(collapse: Boolean): List<MergedHourBlock> {
    if (!collapse || this.isEmpty()) {
        return this.map {
            MergedHourBlock(it.hour, it.hour, it.hourName, it.hourName, it.scheduale, it.events)
        }
    }

    val result = mutableListOf<MergedHourBlock>()
    var currentBlock: MergedHourBlock? = null

    for (hour in this) {
        if (currentBlock == null) {
            currentBlock = MergedHourBlock(
                startHour = hour.hour,
                endHour = hour.hour,
                startHourName = hour.hourName,
                endHourName = hour.hourName,
                scheduale = hour.scheduale,
                events = hour.events
            )
        } else {
            // Relies on Lesson and Event being data classes for proper structural equality comparison (==)
            val isIdentical =
                currentBlock.scheduale == hour.scheduale && currentBlock.events == hour.events

            if (isIdentical) {
                // Extend the current block to include this hour
                currentBlock = currentBlock.copy(
                    endHour = hour.hour,
                    endHourName = hour.hourName
                )
            } else {
                // Store the finished block and start a new one
                result.add(currentBlock)
                currentBlock = MergedHourBlock(
                    startHour = hour.hour,
                    endHour = hour.hour,
                    startHourName = hour.hourName,
                    endHourName = hour.hourName,
                    scheduale = hour.scheduale,
                    events = hour.events
                )
            }
        }
    }

    currentBlock?.let { result.add(it) }
    return result
}

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
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 8.dp,
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
                    collapseIdenticalLessons = appPreferences.collapseSameTwoClasses.value
                )
            }
        }
    }
}

@Composable
fun DayScheduleList(hoursData: List<HourData>, collapseIdenticalLessons: Boolean) {
    // Recalculate merged blocks only when the underlying data or the toggle flag changes
    val mergedBlocks = remember(hoursData, collapseIdenticalLessons) {
        hoursData.mergeConsecutive(collapse = collapseIdenticalLessons)
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
            // Display the hour number prominently on the side
            Box(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    ) // Changed from exact size to allow text expansion
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = block.displayTime,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
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

// Maps standard numerical day representations to Hebrew day names
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