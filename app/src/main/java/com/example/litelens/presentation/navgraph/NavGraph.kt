package com.example.litelens.presentation.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.litelens.presentation.home.HomeScreen
import com.example.litelens.presentation.savedSearches.SavedSearches

@Composable
fun NavGraph(
    startDestination: String
) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        navigation(
            route = Route.HomeNavigation.route,
            startDestination = Route.HomeScreen.route
        ) {
            composable(
                route = Route.HomeScreen.route
            ) {
                HomeScreen(
                    onNavigateToSavedSearches = { navController.navigate(Route.SavedSearches.route) }
                )
            }
        }

        navigation(
            route = Route.SavedNavigation.route,
            startDestination = Route.SavedSearches.route
        ) {
            composable(
                route = Route.SavedSearches.route
            ) {
                SavedSearches()
            }
        }

    }
}