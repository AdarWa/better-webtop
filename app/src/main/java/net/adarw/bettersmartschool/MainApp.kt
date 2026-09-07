package net.adarw.bettersmartschool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import net.adarw.bettersmartschool.api.SmartSchoolApiClient
import net.adarw.bettersmartschool.settings.AppPreferences
import net.adarw.bettersmartschool.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(client: SmartSchoolApiClient) {
    var showSettings by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val appPreferences = remember(context) { AppPreferences(context) }

    // Launch coroutines tied to the lifecycle of this composable
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MainTopAppBar(
                showSettings = showSettings,
                onSettingsClick = { showSettings = true },
                onBackClick = { showSettings = false },
                onRefreshClick = {
                    coroutineScope.launch {
                        ScheduleData.refresh(appPreferences)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (showSettings) {
                SettingsScreen(appPreferences)
            } else {
                ScheduleContent(appPreferences = appPreferences)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    showSettings: Boolean,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    TopAppBar(
        title = { Text(if (showSettings) "הגדרות" else "מערכת") },
        navigationIcon = {
            if (showSettings) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (!showSettings) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            }
        }
    )
}