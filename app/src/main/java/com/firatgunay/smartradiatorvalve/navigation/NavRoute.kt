package com.firatgunay.smartradiatorvalve.navigation

sealed class NavRoute(val route: String) {
    data object Rooms : NavRoute("rooms")
    data object Schedule : NavRoute("schedule")
    data object Settings : NavRoute("settings")
    data object AiMode : NavRoute("ai_mode")
} 