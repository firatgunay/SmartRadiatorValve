package com.firatgunay.smartradiatorvalve.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Rooms : BottomNavItem(
        route = NavRoute.Rooms.route,
        title = "Odalar",
        icon = Icons.Default.Home
    )
    
    data object Schedule : BottomNavItem(
        route = NavRoute.Schedule.route,
        title = "Program",
        icon = Icons.Default.Schedule
    )
    
    data object AiMode : BottomNavItem(
        route = NavRoute.AiMode.route,
        title = "Yapay Zeka",
        icon = Icons.Default.Psychology
    )
    
    data object Settings : BottomNavItem(
        route = NavRoute.Settings.route,
        title = "Ayarlar",
        icon = Icons.Default.Settings
    )
} 