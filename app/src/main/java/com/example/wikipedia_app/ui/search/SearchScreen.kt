package com.example.wikipedia_app.ui.search

import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wikipedia_app.model.SearchResult
import com.example.wikipedia_app.model.WikipediaResponse
import com.example.wikipedia_app.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Html
import android.os.Build
import androidx.compose.foundation.clickable

@Composable
fun SearchScreen(navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var totalHits by remember { mutableStateOf(0) }
    var suggestion by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Speech recognizer launcher
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                query = it
                fetchArticles(
                    query = query,
                    onLoadingChanged = { isLoading = it },
                    onResult = { results, hits, suggest -> 
                        searchResults = results
                        totalHits = hits
                        suggestion = suggest
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            Toast.makeText(context, "Speech recognition failed", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    fetchArticles(
                        query = query,
                        onLoadingChanged = { isLoading = it },
                        onResult = { results, hits, suggest -> 
                            searchResults = results
                            totalHits = hits
                            suggestion = suggest
                        },
                        onError = { errorMessage ->
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                label = { Text("Search Wikipedia") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    IconButton(onClick = { startVoiceSearch(context, voiceSearchLauncher) }) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Search")
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Show search suggestion if available
        suggestion?.let { suggest ->
            Text(
                text = "Did you mean: $suggest",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable {
                        query = suggest
                        fetchArticles(
                            query = suggest,
                            onLoadingChanged = { isLoading = it },
                            onResult = { results, hits, suggest -> 
                                searchResults = results
                                totalHits = hits
                                suggestion = suggest
                            },
                            onError = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
            )
        }

        // Show total hits
        if (totalHits > 0) {
            Text(
                text = "Found $totalHits results",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchResults) { result ->
                    SearchResultItem(result) {
                        navController.navigate("article/${result.title}")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = decodeHtml(result.snippet)
                    .take(150) + "...",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
            result.wordcount?.let { wordcount ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$wordcount words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to decode HTML entities properly
fun decodeHtml(htmlText: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(htmlText).toString()
    }
}

private fun startVoiceSearch(context: android.content.Context, voiceSearchLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
    }
    voiceSearchLauncher.launch(intent)
}

private fun fetchArticles(
    query: String,
    onLoadingChanged: (Boolean) -> Unit,
    onResult: (List<SearchResult>, Int, String?) -> Unit,
    onError: (String) -> Unit
) {
    if (query.isNotBlank()) {
        onLoadingChanged(true)
        RetrofitInstance.api.searchArticles(query).enqueue(object : Callback<WikipediaResponse> {
            override fun onResponse(call: Call<WikipediaResponse>, response: Response<WikipediaResponse>) {
                onLoadingChanged(false)
                if (response.isSuccessful) {
                    val wikipediaResponse = response.body()
                    when {
                        wikipediaResponse?.error != null -> {
                            onError(wikipediaResponse.error.info)
                            Log.e("API_ERROR", "Error: ${wikipediaResponse.error.code} - ${wikipediaResponse.error.info}")
                        }
                        wikipediaResponse?.warnings != null -> {
                            Log.w("API_WARNING", "Warning: ${wikipediaResponse.warnings}")
                        }
                        else -> {
                            val results = wikipediaResponse?.query?.search ?: emptyList()
                            val totalHits = wikipediaResponse?.query?.searchinfo?.totalhits ?: 0
                            val suggestion = wikipediaResponse?.query?.searchinfo?.suggestion
                            onResult(results, totalHits, suggestion)
                            Log.d("API_RESPONSE", "Fetched ${results.size} articles successfully.")
                        }
                    }
                } else {
                    onError("Error fetching data: ${response.code()}")
                    Log.e("API_RESPONSE", "Response not successful: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WikipediaResponse>, t: Throwable) {
                onLoadingChanged(false)
                onError(t.message ?: "Unknown error")
                Log.e("API_RESPONSE", "Network error: ${t.message}")
            }
        })
    }
}