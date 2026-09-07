package net.adarw.bettersmartschool.widget

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import net.adarw.bettersmartschool.MainActivity
import net.adarw.bettersmartschool.MergedHourBlock
import net.adarw.bettersmartschool.ScheduleData
import net.adarw.bettersmartschool.getDayIndex
import net.adarw.bettersmartschool.mergeConsecutive
import net.adarw.bettersmartschool.settings.AppPreferences
import java.time.LocalDate

class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}

class ScheduleWidget : GlanceAppWidget() {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WidgetContent() {
    val context = LocalContext.current

    var mergedBlocks by remember { mutableStateOf<List<MergedHourBlock>>(emptyList()) }
    var currentHourBlock by remember { mutableStateOf(-1) }
    var statusMessage by remember { mutableStateOf("טוען מערכת...") }

    LaunchedEffect(Unit) {
        val appPreferences = AppPreferences(context)
        ScheduleData.loadFromCache(appPreferences) // Load cached data for the widget

        val scheduleResponse = ScheduleData.schedule.value
        if (scheduleResponse == null || !scheduleResponse.status || scheduleResponse.data.isNullOrEmpty()) {
            statusMessage = scheduleResponse?.message ?: "אין נתונים זמינים"
            return@LaunchedEffect
        }

        val currentDayIndex = getDayIndex()
        val todaySchedule = scheduleResponse.data.getOrNull(currentDayIndex)

        if (todaySchedule != null) {
            mergedBlocks = todaySchedule.hoursData.mergeConsecutive(
                collapse = appPreferences.collapseSameTwoClasses.value,
                appPreferences = appPreferences
            )
            currentHourBlock = appPreferences.findClosestTimeHour()
        } else {
            statusMessage = "אין מערכת להיום"
        }
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .clickable(actionStartActivity<MainActivity>())
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (mergedBlocks.isNotEmpty()) {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(mergedBlocks) { block ->
                    val isColored = currentHourBlock in block.startHour..block.endHour
                    WidgetHourCard(block = block, isColored = isColored)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
            }
        } else {
            Text(
                text = statusMessage,
                style = TextStyle(color = GlanceTheme.colors.onBackground)
            )
        }
    }
}