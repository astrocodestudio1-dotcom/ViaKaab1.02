package com.example.viakaab.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val ViaKaabDarkColorScheme = darkColorScheme(
    primary = BrandEmerald,
    onPrimary = PureWhite,
    primaryContainer = BrandSkyDark,
    onPrimaryContainer = BrandSkyTint,
    secondary = BrandBlue,
    onSecondary = PureWhite,
    tertiary = BrandSky,
    onTertiary = PureWhite,
    background = Slate950,
    onBackground = PureWhite,
    surface = Slate900,
    onSurface = PureWhite,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    surfaceContainerLowest = Slate950,
    surfaceContainerLow = Slate900,
    surfaceContainer = Slate900,
    surfaceContainerHigh = Slate800,
    surfaceContainerHighest = Slate700,
    outline = Slate700,
    outlineVariant = Slate800,
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

val ViaKaabShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ViaKaabTheme(
    darkTheme: Boolean = true,
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
        shapes = ViaKaabShapes,
        content = content
    )
}
