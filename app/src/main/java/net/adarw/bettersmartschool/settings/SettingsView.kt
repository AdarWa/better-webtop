package net.adarw.bettersmartschool.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.ListPref
import com.jamal.composeprefs3.ui.prefs.SwitchPref
import net.adarw.bettersmartschool.dataStore

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    PrefsScreen(dataStore = context.dataStore) {
        prefsGroup("General") {
            prefsItem {
                SwitchPref(
                    key = "notifications_enabled",
                    title = "Enable Notifications",
                    summary = "Receive schedule updates"
                )
            }
            prefsItem {
                ListPref(
                    key = "theme_selection",
                    title = "App Theme",
                    entries = mapOf("light" to "Light", "dark" to "Dark")
                )
            }
        }
    }
}