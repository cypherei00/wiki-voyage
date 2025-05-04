package com.example.wikipedia_app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.wikipedia_app.R
import com.example.wikipedia_app.navigation.Screen
import com.example.wikipedia_app.ui.theme.TealCyan
import com.example.wikipedia_app.ui.theme.CreamOffWhite

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, Icons.Default.Home, R.string.home),
        BottomNavItem(Screen.Search.route, Icons.Default.Search, R.string.search),
        BottomNavItem(Screen.Bookmarks.route, Icons.Default.Bookmark, R.string.bookmarks),
        BottomNavItem(Screen.Game.route, Icons.Default.Games, R.string.game),
        BottomNavItem(Screen.Settings.route, Icons.Default.Settings, R.string.settings)
    )

    NavigationBar(
        containerColor = TealCyan,
        contentColor = CreamOffWhite
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = stringResource(item.labelResId)) },
                label = { Text(stringResource(item.labelResId)) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CreamOffWhite,
                    unselectedIconColor = CreamOffWhite.copy(alpha = 0.7f),
                    selectedTextColor = CreamOffWhite,
                    unselectedTextColor = CreamOffWhite.copy(alpha = 0.7f),
                    indicatorColor = TealCyan.copy(alpha = 0.8f)
                )
            )
        }
    }
} 