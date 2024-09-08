package com.example.litelens.presentation.navgraph

sealed class Route(
    val route: String
) {

    data object AppStartNavigation: Route(route = Routes.ROUTE_APP_START_NAVIGATION)
    data object HomeNavigation: Route(route = Routes.ROUTE_HOME_NAVIGATION)
    data object OnBoardingScreen: Route(route = Routes.ROUTE_ONBOARDING_SCREEN)
    data object HomeScreen: Route(route = Routes.ROUTE_HOME_SCREEN)
    data object SavedNavigation: Route(route = Routes.ROUTE_SAVED_NAVIGATION)
    data object SavedSearches: Route(route = Routes.ROUTE_SAVED_SEARCHES)

}

object Routes {
    const val ROUTE_APP_START_NAVIGATION = "appStartNavigationRoute"
    const val ROUTE_HOME_NAVIGATION = "homeNavigationRoute"
    const val ROUTE_SAVED_NAVIGATION = "savedSearchesRoute"
    const val ROUTE_ONBOARDING_SCREEN = "onBoardingScreen"
    const val ROUTE_HOME_SCREEN = "homeScreen"
    const val ROUTE_SAVED_SEARCHES = "savedSearches"
}