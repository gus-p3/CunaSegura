package mx.edu.utng.cunasegurawear.presentation.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import mx.edu.utng.cunasegurawear.domain.model.AlertState

@Composable
fun AlertActiveScreen(state: AlertState) {
    var showMap by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            showMap = !showMap
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(4.dp, Color(0xFFEF4444), CircleShape), // Neon Alert Red border
        contentAlignment = Alignment.Center
    ) {
        if (showMap) {
            MapCanvas()
        } else {
            SosActiveContent(state.contactsNotified)
        }
    }
}

@Composable
fun SosActiveContent(contactsNotified: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 0),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 0),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        // Radar Waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = pulseAlpha1),
                radius = (size.minDimension / 2) * pulseScale1,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = pulseAlpha2),
                radius = (size.minDimension / 2) * pulseScale2,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "SOS ACTIVO",
                color = Color(0xFFEF4444),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "ENVIANDO SEÑAL...",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Contactos: $contactsNotified",
                fontSize = 9.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun MapCanvas() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "UBICACIÓN GPS",
            fontSize = 11.sp,
            color = Color(0xFF10B981),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
        )
        Canvas(modifier = Modifier.size(75.dp)) {
            // Draw dark-mode styled base map background
            drawCircle(color = Color(0xFF1E293B))

            // Draw neon street grid
            val width = size.width
            val height = size.height
            val streetColor = Color(0xFF334155)

            // Horizontal Streets
            drawLine(streetColor, androidx.compose.ui.geometry.Offset(0f, height * 0.3f), androidx.compose.ui.geometry.Offset(width, height * 0.3f), strokeWidth = 3.dp.toPx())
            drawLine(streetColor, androidx.compose.ui.geometry.Offset(0f, height * 0.7f), androidx.compose.ui.geometry.Offset(width, height * 0.7f), strokeWidth = 3.dp.toPx())

            // Vertical Streets
            drawLine(streetColor, androidx.compose.ui.geometry.Offset(width * 0.3f, 0f), androidx.compose.ui.geometry.Offset(width * 0.3f, height), strokeWidth = 3.dp.toPx())
            drawLine(streetColor, androidx.compose.ui.geometry.Offset(width * 0.7f, 0f), androidx.compose.ui.geometry.Offset(width * 0.7f, height), strokeWidth = 3.dp.toPx())

            // Draw a central pulsing GPS marker
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.3f),
                radius = 16f,
                center = center
            )
            drawCircle(
                color = Color(0xFFEF4444), // Red marker dot
                radius = 6f,
                center = center
            )
        }
    }
}
