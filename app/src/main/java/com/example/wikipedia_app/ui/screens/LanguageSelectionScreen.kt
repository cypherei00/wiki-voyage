package com.example.wikipedia_app.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wikipedia_app.R
import com.example.wikipedia_app.navigation.Screen
import com.example.wikipedia_app.network.ApiConfig
import java.util.*

data class Language(
    val code: String,
    val name: String,
    val nativeName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    navController: NavController,
    onLanguageSelected: (String) -> Unit
) {
    val TAG = "LanguageSelection"

    val languages = remember {
        listOf(
            Language("en", "English", "English"),
            Language("es", "Spanish", "Español"),
            Language("fr", "French", "Français"),
            Language("de", "German", "Deutsch"),
            Language("it", "Italian", "Italiano"),
            Language("pt", "Portuguese", "Português"),
            Language("ru", "Russian", "Русский"),
            Language("zh", "Chinese", "中文"),
            Language("ja", "Japanese", "日本語"),
            Language("ko", "Korean", "한국어")
        )
    }

    val currentLocale = remember { Locale.getDefault() }
    Log.d(TAG, "Current system locale: ${currentLocale.language}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language_selection)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(languages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = language.code == currentLocale.language,
                    onLanguageSelected = {
                        Log.d(TAG, "Language selected: ${language.name} (${language.code})")
                        try {
                            ApiConfig.setLanguage(language.code)
                            onLanguageSelected(language.code)
                            Log.d(TAG, "Language change completed successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error changing language: ${e.message}", e)
                        }
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onLanguageSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLanguageSelected)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = language.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
} 