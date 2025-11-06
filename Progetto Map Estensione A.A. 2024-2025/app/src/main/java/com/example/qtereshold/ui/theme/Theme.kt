package com.example.qtereshold.ui.theme // Assicurati che il package sia il tuo

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Questi sono i tuoi colori di fallback (personalizzali se vuoi)
private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFBB86FC),
    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC5),
    tertiary = androidx.compose.ui.graphics.Color(0xFF3700B3)
)

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6200EE),
    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC5),
    tertiary = androidx.compose.ui.graphics.Color(0xFF3700B3)
)

@Composable
fun QThresholdTheme( // O come si chiama il tuo tema (es. QTERESHOLDTheme)
    darkTheme: Boolean = isSystemInDarkTheme(),
    // --- MODIFICA 1: Aggiunto supporto colori dinamici ---
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Se dynamicColor è true E siamo su Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Altrimenti, usa i colori di fallback
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // --- FINE MODIFICA 1 ---

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assumendo che tu abbia un file Typography.kt
        content = content
    )
}