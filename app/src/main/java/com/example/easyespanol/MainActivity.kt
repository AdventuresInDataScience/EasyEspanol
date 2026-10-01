package com.example.easyespanol

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.compose.rememberNavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.navigation.AppNavigation
import com.example.easyespanol.ui.theme.EasyEspanolTheme
import com.example.easyespanol.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = AppSettings(applicationContext)
        val repo = PhraseRepository(applicationContext)

        setContent {
            val dark = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            // Keep the status-bar icons readable when the app's light/dark choice
            // differs from the phone's.
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            EasyEspanolTheme(palette = settings.palette, darkTheme = dark) {
                AppNavigation(rememberNavController(), repo, settings)
            }
        }
    }
}
