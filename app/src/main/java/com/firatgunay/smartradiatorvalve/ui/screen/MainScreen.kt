package com.firatgunay.smartradiatorvalve.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.firatgunay.smartradiatorvalve.navigation.BottomNavItem
import com.firatgunay.smartradiatorvalve.navigation.NavRoute
import com.firatgunay.smartradiatorvalve.ui.viewmodel.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val items = listOf(
                    BottomNavItem.Rooms,
                    BottomNavItem.Schedule,
                    BottomNavItem.AiMode,
                    BottomNavItem.Settings
                )
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Rooms.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoute.Rooms.route) {
                HomeScreen(
                    onLogout = {
                        viewModel.cleanup()
                        onLogout()
                    }
                )
            }
            composable(NavRoute.Schedule.route) {
                ScheduleScreen()
            }
            composable(NavRoute.AiMode.route) {
                AiModeScreen()
            }
            composable(NavRoute.Settings.route) {
                SettingsScreen()
            }
        }
    }
} 