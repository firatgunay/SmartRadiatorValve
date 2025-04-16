import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "Ana Sayfa",
        icon = Icons.Default.Home
    )
    
    object Schedule : BottomNavItem(
        route = "schedule",
        title = "Program",
        icon = Icons.Default.Schedule
    )
    
    object Settings : BottomNavItem(
        route = "settings",
        title = "Ayarlar",
        icon = Icons.Default.Settings
    )

    companion object {
        fun fromRoute(route: String?): BottomNavItem {
            return when (route) {
                "home" -> Home
                "schedule" -> Schedule
                "settings" -> Settings
                else -> Home
            }
        }
    }
} 