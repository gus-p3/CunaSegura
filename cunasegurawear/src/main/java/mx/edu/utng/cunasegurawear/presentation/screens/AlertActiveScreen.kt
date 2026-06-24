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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.wear.compose.material.MaterialTheme
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
            .border(4.dp, MaterialTheme.colors.error, CircleShape), // Alert Red border
        contentAlignment = Alignment.Center
    ) {
        if (showMap) {
            MapCanvas(state.gpsAddress)
        } else {
            SosActiveContent(state.contactsNotified, state.activeActionLabel)
        }
    }
}

@Composable
fun SosActiveContent(contactsNotified: Int, activeActionLabel: String) {
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

    val errorColor = MaterialTheme.colors.error
    val onBackgroundColor = MaterialTheme.colors.onBackground

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        // Radar Waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = errorColor.copy(alpha = pulseAlpha1),
                radius = (size.minDimension / 2) * pulseScale1,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = errorColor.copy(alpha = pulseAlpha2),
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
                color = errorColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (activeActionLabel.isNotEmpty()) {
                Text(
                    activeActionLabel.uppercase(),
                    color = onBackgroundColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                "ENVIANDO SEÑAL...",
                fontSize = 10.sp,
                color = onBackgroundColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Contactos: $contactsNotified",
                fontSize = 9.sp,
                color = onBackgroundColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun MapCanvas(gpsAddress: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapAnimations")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )
    val markerPulse by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "markerPulse"
    )
    val markerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "markerAlpha"
    )

    val primaryColor = MaterialTheme.colors.primary
    val secondaryVariantColor = MaterialTheme.colors.secondaryVariant
    val errorColor = MaterialTheme.colors.error
    val onSurfaceColor = MaterialTheme.colors.onSurface
    val onBackgroundColor = MaterialTheme.colors.onBackground

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "UBICACIÓN GPS",
            fontSize = 11.sp,
            color = primaryColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 18.dp, bottom = 2.dp)
        )
        Canvas(modifier = Modifier.size(86.dp)) {
            val width = size.width
            val height = size.height

            // 1. Base map background circle
            drawCircle(color = secondaryVariantColor.copy(alpha = 0.6f))

            // 2. Curvy blue river representation
            val riverPath = Path().apply {
                moveTo(0f, height * 0.2f)
                cubicTo(width * 0.3f, height * 0.1f, width * 0.6f, height * 0.5f, width, height * 0.4f)
            }
            drawPath(
                path = riverPath,
                color = primaryColor.copy(alpha = 0.12f),
                style = Stroke(width = 8.dp.toPx())
            )

            // 3. Main highway (thick route)
            val mainRoad = Path().apply {
                moveTo(0f, height * 0.8f)
                quadraticTo(width * 0.5f, height * 0.7f, width, height * 0.1f)
            }
            drawPath(
                path = mainRoad,
                color = onSurfaceColor.copy(alpha = 0.22f),
                style = Stroke(width = 4.dp.toPx())
            )

            // 4. Secondary streets (diagonal thin grid)
            val streetColor = onSurfaceColor.copy(alpha = 0.1f)
            drawLine(streetColor, Offset(width * 0.2f, 0f), Offset(width * 0.8f, height), strokeWidth = 1.5.dp.toPx())
            drawLine(streetColor, Offset(0f, height * 0.6f), Offset(width, height * 0.9f), strokeWidth = 1.5.dp.toPx())
            drawLine(streetColor, Offset(width * 0.5f, 0f), Offset(width * 0.1f, height), strokeWidth = 1.5.dp.toPx())

            // 5. Compass markings (dashes around the rim)
            val tickCount = 12
            for (i in 0 until tickCount) {
                val angleDeg = i * (360f / tickCount)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val outerX = center.x + (width / 2) * Math.cos(angleRad).toFloat()
                val outerY = center.y + (height / 2) * Math.sin(angleRad).toFloat()
                val innerX = center.x + (width / 2 - 4.dp.toPx()) * Math.cos(angleRad).toFloat()
                val innerY = center.y + (height / 2 - 4.dp.toPx()) * Math.sin(angleRad).toFloat()
                drawLine(
                    color = primaryColor.copy(alpha = 0.35f),
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 6. Surrounding pale beacon points (other users or references)
            drawCircle(color = primaryColor.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = Offset(width * 0.3f, height * 0.45f))
            drawCircle(color = primaryColor.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = Offset(width * 0.75f, height * 0.65f))

            // 7. Radar sweep line and gradient tail
            val sweepRad = Math.toRadians(sweepAngle.toDouble())
            val sweepX = center.x + (width / 2) * Math.cos(sweepRad).toFloat()
            val sweepY = center.y + (height / 2) * Math.sin(sweepRad).toFloat()
            drawLine(
                color = primaryColor.copy(alpha = 0.3f),
                start = center,
                end = Offset(sweepX, sweepY),
                strokeWidth = 2.dp.toPx()
            )
            drawArc(
                color = primaryColor.copy(alpha = 0.08f),
                startAngle = sweepAngle - 30f,
                sweepAngle = 30f,
                useCenter = true
            )

            // 8. Pulsing accuracy ring
            drawCircle(
                color = primaryColor.copy(alpha = markerAlpha * 0.4f),
                radius = markerPulse.dp.toPx(),
                center = center
            )

            // 9. Central GPS active marker dot
            drawCircle(
                color = errorColor,
                radius = 4.dp.toPx(),
                center = center
            )
        }
        
        Text(
            text = gpsAddress.ifEmpty { "Buscando satélites..." },
            fontSize = 9.sp,
            color = onBackgroundColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp),
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
