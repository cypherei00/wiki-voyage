package com.example.wikipedia_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wikipedia_app.ui.theme.WikipediaAppTheme

import com.example.wikipedia_app.navigation.Screen
import com.example.wikipedia_app.ui.article.ArticleScreen
import com.example.wikipedia_app.ui.home.HomeScreen
import com.example.wikipedia_app.ui.search.SearchScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WikipediaAppTheme  {
                val navController = rememberNavController()
                WikipediaNavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun WikipediaNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController)
        }
        composable(
            route = Screen.Article.route,
            arguments = listOf(navArgument("title") { type = NavType.StringType })
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            ArticleScreen(title = title, navController = navController)
        }
//        composable(Screen.Bookmarks.route) {
//            BookmarksScreen(navController)
//        }
//        composable(Screen.Settings.route) {
//            SettingsScreen(navController)
//        }
//        composable(Screen.Game.route) {
//            GameScreen(navController)
//        }
    }
}