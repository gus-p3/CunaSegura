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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import mx.edu.utng.cunasegurawear.domain.model.AlertState

@Composable
fun StatusScreen(
    state: AlertState,
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

    Scaffold(timeText = { TimeText() }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .padding(bottom = 8.dp)
            ) {
                // Pulsing safety field ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = pulseAlpha),
                        radius = (size.minDimension / 2) * pulseScale,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Central shield button
                Canvas(modifier = Modifier.size(64.dp)) {
                    val brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF34D399), Color(0xFF059669)),
                        center = center,
                        radius = size.minDimension / 2
                    )
                    drawCircle(
                        brush = brush,
                        radius = size.minDimension / 2
                    )

                    // Stylized shield drawing path
                    val width = size.width
                    val height = size.height
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(width * 0.5f, height * 0.25f)
                        lineTo(width * 0.75f, height * 0.35f)
                        lineTo(width * 0.75f, height * 0.6f)
                        quadraticTo(width * 0.75f, height * 0.75f, width * 0.5f, height * 0.82f)
                        quadraticTo(width * 0.25f, height * 0.75f, width * 0.25f, height * 0.6f)
                        lineTo(width * 0.25f, height * 0.35f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw inner checkmark
                    val checkPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(width * 0.4f, height * 0.55f)
                        lineTo(width * 0.48f, height * 0.62f)
                        lineTo(width * 0.62f, height * 0.45f)
                    }
                    drawPath(
                        path = checkPath,
                        color = Color.White,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Text("ESTADO SEGURO", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            // Row with the buttons to simulate 3 and 4 taps
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSimulate3Taps,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(width = 64.dp, height = 32.dp)
                ) {
                    Text("3 Toques", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSimulate4Taps,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(width = 64.dp, height = 32.dp)
                ) {
                    Text("4 Toques", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Settings button to access config without relying on physical hardware button
            Button(
                onClick = onConfig,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF1E293B),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(32.dp)
            ) {
                Text("⚙️", fontSize = 12.sp)
            }
        }
    }
}
