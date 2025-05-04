package com.example.wikipedia_app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wikipedia_app.R
import com.example.wikipedia_app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentTheme: String,
    onThemeChanged: (String) -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    val themes = listOf(
        stringResource(R.string.system_default),
        stringResource(R.string.light),
        stringResource(R.string.dark)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Language Selection
            ListItem(
                headlineContent = { Text(stringResource(R.string.language)) },
                leadingContent = {
                    Icon(Icons.Default.Language, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    navController.navigate(Screen.LanguageSelection.route)
                }
            )
            Divider()

            // Theme Selection
            ListItem(
                headlineContent = { Text(stringResource(R.string.theme)) },
                supportingContent = { Text(currentTheme) },
                leadingContent = {
                    Icon(Icons.Default.Palette, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    showThemeDialog = true
                }
            )
            Divider()
        }

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text(stringResource(R.string.theme)) },
                text = {
                    Column {
                        themes.forEach { theme ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onThemeChanged(theme)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(theme)
                                if (theme == currentTheme) {
                                    RadioButton(
                                        selected = true,
                                        onClick = null
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
} 