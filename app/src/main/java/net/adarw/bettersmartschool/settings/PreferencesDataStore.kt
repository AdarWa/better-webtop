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

    val isAuthenticated
        get() = !webToken.value.isEmpty() && !uniqueId.value.isEmpty() && !institutionCode.value.isEmpty() && !grade.value.isEmpty() && !gradeClass.value.isEmpty()

    val collapseSameTwoClasses = Preference(
        key = booleanPreferencesKey("collapse_same_two_classes"),
        defaultValue = false
    )
}