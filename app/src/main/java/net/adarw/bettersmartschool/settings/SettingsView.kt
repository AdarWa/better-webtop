package net.adarw.bettersmartschool.settings

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.SwitchPref
import net.adarw.bettersmartschool.auth.LoginWebView

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
    var showWebView by remember { mutableStateOf(false) }
    if(showWebView){
        LoginWebView("https://webtop.smartschool.co.il/account/login") {
            showWebView = false
            prefs.uniqueId.value = it.uniqueId
            prefs.webToken.value = it.webToken
        }
    }else {
        Button({showWebView = true}) {
            Text("Authenticate")
        }
    }

    if(prefs.isAuthenticated) {
        Text("Credentials Present")
    }else {
        Text("Please Authenticate", color = Color.Red)
    }
}