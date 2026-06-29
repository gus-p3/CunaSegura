package mx.edu.utng.cunasegurawear.presentation.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import mx.edu.utng.cunasegurawear.domain.model.AlertState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StatusScreen(
    state: AlertState,
    onSosClick: () -> Unit,
    onSimulate1Tap: () -> Unit,
    onSimulate2Taps: () -> Unit,
    onSimulate3Taps: () -> Unit,
    onSimulate4Taps: () -> Unit,
    onConfig: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val primaryColor = MaterialTheme.colors.primary
    val primaryVariantColor = MaterialTheme.colors.primaryVariant
    val errorColor = MaterialTheme.colors.error
    val onErrorColor = MaterialTheme.colors.onError
    val secondaryVariantColor = MaterialTheme.colors.secondaryVariant
    val onSecondaryColor = MaterialTheme.colors.onSecondary

    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            // ── Shield icon ──────────────────────────────────────────
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .padding(top = 24.dp, bottom = 4.dp)
                        .clickable { onSosClick() }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = primaryColor.copy(alpha = pulseAlpha),
                            radius = (size.minDimension / 2) * pulseScale,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                    Canvas(modifier = Modifier.size(64.dp)) {
                        val brush = Brush.radialGradient(
                            colors = listOf(primaryColor, primaryVariantColor),
                            center = center,
                            radius = size.minDimension / 2
                        )
                        drawCircle(brush = brush, radius = size.minDimension / 2)
                        val w = size.width
                        val h = size.height
                        val shieldPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.5f, h * 0.25f)
                            lineTo(w * 0.75f, h * 0.35f)
                            lineTo(w * 0.75f, h * 0.6f)
                            quadraticTo(w * 0.75f, h * 0.75f, w * 0.5f, h * 0.82f)
                            quadraticTo(w * 0.25f, h * 0.75f, w * 0.25f, h * 0.6f)
                            lineTo(w * 0.25f, h * 0.35f)
                            close()
                        }
                        drawPath(shieldPath, Color.White, style = Stroke(width = 3.dp.toPx()))
                        val checkPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.4f, h * 0.55f)
                            lineTo(w * 0.48f, h * 0.62f)
                            lineTo(w * 0.62f, h * 0.45f)
                        }
                        drawPath(checkPath, Color.White, style = Stroke(width = 3.dp.toPx()))
                    }
                }
            }

            // ── Status label ─────────────────────────────────────────
            item {
                Text(
                    text = "ESTADO SEGURO",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // ── Row 1: 1 Toque / 2 Toques ────────────────────────────
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSimulate1Tap,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = primaryColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(width = 72.dp, height = 32.dp)
                    ) {
                        Text("1 Toque", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSimulate2Taps,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = primaryColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(width = 72.dp, height = 32.dp)
                    ) {
                        Text("2 Toques", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Row 2: 3 Toques / 4 Toques ───────────────────────────
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSimulate3Taps,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = errorColor,
                            contentColor = onErrorColor
                        ),
                        modifier = Modifier.size(width = 72.dp, height = 32.dp)
                    ) {
                        Text("3 Toques", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSimulate4Taps,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = errorColor,
                            contentColor = onErrorColor
                        ),
                        modifier = Modifier.size(width = 72.dp, height = 32.dp)
                    ) {
                        Text("4 Toques", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Config button (siempre accesible con scroll) ──────────
            item {
                Button(
                    onClick = onConfig,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = secondaryVariantColor,
                        contentColor = onSecondaryColor
                    ),
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 16.dp)
                        .size(width = 120.dp, height = 36.dp)
                ) {
                    Text("⚙️ Configuración", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
