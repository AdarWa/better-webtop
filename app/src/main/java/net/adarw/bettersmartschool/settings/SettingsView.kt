package net.adarw.bettersmartschool.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.EditTextPref
import com.jamal.composeprefs3.ui.prefs.SwitchPref
import net.adarw.bettersmartschool.auth.LoginWebView

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(prefs: AppPreferences) {
    val context = LocalContext.current
    var showWebView by remember { mutableStateOf(false) }

    // Renders the WebView as a full-screen component when active
    if (showWebView) {
        LoginWebView("https://webtop.smartschool.co.il/account/login") { result ->
            showWebView = false
            prefs.uniqueId.value = result.uniqueId
            prefs.webToken.value = result.webToken
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Settings section takes up the remaining available vertical space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            PrefsScreen(dataStore = context.dataStore) {
                prefsGroup("בית ספר") {
                    prefsItem {
                        EditTextPref(
                            key = prefs.institutionCode.key.name,
                            title = "קוד בית ספר",
                        )
                    }
                    prefsItem {
                        EditTextPref(
                            key = prefs.grade.key.name,
                            title = "כיתה",
                        )
                    }
                    prefsItem {
                        EditTextPref(
                            key = prefs.gradeClass.key.name,
                            title = "מספר כיתה",
                        )
                    }
                }
                prefsGroup("כללי") {
                    prefsItem {
                        SwitchPref(
                            key = prefs.collapseSameTwoClasses.key.name,
                            title = "קבץ שני שיעורים זהים",
                        )
                    }
                    prefsItem {
                        SwitchPref(
                            key = prefs.darkTheme.key.name,
                            title = "מצב כהה",
                            defaultChecked = false
                        )
                    }
                }
                prefsGroup("שעות") {
                    prefsItem {
                        EditTextPref(
                            key = prefs.startEndTimes.key.name,
                            title = "התאמת שעות למערכת",
                            defaultValue = prefs.startEndTimes.defaultValue
                        )
                    }
                }
                prefsGroup("רשת") {
                    prefsItem {
                        EditTextPref(
                            key = prefs.cacheInvalidationInterval.key.name,
                            title = "אינטרבל שלילת מטמון(דקות)"
                        )
                    }
                }
            }
        }

        // Authentication status and controls anchored at the bottom
        AuthenticationSection(
            isAuthenticated = prefs.isAuthenticated,
            onAuthenticateClick = { showWebView = true }
        )
    }
}

@Composable
private fun AuthenticationSection(
    isAuthenticated: Boolean,
    onAuthenticateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isAuthenticated) {
                Text(
                    text = "מחובר",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )

            } else {
                Text(
                    text = "לא מחובר",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Button(
                onClick = onAuthenticateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(!isAuthenticated)
                    Text("התחבר")
                else
                    Text("התחבר מחדש")
            }
        }
    }
}