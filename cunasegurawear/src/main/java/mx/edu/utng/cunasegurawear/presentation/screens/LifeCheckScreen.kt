package mx.edu.utng.cunasegurawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text

@Composable
fun LifeCheckScreen(
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val primaryColor = MaterialTheme.colors.primary
    val onPrimaryColor = MaterialTheme.colors.onPrimary
    val errorColor = MaterialTheme.colors.error
    val onErrorColor = MaterialTheme.colors.onError
    val onBackgroundColor = MaterialTheme.colors.onBackground

    Scaffold {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(onBackgroundColor.copy(alpha = 0.08f)) // Semitransparent floating card
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "CHECK DE VIDA",
                    fontSize = 11.sp,
                    color = onBackgroundColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "¿ESTÁS BIEN?",
                    color = errorColor, // Warning Red from theme
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SÍ button (brand primary cian)
                Button(
                    onClick = onYes,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = primaryColor,
                        contentColor = onPrimaryColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .size(height = 42.dp, width = 0.dp)
                ) {
                    Text("SÍ", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }

                // NO button (brand error red)
                Button(
                    onClick = onNo,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = errorColor,
                        contentColor = onErrorColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .size(height = 42.dp, width = 0.dp)
                ) {
                    Text("NO", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
