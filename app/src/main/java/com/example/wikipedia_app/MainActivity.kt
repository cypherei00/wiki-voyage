package com.example.wikipedia_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.wikipedia_app.data.AppDatabase
import com.example.wikipedia_app.data.BookmarkRepository
import com.example.wikipedia_app.data.GameService
//import com.example.wikipedia_app.data.GameService
import com.example.wikipedia_app.data.HistoryRepository
import com.example.wikipedia_app.data.TrendingRepository
import com.example.wikipedia_app.navigation.Screen
import com.example.wikipedia_app.ui.article.ArticleScreen
import com.example.wikipedia_app.ui.home.HomeScreen
import com.example.wikipedia_app.ui.screens.BookmarksScreen
import com.example.wikipedia_app.ui.screens.GameScreen
import com.example.wikipedia_app.ui.search.SearchScreen
import com.example.wikipedia_app.ui.theme.WikipediaAppTheme
import com.example.wikipedia_app.ui.viewmodels.BookmarkViewModel
import com.example.wikipedia_app.ui.viewmodels.HistoryViewModel
import com.example.wikipedia_app.ui.viewmodels.TrendingViewModel
import com.example.wikipedia_app.ui.viewmodels.TTSViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WikipediaAppTheme {
                val navController = rememberNavController()
                val database = remember {
                    AppDatabase.getDatabase(this)
                }
                val bookmarkRepository = remember {
                    BookmarkRepository(database.bookmarkDao())
                }
                val bookmarkViewModel = remember {
                    BookmarkViewModel(bookmarkRepository)
                }
                val historyRepository = remember {
                    HistoryRepository(database.historyDao())
                }
                val historyViewModel = remember {
                    HistoryViewModel(historyRepository)
                }
                val trendingRepository = remember {
                    TrendingRepository()
                }
                val trendingViewModel = remember {
                    TrendingViewModel(trendingRepository)
                }
                val gameService = remember {
                    GameService()
                }
                val ttsViewModel = remember {
                    TTSViewModel(this)
                }
                WikipediaNavGraph(
                    navController = navController,
                    bookmarkViewModel = bookmarkViewModel,
                    historyViewModel = historyViewModel,
                    trendingViewModel = trendingViewModel,
                    gameService = gameService,
                    ttsViewModel = ttsViewModel
                )
            }
        }
    }
}

@Composable
fun WikipediaNavGraph(
    navController: NavHostController,
    bookmarkViewModel: BookmarkViewModel,
    historyViewModel: HistoryViewModel,
    trendingViewModel: TrendingViewModel,
    gameService: GameService,
    ttsViewModel: TTSViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = trendingViewModel
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                historyViewModel = historyViewModel
            )
        }
        composable(
            route = "article/{title}",
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            ArticleScreen(
                title = title,
                navController = navController,
                viewModel = bookmarkViewModel,
                historyViewModel = historyViewModel,
                ttsViewModel = ttsViewModel
            )
        }
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                navController = navController,
                viewModel = bookmarkViewModel,
                onBookmarkClick = { url ->
                    val title = url.substringAfterLast("/")
                    navController.navigate(Screen.Article.createRoute(title))
                }
            )
        }
        composable(Screen.Game.route) {
            GameScreen(
                gameService = gameService,
                onPlayAgain = {
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }
    }
}