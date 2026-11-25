package com.example.viakaab.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ViaKaabDarkColorScheme = darkColorScheme(
    primary = BrandSky,
    onPrimary = PureWhite,
    primaryContainer = BrandSkyDark,
    onPrimaryContainer = BrandSkyTint,
    secondary = BrandBlue,
    onSecondary = PureWhite,
    tertiary = BrandEmerald,
    onTertiary = PureWhite,
    background = Slate900,
    onBackground = PureWhite,
    surface = Slate800,
    onSurface = PureWhite,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400,
    error = BrandRed,
    onError = PureWhite
)

private val ViaKaabLightColorScheme = lightColorScheme(
    primary = Slate900,
    onPrimary = PureWhite,
    primaryContainer = BrandSkyTint,
    onPrimaryContainer = Slate900,
    secondary = BrandSky,
    onSecondary = PureWhite,
    tertiary = BrandEmerald,
    onTertiary = PureWhite,
    background = LightSurface,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate200,
    onSurfaceVariant = Slate600,
    error = BrandRed,
    onError = PureWhite
)

@Composable
fun ViaKaabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ViaKaabDarkColorScheme
        else -> ViaKaabLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ViaKaabTypography,
        content = content
    )
}
