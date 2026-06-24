package mx.edu.utng.cunasegurawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val WatchColorPalette = Colors(
    primary = Color(0xFF10B981),       // Neon Emerald Green
    primaryVariant = Color(0xFF059669),
    secondary = Color(0xFF3B82F6),     // Electric Blue
    secondaryVariant = Color(0xFF2563EB),
    background = Color(0xFF0C0E12),    // Premium deep graphite background
    onPrimary = Color.White,
    onSecondary = Color.White,
    onError = Color.White,
    error = Color(0xFFEF4444)          // Neon Alert Red
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
