package mx.edu.utng.cunasegurawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

// Mobile app dark theme color tokens mapped to Wear OS palette
val primaryDark = Color(0xFFAAC7FF)
val onPrimaryDark = Color(0xFF0A305F)
val primaryContainerDark = Color(0xFF274777)
val onPrimaryContainerDark = Color(0xFFD6E3FF)
val secondaryDark = Color(0xFFBEC6DC)
val onSecondaryDark = Color(0xFF283141)
val secondaryContainerDark = Color(0xFF3E4759)
val onSecondaryContainerDark = Color(0xFFDAE2F9)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val backgroundDark = Color(0xFF111318)
val onBackgroundDark = Color(0xFFE2E2E9)
val surfaceDark = Color(0xFF111318)
val onSurfaceDark = Color(0xFFE2E2E9)

val WatchColorPalette = Colors(
    primary = primaryDark,
    primaryVariant = primaryContainerDark,
    secondary = secondaryDark,
    secondaryVariant = secondaryContainerDark,
    background = backgroundDark,
    surface = surfaceDark,
    error = errorDark,
    onPrimary = onPrimaryDark,
    onSecondary = onSecondaryDark,
    onBackground = onBackgroundDark,
    onSurface = onSurfaceDark,
    onError = onErrorDark
)

@Composable
fun WatchTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WatchColorPalette,
        content = content
    )
}
