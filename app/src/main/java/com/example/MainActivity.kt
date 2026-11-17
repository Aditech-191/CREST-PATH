package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ThemePreferencesRepository
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.ui.AuthScreen
import com.example.ui.MainShellScreen
import com.example.ui.theme.CrestPathTheme
import com.example.ui.theme.ThemeMode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase ops
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            CrestPathRoot()
        }
    }
}

@Composable
private fun CrestPathRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeRepo = remember(context) { ThemePreferencesRepository(context) }
    val themeMode by themeRepo.themeModeFlow.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    val onQuickToggleTheme: () -> Unit = {
        val next = if (themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        scope.launch { themeRepo.setThemeMode(next) }
    }

    CrestPathTheme(themeMode = themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val user = currentUser
            if (user == null) {
                AuthScreen(
                    themeMode = themeMode,
                    onToggleTheme = onQuickToggleTheme,
                    onAuthSuccess = { /* Reactive authStateFlow automatically transitions UI */ }
                )
            } else {
                key(user.uid) {
                    MainShellScreen(
                        currentUserId = user.uid,
                        userDisplayName = user.displayName,
                        userEmail = user.email,
                        themeMode = themeMode,
                        onSelectTheme = { selected ->
                            scope.launch { themeRepo.setThemeMode(selected) }
                        },
                        onQuickToggleTheme = onQuickToggleTheme
                    )
                }
            }
        }
    }
}
