package com.project.puk.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun SpotifyTheme(
    useDynamicColor: Boolean = false,
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            androidx.compose.material3.dynamicDarkColorScheme(context)
        }
        else -> androidx.compose.material3.darkColorScheme(
            primary = Color(0xFFFF6A00),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF331500),
            onPrimaryContainer = Color(0xFFFFDBC8),
            inversePrimary = Color(0xFF000000),
            secondary = Color(0xFFFF8A36),
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0xFF2A1508),
            onSecondaryContainer = Color(0xFFFFCCB3),
            tertiary = Color(0xFFFFB380),
            onTertiary = Color(0xFF181818),
            tertiaryContainer = Color(0xFF202020),
            onTertiaryContainer = Color(0xFFD6D6D6),
            outline = Color(0xFFFF6A00),
            outlineVariant = Color(0xFF442200)
        )
    }

    val colorScheme = if (amoled) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF0F0F0F),
            surfaceContainer = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainerHigh = Color(0xFF141414),
            surfaceContainerLowest = Color.Black,
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SairaTypography,
        content = content
    )
}
