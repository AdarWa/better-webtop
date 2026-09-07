package net.adarw.bettersmartschool.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import net.adarw.bettersmartschool.MainActivity
import net.adarw.bettersmartschool.MergedHourBlock
import net.adarw.bettersmartschool.api.Event
import net.adarw.bettersmartschool.api.Lesson

@Composable
fun WidgetHourCard(block: MergedHourBlock, isColored: Boolean) {
    val containerColor = if (isColored) {
        GlanceTheme.colors.errorContainer
    } else {
        GlanceTheme.colors.surface
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(containerColor)
            .cornerRadius(12.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Time Block Indicator
        Column(
            modifier = GlanceModifier
                .width(83.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(8.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = block.displayTime,
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
            if (block.displayClockTime.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = block.displayClockTime,
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = 10.sp
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Schedule Content
        Column(modifier = GlanceModifier.defaultWeight()) {
            if (block.isEmptyBlock) {
                Text(
                    text = "שעה חופשית",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier.padding(top = 12.dp)
                )
            } else {
                block.scheduale.forEach { lesson ->
                    WidgetLessonItem(lesson)
                    Spacer(modifier = GlanceModifier.height(6.dp))
                }
                block.events.forEach { event ->
                    WidgetEventItem(event)
                    Spacer(modifier = GlanceModifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun WidgetLessonItem(lesson: Lesson) {
    Column {
        Text(
            text = lesson.subject ?: "מקצוע לא ידוע",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp
            )
        )

        val teacherName = listOfNotNull(lesson.teacherPrivateName, lesson.teacherLastName)
            .joinToString(" ")
            .takeIf { it.isNotBlank() } ?: "מורה לא שובץ"

        Text(
            text = "מורה: $teacherName",
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        )

        lesson.room?.let {
            Text(
                text = "חדר: $it",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }

        if (lesson.isPartani == true) {
            Text(
                text = "פרטני",
                style = TextStyle(
                    color = GlanceTheme.colors.onTertiaryContainer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = GlanceModifier
                    .padding(top = 2.dp)
                    .background(GlanceTheme.colors.tertiaryContainer)
                    .cornerRadius(4.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun WidgetEventItem(event: Event) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.secondaryContainer)
            .cornerRadius(4.dp)
            .padding(6.dp)
    ) {
        Text(
            text = event.title ?: "אירוע",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSecondaryContainer,
                fontSize = 12.sp
            )
        )
        event.textualType?.let {
            Text(
                text = it,
                style = TextStyle(
                    color = GlanceTheme.colors.onSecondaryContainer,
                    fontSize = 10.sp
                )
            )
        }
    }
}