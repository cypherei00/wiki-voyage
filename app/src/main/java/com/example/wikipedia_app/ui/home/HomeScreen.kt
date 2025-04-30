package com.example.wikipedia_app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wikipedia_app.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wikipedia Home",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(onClick = { navController.navigate(Screen.Search.route) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Search Articles")
                }
            }
            item {
                Button(onClick = { navController.navigate(Screen.Bookmarks.route) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "View Bookmarks")
                }
            }
            item {
                Button(onClick = { navController.navigate(Screen.Settings.route) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Settings")
                }
            }
            item {
                Button(onClick = { navController.navigate(Screen.Game.route) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Hyperlink Game")
                }
            }
        }
    }
}