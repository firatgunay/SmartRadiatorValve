package com.firatgunay.smartradiatorvalve.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.firatgunay.smartradiatorvalve.ui.viewmodel.SettingsViewModel
import com.firatgunay.smartradiatorvalve.ui.viewmodel.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is SettingsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    // Profil Kartı
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = state.userProfile.displayName ?: "İsimsiz Kullanıcı",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = state.userProfile.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tema Ayarı
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "Karanlık Mod",
                        subtitle = "Uygulama temasını değiştir",
                        trailing = {
                            Switch(
                                checked = state.isDarkMode,
                                onCheckedChange = { /* TODO */ }
                            )
                        }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Bildirim Ayarları
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Bildirimler",
                        subtitle = "Bildirim tercihlerini yönet",
                        trailing = {
                            Switch(
                                checked = state.notificationsEnabled,
                                onCheckedChange = { /* TODO */ }
                            )
                        }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Sıcaklık Birimi
                    SettingsItem(
                        icon = Icons.Default.Thermostat,
                        title = "Sıcaklık Birimi",
                        subtitle = "Celsius veya Fahrenheit",
                        trailing = {
                            TextButton(onClick = {
                                /* TODO */
                            }) {
                                Text(state.temperatureUnit)
                            }
                        }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Cihaz Yönetimi
                    SettingsItem(
                        icon = Icons.Default.DeviceHub,
                        title = "Cihaz Yönetimi",
                        subtitle = "Bağlı cihazları yönet",
                        onClick = { /* TODO: Implement */ }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Profil Ayarları
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Profil Ayarları",
                        subtitle = "Kullanıcı bilgilerini düzenle",
                        onClick = { showProfileDialog = true }
                    )
                }
            }
            
            is SettingsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }
    }

    // Profil Dialog
    if (showProfileDialog) {
        var newDisplayName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Profil Düzenle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDisplayName,
                        onValueChange = { newDisplayName = it },
                        label = { Text("İsim") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateDisplayName(newDisplayName)
                        showProfileDialog = false
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailing?.let {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    it()
                }
            }
        }
    }
} 