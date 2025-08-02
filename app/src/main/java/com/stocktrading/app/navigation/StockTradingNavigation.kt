package com.stocktrading.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.stocktrading.app.ui.explore.ExploreScreen
import com.stocktrading.app.ui.watchlist.WatchlistScreen
import com.stocktrading.app.ui.watchlist.WatchlistDetailScreen
import com.stocktrading.app.ui.product.ProductScreen
import com.stocktrading.app.ui.viewall.ViewAllScreen
import com.stocktrading.app.ui.theme.ThemeManager
import com.stocktrading.app.ui.components.FloatingThemeToggle

@Composable
fun StockTradingNavigation(
    navController: NavHostController,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier,
    startDestination: String = "explore"
) {

    
    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable("explore") {
                ExploreScreen(
                    onNavigateToProduct = { symbol ->
                        navController.navigate("product/$symbol")
                    },
                    onNavigateToViewAll = { section ->
                        navController.navigate("viewall/$section")
                    }
                )
            }

            composable("watchlist") {
                WatchlistScreen(
                    onNavigateToProduct = { symbol ->
                        navController.navigate("product/$symbol")
                    },
                    onNavigateToWatchlistDetail = { watchlistId, watchlistName ->
                        navController.navigate("watchlist_detail/$watchlistId/$watchlistName")
                    }
                )
            }

            composable("watchlist_detail/{watchlistId}/{watchlistName}") { backStackEntry ->
                val watchlistId = backStackEntry.arguments?.getString("watchlistId")?.toLongOrNull() ?: 0L
                val watchlistName = backStackEntry.arguments?.getString("watchlistName") ?: ""
                WatchlistDetailScreen(
                    watchlistId = watchlistId,
                    watchlistName = watchlistName,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToProduct = { symbol ->
                        navController.navigate("product/$symbol")
                    }
                )
            }

            composable("product/{symbol}") { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                ProductScreen(
                    symbol = symbol,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("viewall/{section}") { backStackEntry ->
                val section = backStackEntry.arguments?.getString("section") ?: ""
                ViewAllScreen(
                    section = section,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToProduct = { symbol ->
                        navController.navigate("product/$symbol")
                    }
                )
            }
        }
        

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val shouldShowFloatingToggle = currentRoute == "explore"
        
        if (shouldShowFloatingToggle) {
            FloatingThemeToggle(
                themeManager = themeManager,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

fun navigateToExplore(navController: NavHostController) {
    navController.navigate("explore") {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun navigateToWatchlist(navController: NavHostController) {
    navController.navigate("watchlist") {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

