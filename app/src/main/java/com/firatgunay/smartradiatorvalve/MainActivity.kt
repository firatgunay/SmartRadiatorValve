package com.firatgunay.smartradiatorvalve

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.firatgunay.smartradiatorvalve.ui.screen.LoginScreen
import com.firatgunay.smartradiatorvalve.ui.screen.HomeScreen
import com.firatgunay.smartradiatorvalve.ui.screen.MainScreen
import com.firatgunay.smartradiatorvalve.ui.theme.SmartRadiatorValveTheme
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("MainActivity", "Uncaught exception", throwable)
        }
        enableEdgeToEdge()
        setContent {
            SmartRadiatorValveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLoggedIn by remember { 
                        mutableStateOf(FirebaseAuth.getInstance().currentUser != null) 
                    }
                    // Firebase Auth state listener
                    DisposableEffect(Unit) {
                        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                            isLoggedIn = auth.currentUser != null
                        }
                        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
                        
                        onDispose {
                            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
                        }
                    }

                    if (!isLoggedIn) {
                        LoginScreen(
                            onLoginSuccess = { 
                                // Artık burada isLoggedIn'i manuel olarak set etmeye gerek yok
                                // AuthStateListener otomatik olarak güncelleyecek
                            }
                        )
                    } else {
                        MainScreen(
                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                // isLoggedIn state'i AuthStateListener tarafından güncellenecek
                            }
                        )
                    }
                }
            }
        }
    }
}