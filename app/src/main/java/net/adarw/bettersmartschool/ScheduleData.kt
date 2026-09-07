package net.adarw.bettersmartschool

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import net.adarw.bettersmartschool.api.ShotefScheduleRequest
import net.adarw.bettersmartschool.api.ShotefScheduleResponse
import net.adarw.bettersmartschool.settings.AppPreferences

object ScheduleData {

    private val _schedule = MutableStateFlow<ShotefScheduleResponse?>(null)
    val schedule: StateFlow<ShotefScheduleResponse?> = _schedule.asStateFlow()

    suspend fun refresh(appPreferences: AppPreferences) {
        if (appPreferences.isAuthenticated) {
            val request = ShotefScheduleRequest(
                appPreferences.institutionCode.value.toInt(),
                "${appPreferences.grade.value}|${appPreferences.gradeClass.value}",
                1
            )
            _schedule.emit(client.getScheduleData(
                request, appPreferences.webToken.value,
                appPreferences.uniqueId.value
            ))
            cache(appPreferences)
        }
    }

    suspend fun loadFromCache(appPreferences: AppPreferences) {
        if(appPreferences.cachedSchedule.value.isEmpty()) return

        _schedule.emit(Json.decodeFromString(appPreferences.cachedSchedule.value))
    }

    fun cache(appPreferences: AppPreferences) {
        if(schedule.value == null) return
        appPreferences.cachedSchedule.value = Json.encodeToString(schedule.value)
        appPreferences.lastUpdated.value = System.currentTimeMillis()
    }

}