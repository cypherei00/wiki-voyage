package com.example.wikipedia_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wikipedia_app.data.Bookmark
import com.example.wikipedia_app.ui.theme.*
import com.example.wikipedia_app.ui.viewmodels.BookmarkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    navController: NavController,
    viewModel: BookmarkViewModel,
    onBookmarkClick: (String) -> Unit
) {
    val bookmarks by viewModel.allBookmarks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Bookmarks",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CreamOffWhite
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CreamOffWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealCyan,
                    titleContentColor = CreamOffWhite
                )
            )
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    "No bookmarks yet",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DarkBrown
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBeige)
                    .padding(padding)
            ) {
                items(bookmarks) { bookmark ->
                    BookmarkItem(
                        bookmark = bookmark,
                        onClick = { onBookmarkClick(bookmark.url) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = CreamOffWhite.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkBrown
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "Bookmarked",
                tint = TealCyan
            )
        }
    }
} 