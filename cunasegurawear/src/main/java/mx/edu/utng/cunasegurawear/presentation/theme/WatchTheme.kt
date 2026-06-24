package mx.edu.utng.cunasegurawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

// Mobile app dark theme color tokens mapped to Wear OS palette
val primaryDark = Color(0xFF85D1E8)
val onPrimaryDark = Color(0xFF003641)
val primaryContainerDark = Color(0xFF004E5D)
val onPrimaryContainerDark = Color(0xFFAFECFF)
val secondaryDark = Color(0xFFB2CBD3)
val onSecondaryDark = Color(0xFF1D343A)
val secondaryContainerDark = Color(0xFF344A51)
val onSecondaryContainerDark = Color(0xFFCEE7EF)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val backgroundDark = Color(0xFF0F1416)
val onBackgroundDark = Color(0xFFDEE3E6)
val surfaceDark = Color(0xFF0F1416)
val onSurfaceDark = Color(0xFFDEE3E6)

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
