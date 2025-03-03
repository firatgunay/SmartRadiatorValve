package com.firatgunay.smartradiatorvalve.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class UserProfile(
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val creationTime: String? = null
)

sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data class Success(
        val userProfile: UserProfile,
        val isDarkMode: Boolean,
        val notificationsEnabled: Boolean,
        val temperatureUnit: String,
        val appVersion: String = "1.0.0"
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    val profile = UserProfile(
                        email = user.email ?: "",
                        displayName = user.displayName,
                        photoUrl = user.photoUrl?.toString(),
                        creationTime = user.metadata?.creationTimestamp?.toString()
                    )
                    
                    _uiState.value = SettingsUiState.Success(
                        userProfile = profile,
                        isDarkMode = false, // SharedPreferences'dan alınacak
                        notificationsEnabled = true, // SharedPreferences'dan alınacak
                        temperatureUnit = "Celsius" // SharedPreferences'dan alınacak
                    )
                } else {
                    _uiState.value = SettingsUiState.Error("Kullanıcı bulunamadı")
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Profil yüklenirken hata oluştu: ${e.message}")
            }
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            try {
                auth.currentUser?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                )?.addOnSuccessListener {
                    loadUserProfile()
                }?.addOnFailureListener { e ->
                    _uiState.value = SettingsUiState.Error("İsim güncellenirken hata oluştu: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("İsim güncellenirken hata oluştu: ${e.message}")
            }
        }
    }
} 