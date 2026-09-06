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

@Composable
fun ScheduleScreen(response: ShotefScheduleResponse) {
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
                DayScheduleList(hoursData = selectedDay.hoursData)
            }
        }
    }
}

@Composable
fun DayScheduleList(hoursData: List<HourData>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(hoursData) { hourData ->
            HourCard(hourData = hourData)
        }
    }
}

@Composable
fun HourCard(hourData: HourData) {
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hourData.hourName ?: hourData.hour.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (hourData.scheduale.isEmpty() && hourData.events.isEmpty()) {
                    Text(
                        text = "שעה חופשית",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    hourData.scheduale.forEach { lesson ->
                        LessonItem(lesson = lesson)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    hourData.events.forEach { event ->
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
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
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
        modifier = Modifier.fillMaxSize().padding(32.dp),
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
fun ScheduleContent(client: SmartSchoolApiClient) {
    var scheduleResponse by remember { mutableStateOf<ShotefScheduleResponse?>(null) }

    LaunchedEffect(Unit) {
        scheduleResponse = client.getScheduleData(ShotefScheduleRequest(340018, "12|10", 1))
    }

    val resp = scheduleResponse
    if (resp != null) {
        ScheduleScreen(resp)
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
        1 -> "ראשון"
        2 -> "שני"
        3 -> "שלישי"
        4 -> "רביעי"
        5 -> "חמישי"
        6 -> "שישי"
        7 -> "שבת"
        else -> "יום $dayIndex"
    }
}