package mx.edu.utng.cunasegura.presentation.emergency

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val FondoRosa = Color(0xFFFFCDD2)
private val RojoSOS = Color(0xFFD32F2F)
private val AzulCunaSegura = Color(0xFF1F4E79)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyActiveScreen(
    alertaId: Int,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EmergencyViewModel = viewModel(
        factory = EmergencyViewModelFactory(context, alertaId)
    )

    val alerta by viewModel.alerta.collectAsState()
    val contactCount by viewModel.contactCount.collectAsState()
    val secondsLeft by viewModel.secondsLeft.collectAsState()
    val alertaCancelada by viewModel.alertaCancelada.collectAsState()

    // Maneja la navegación cuando la alerta se cancela con éxito
    LaunchedEffect(alertaCancelada) {
        if (alertaCancelada) {
            onBackToHome()
        }
    }

    // Animación de escala para el botón de SOS activo (pulso)
    val infiniteTransition = rememberInfiniteTransition(label = "PulsoSOS")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Escala"
    )

    Scaffold(
        containerColor = FondoRosa
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Cabecera de estado
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "EMERGENCIA ACTIVA",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = RojoSOS,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tu alerta está siendo procesada",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            // Círculo SOS Animado con Ondas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .scale(scale)
            ) {
                // Anillo exterior de onda decorativo
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(RojoSOS.copy(alpha = 0.15f))
                )
                // Círculo principal del botón
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(RojoSOS),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerta activa",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Información de envío y contactos
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Alerta enviada a $contactCount contactos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulCunaSegura,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // GPS Coordinates row
                        val lat = alerta?.latitud ?: 0.0
                        val lng = alerta?.longitud ?: 0.0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AzulCunaSegura.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ubicación GPS",
                                tint = AzulCunaSegura,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GPS: $lat, $lng",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulCunaSegura
                            )
                        }
                    }
                }
            }

            // Countdown timer & Cancel button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (secondsLeft > 0) {
                    Text(
                        text = "Cancelar en ${secondsLeft}s",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Button(
                        onClick = { viewModel.cancelarAlerta() },
                        colors = ButtonDefaults.buttonColors(containerColor = RojoSOS),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar ahora",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancelar ahora",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Alerta confirmada
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RojoSOS),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "ALERTA CONFIRMADA\nAyuda en camino",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
