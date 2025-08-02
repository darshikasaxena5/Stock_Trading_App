package com.stocktrading.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.stocktrading.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FloatingThemeToggle(
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val themeMode by themeManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val scope = rememberCoroutineScope()
    val isDark = StockTradingColors.isDarkTheme()
    
    FloatingActionButton(
        onClick = {
            scope.launch {
                val newMode = when (themeMode) {
                    ThemeMode.LIGHT -> ThemeMode.DARK
                    ThemeMode.DARK -> ThemeMode.LIGHT
                    ThemeMode.SYSTEM -> ThemeMode.LIGHT
                }
                themeManager.setThemeMode(newMode)
            }
        },
        modifier = modifier,
        containerColor = if (isDark) {
            Color(0xFF2A2A2A)
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (isDark) {
            Color(0xFF90CAF9)
        } else {
            MaterialTheme.colorScheme.primary
        }
    ) {
        Icon(
            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = "Toggle Theme"
        )
    }
}






