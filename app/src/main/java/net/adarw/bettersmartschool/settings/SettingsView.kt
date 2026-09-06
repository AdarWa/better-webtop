package net.adarw.bettersmartschool.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.ListPref
import com.jamal.composeprefs3.ui.prefs.SwitchPref

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(prefs: AppPreferences) {
    val context = LocalContext.current

    PrefsScreen(dataStore = context.dataStore) {
        prefsGroup("כללי") {
            prefsItem {
                SwitchPref(
                    key = prefs.collapseSameTwoClasses.key.name,
                    title = "קבץ שני שיעורים זהים",
                )
            }
        }
    }
}