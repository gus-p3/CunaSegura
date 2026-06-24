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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
            delay(4000L) // Alterna entre detalles del evento y mapa cada 4 segundos
            showMap = !showMap
        }
    }
    
    val borderColor = when (state.activeActionName) {
        "MENSAJE_SMS" -> Color(0xFF4CAF50) // Verde
        "UBICACION_TIEMPO_REAL" -> Color(0xFF2196F3) // Azul
        "ALARMA_TV" -> Color(0xFFFF9800) // Naranja
        "LLAMAR_911" -> Color(0xFFF44336) // Rojo
        else -> MaterialTheme.colors.error
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(4.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (showMap && state.isGpsActive) {
            MapCanvas(state.gpsAddress)
        } else {
            when (state.activeActionName) {
                "MENSAJE_SMS" -> SmsActiveContent(state.contactsNotified)
                "UBICACION_TIEMPO_REAL" -> GpsActiveContent(state.gpsAddress)
                "ALARMA_TV" -> BocinaActiveContent()
                "LLAMAR_911" -> Call911ActiveContent()
                else -> SosActiveContent(state.contactsNotified, state.activeActionLabel)
            }
        }
    }
}

@Composable
fun SmsActiveContent(contactsNotified: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "sms")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "smsPulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "smsAlpha"
    )

    val smsColor = Color(0xFF4CAF50)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = smsColor.copy(alpha = pulseAlpha),
                radius = (size.minDimension / 2) * pulseScale,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Canvas(modifier = Modifier.size(44.dp)) {
                val w = size.width
                val h = size.height
                // Dibujar burbuja de diálogo
                val path = Path().apply {
                    moveTo(w * 0.1f, h * 0.2f)
                    lineTo(w * 0.9f, h * 0.2f)
                    lineTo(w * 0.9f, h * 0.7f)
                    lineTo(w * 0.4f, h * 0.7f)
                    lineTo(w * 0.2f, h * 0.9f)
                    lineTo(w * 0.2f, h * 0.7f)
                    lineTo(w * 0.1f, h * 0.7f)
                    close()
                }
                drawPath(path = path, color = smsColor, style = Stroke(width = 3.dp.toPx()))
                // Dibujar líneas interiores de texto
                drawLine(smsColor, Offset(w * 0.25f, h * 0.35f), Offset(w * 0.75f, h * 0.35f), strokeWidth = 3.dp.toPx())
                drawLine(smsColor, Offset(w * 0.25f, h * 0.5f), Offset(w * 0.65f, h * 0.5f), strokeWidth = 3.dp.toPx())
            }

            Text(
                "SMS ENVIADO",
                color = smsColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "SMS de auxilio enviado",
                fontSize = 9.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.8f)
            )
            Text(
                "Contactos: $contactsNotified",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = smsColor.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun GpsActiveContent(gpsAddress: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "gps")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpsPulse"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpsAlpha"
    )

    val gpsColor = Color(0xFF2196F3)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = gpsColor.copy(alpha = radarAlpha),
                radius = (size.minDimension / 2) * radarPulse,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Canvas(modifier = Modifier.size(44.dp)) {
                val w = size.width
                val h = size.height
                // Círculos concéntricos que representan GPS
                drawCircle(color = gpsColor, radius = w * 0.15f)
                drawCircle(color = gpsColor, radius = w * 0.35f, style = Stroke(width = 2.dp.toPx()))
                drawCircle(color = gpsColor, radius = w * 0.5f, style = Stroke(width = 1.5.dp.toPx()))
                // Antena central
                drawLine(gpsColor, Offset(w * 0.5f, h * 0.15f), Offset(w * 0.5f, h * 0.9f), strokeWidth = 3.dp.toPx())
                // Base
                drawLine(gpsColor, Offset(w * 0.3f, h * 0.9f), Offset(w * 0.7f, h * 0.9f), strokeWidth = 3.dp.toPx())
            }

            Text(
                "COMPARTIENDO GPS",
                color = gpsColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Ubicación en tiempo real",
                fontSize = 9.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.8f)
            )
            Text(
                gpsAddress.ifEmpty { "Calculando ruta..." },
                fontSize = 8.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun BocinaActiveContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "bocina")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "bocinaPulse"
    )
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "bocinaAlpha"
    )

    val alarmColor = Color(0xFFFF9800)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = alarmColor.copy(alpha = waveAlpha),
                radius = (size.minDimension / 2) * waveScale,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Canvas(modifier = Modifier.size(44.dp)) {
                val w = size.width
                val h = size.height
                // Dibujar megáfono/bocina
                val hornPath = Path().apply {
                    moveTo(w * 0.2f, h * 0.35f)
                    lineTo(w * 0.45f, h * 0.35f)
                    lineTo(w * 0.75f, h * 0.15f)
                    lineTo(w * 0.75f, h * 0.85f)
                    lineTo(w * 0.45f, h * 0.65f)
                    lineTo(w * 0.2f, h * 0.65f)
                    close()
                }
                drawPath(path = hornPath, color = alarmColor)
                // Dibujar mango
                drawRoundRect(
                    color = alarmColor,
                    topLeft = Offset(w * 0.3f, h * 0.65f),
                    size = Size(w * 0.15f, h * 0.2f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                // Ondas sonoras
                drawArc(
                    color = alarmColor,
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(w * 0.6f, h * 0.25f),
                    size = Size(w * 0.3f, h * 0.5f),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            Text(
                "BOCINA VECINAL",
                color = alarmColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Alarma comunitaria activa",
                fontSize = 9.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.8f)
            )
            Text(
                "Sirena encendida",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = alarmColor.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun Call911ActiveContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "call911")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "911Pulse"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "911Alpha"
    )

    val red911Color = Color(0xFFF44336)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = red911Color.copy(alpha = ringAlpha),
                radius = (size.minDimension / 2) * ringScale,
                style = Stroke(width = 4.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Canvas(modifier = Modifier.size(44.dp)) {
                val w = size.width
                val h = size.height
                
                // Dibujar escudo de emergencias
                val shieldPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.1f)
                    lineTo(w * 0.85f, h * 0.22f)
                    lineTo(w * 0.85f, h * 0.55f)
                    quadraticTo(w * 0.85f, h * 0.75f, w * 0.5f, h * 0.9f)
                    quadraticTo(w * 0.15f, h * 0.75f, w * 0.15f, h * 0.55f)
                    lineTo(w * 0.15f, h * 0.22f)
                    close()
                }
                drawPath(path = shieldPath, color = red911Color)
                
                // Dibujar auricular de teléfono en blanco dentro del escudo
                val phonePath = Path().apply {
                    moveTo(w * 0.35f, h * 0.4f)
                    quadraticTo(w * 0.5f, h * 0.35f, w * 0.65f, h * 0.4f)
                    lineTo(w * 0.68f, h * 0.5f)
                    quadraticTo(w * 0.58f, h * 0.46f, w * 0.57f, h * 0.54f)
                    lineTo(w * 0.54f, h * 0.66f)
                    quadraticTo(w * 0.52f, h * 0.72f, w * 0.42f, h * 0.68f)
                    close()
                }
                drawPath(path = phonePath, color = Color.White)
            }

            Text(
                "LLAMADA AL 911",
                color = red911Color,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Marcando a Emergencias",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            )
            Text(
                "Mantén la calma",
                fontSize = 8.sp,
                color = red911Color.copy(alpha = 0.9f)
            )
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

            drawCircle(color = secondaryVariantColor.copy(alpha = 0.6f))

            val riverPath = Path().apply {
                moveTo(0f, height * 0.2f)
                cubicTo(width * 0.3f, height * 0.1f, width * 0.6f, height * 0.5f, width, height * 0.4f)
            }
            drawPath(
                path = riverPath,
                color = primaryColor.copy(alpha = 0.12f),
                style = Stroke(width = 8.dp.toPx())
            )

            val mainRoad = Path().apply {
                moveTo(0f, height * 0.8f)
                quadraticTo(width * 0.5f, height * 0.7f, width, height * 0.1f)
            }
            drawPath(
                path = mainRoad,
                color = onSurfaceColor.copy(alpha = 0.22f),
                style = Stroke(width = 4.dp.toPx())
            )

            val streetColor = onSurfaceColor.copy(alpha = 0.1f)
            drawLine(streetColor, Offset(width * 0.2f, 0f), Offset(width * 0.8f, height), strokeWidth = 1.5.dp.toPx())
            drawLine(streetColor, Offset(0f, height * 0.6f), Offset(width, height * 0.9f), strokeWidth = 1.5.dp.toPx())
            drawLine(streetColor, Offset(width * 0.5f, 0f), Offset(width * 0.1f, height), strokeWidth = 1.5.dp.toPx())

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

            drawCircle(color = primaryColor.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = Offset(width * 0.3f, height * 0.45f))
            drawCircle(color = primaryColor.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = Offset(width * 0.75f, height * 0.65f))

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

            drawCircle(
                color = primaryColor.copy(alpha = markerAlpha * 0.4f),
                radius = markerPulse.dp.toPx(),
                center = center
            )

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
