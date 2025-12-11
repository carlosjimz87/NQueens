package com.carlosjimz87.nqueens.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LightColorScheme = lightColorScheme(
    primary = GoldCrown,
    onPrimary = Color(0xFF3A2A00),
    primaryContainer = Color(0xFFFFE38F),
    onPrimaryContainer = Color(0xFF2A1D00),

    secondary = ChessBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE3FF),
    onSecondaryContainer = Color(0xFF101B48),

    tertiary = GlowBlue,
    onTertiary = Color(0xFF0D1330),
    tertiaryContainer = Color(0xFFDEE4FF),
    onTertiaryContainer = Color(0xFF0D1330),

    background = Color(0xFFF8F8FF),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    surfaceVariant = Color(0xFFE3E6F5),
    onSurfaceVariant = Color(0xFF444B63),
)

val DarkColorScheme = darkColorScheme(
    primary = GoldCrown,
    onPrimary = Color(0xFF3A2A00),
    primaryContainer = Color(0xFF523C00),
    onPrimaryContainer = Color(0xFFFFE38F),

    secondary = Color(0xFFC1C6FF),
    onSecondary = Color(0xFF1A234F),
    secondaryContainer = Color(0xFF343F70),
    onSecondaryContainer = Color(0xFFDDE3FF),

    tertiary = GlowBlue,
    onTertiary = Color(0xFF0D1330),
    tertiaryContainer = Color(0xFF313D6B),
    onTertiaryContainer = Color(0xFFDEE4FF),

    background = RoyalBlueDark,
    onBackground = Color(0xFFE3E6F5),
    surface = Color(0xFF1F2947),
    surfaceVariant = RoyalBlue,
    onSurfaceVariant = Color(0xFFCED3EA),
)

@Composable
fun NQueensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}