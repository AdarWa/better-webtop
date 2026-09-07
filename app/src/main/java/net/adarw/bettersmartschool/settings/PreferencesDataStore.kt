package net.adarw.bettersmartschool.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

data class StartEndTime(val startTime: String, val endTime: String)

class AppPreferences(private val context: Context) {

    inner class Preference<T>(
        val key: Preferences.Key<T>,
        val defaultValue: T
    ) {
        var value: T
            get() = runBlocking {
                // Suspends the coroutine created by runBlocking until the first item is emitted
                context.dataStore.data.map { preferences ->
                    preferences[key] ?: defaultValue
                }.first()
            }
            set(newValue) {
                runBlocking {
                    // Suspends until the edit transaction is fully committed to disk
                    context.dataStore.edit { preferences ->
                        preferences[key] = newValue
                    }
                }
            }
    }

    val uniqueId = Preference(
        key = stringPreferencesKey("unique_id"),
        defaultValue = ""
    )

    val webToken = Preference(
        key = stringPreferencesKey("web_token"),
        defaultValue = ""
    )

    val institutionCode = Preference(
        key = stringPreferencesKey("institution_code"),
        defaultValue = ""
    )

    val grade = Preference(
        key = stringPreferencesKey("grade"),
        defaultValue = ""
    )

    val gradeClass = Preference(
        key = stringPreferencesKey("gradeClass"),
        defaultValue = ""
    )

    val lastUpdated = Preference(
        key = longPreferencesKey("last_updated"),
        defaultValue = 0
    )

    val cachedSchedule = Preference(
        key = stringPreferencesKey("cached_schedule"),
        ""
    )

    val cacheInvalidationInterval = Preference(
        key = stringPreferencesKey("cache_invalidation_interval"),
        defaultValue = "5"
    )

    val startEndTimes = Preference(
        key = stringPreferencesKey("start_end_times"),
        defaultValue = "8:10;8:50;8:50;9:30;9:45;10:25;10:25;11:05;11:20;12:00;12:00;12:40;13:10;13:50;13:50;14:30;14:40;15:20;15:20;16:00;16:10;16:50;16:50;17:30;17:40;18:20;18:20;19:00"
    )

    val darkTheme = Preference(
        key = booleanPreferencesKey("dark_theme"),
        defaultValue = false
    )

    fun getStartEndTime(hour: Int): StartEndTime {
        val times = startEndTimes.value.split(";")
        return StartEndTime(times.getOrElse((hour-1)*2,{"00:00"}), times.getOrElse((hour-1)*2+1, {"00:00"}))
    }

    val isAuthenticated
        get() = !webToken.value.isEmpty() && !uniqueId.value.isEmpty() && !institutionCode.value.isEmpty() && !grade.value.isEmpty() && !gradeClass.value.isEmpty()

    val collapseSameTwoClasses = Preference(
        key = booleanPreferencesKey("collapse_same_two_classes"),
        defaultValue = false
    )
}