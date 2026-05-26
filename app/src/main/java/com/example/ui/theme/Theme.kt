package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WamDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    tertiary = AccentBlue,
    background = DarkBackground,
    surface = CardDarkBlue,
    onPrimary = OnPrimaryWhite,
    onSecondary = OnSecondaryGrey,
    onTertiary = OnPrimaryWhite,
    onBackground = OnPrimaryWhite,
    onSurface = OnPrimaryWhite,
    error = ErrorRed,
    errorContainer = ErrorRed,
    onErrorContainer = OnPrimaryWhite,
    surfaceVariant = CardDarkBlue,
    onSurfaceVariant = OnSecondaryGrey
)

@Composable
fun WamWalletTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WamDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep MyApplicationTheme alias as well to avoid breaking any other files or tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WamWalletTheme(content = content)
}
