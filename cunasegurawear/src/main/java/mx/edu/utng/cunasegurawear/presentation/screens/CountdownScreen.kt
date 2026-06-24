package mx.edu.utng.cunasegurawear.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text

@Composable
fun CountdownScreen(
    seconds: Int,
    onCancel: () -> Unit
) {
    val animatedSeconds by animateIntAsState(targetValue = seconds, label = "secondsAnimation")
    
    // Smooth progress from 5s to 0s
    val progressTarget = if (seconds == 0) 0f else seconds / 5f
    val animatedProgress by animateFloatAsState(targetValue = progressTarget, label = "progressAnimation")

    Scaffold {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                "ALERTA EN PROCESO",
                fontSize = 11.sp,
                color = Color(0xFFF59E0B), // Warning Orange Accent
                fontWeight = FontWeight.SemiBold
            )
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize(),
                    startAngle = 270f,
                    indicatorColor = Color(0xFFEF4444), // Alert Red
                    trackColor = Color.DarkGray.copy(alpha = 0.5f),
                    strokeWidth = 4.dp
                )
                Text(
                    text = "${animatedSeconds}s",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF1E293B), // Premium dark gray-blue button
                    contentColor = Color.White
                ),
                modifier = Modifier.size(width = 110.dp, height = 36.dp)
            ) {
                Text("CANCELAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
