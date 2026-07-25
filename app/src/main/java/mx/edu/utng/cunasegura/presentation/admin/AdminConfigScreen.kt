package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfigScreen() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val network by viewModel.network.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var radioMaximo by remember { mutableFloatStateOf(200f) }
    var tipoRed by remember { mutableStateOf("Abierta (GPS)") }
    var tiempoAntiFalsa by remember { mutableFloatStateOf(5f) }
    var checkVida by remember { mutableFloatStateOf(2f) }
    var esperarDiasNuevos by remember { mutableFloatStateOf(0f) }
    var tiempoVidaAlerta by remember { mutableFloatStateOf(720f) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Sync state with loaded network
    LaunchedEffect(network) {
        network?.let {
            radioMaximo = it.radio.toFloat()
            tipoRed = if (it.tipo == "Abierta") "Abierta (GPS)" else "Cerrada (Solo QR)"
            tiempoAntiFalsa = it.tiempoAntiFalsa.toFloat()
            checkVida = it.checkVida.toFloat()
            esperarDiasNuevos = it.esperarDiasNuevos.toFloat()
        }
    }
    
    val globalVidaAlerta by viewModel.tiempoVidaAlerta.collectAsState()
    LaunchedEffect(globalVidaAlerta) {
        tiempoVidaAlerta = globalVidaAlerta.toFloat()
    }

    // Show status messages in Snackbar
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearStatusMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración de la Red", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ajustes Globales y Políticas", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Tipo de Red
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tipo de Red", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = tipoRed == "Abierta (GPS)",
                                onClick = { tipoRed = "Abierta (GPS)" },
                                colors = RadioButtonDefaults.colors(selectedColor = AzulCunaSegura)
                            )
                            Text("Abierta (GPS)", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = tipoRed == "Cerrada (Solo QR)",
                                onClick = { tipoRed = "Cerrada (Solo QR)" },
                                colors = RadioButtonDefaults.colors(selectedColor = AzulCunaSegura)
                            )
                            Text("Cerrada (Solo QR)", fontSize = 14.sp)
                        }
                    }

                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Radio Máximo
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Radio Máximo de Cobertura", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${radioMaximo.toInt()} m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = radioMaximo,
                            onValueChange = { radioMaximo = it },
                            valueRange = 50f..500f,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                    }

                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Tiempo Anti Falsas Alarmas
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tiempo anti-falsas alarmas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${tiempoAntiFalsa.toInt()} s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = tiempoAntiFalsa,
                            onValueChange = { tiempoAntiFalsa = it },
                            valueRange = 3f..10f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                    }
                    
                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Check de vida
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check de vida cada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${checkVida.toInt()} min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = checkVida,
                            onValueChange = { checkVida = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                    }

                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Esperar días para nuevos (Políticas de Abuso)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Espera para nuevos miembros", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${esperarDiasNuevos.toInt()} días", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = esperarDiasNuevos,
                            onValueChange = { esperarDiasNuevos = it },
                            valueRange = 0f..7f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                        Text(
                            "Evita el envío inmediato de SOS a usuarios recién incorporados para prevenir el spam y abuso.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Tiempo de vida de la alerta
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tiempo vida de alerta (Global)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${tiempoVidaAlerta.toInt()} min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = tiempoVidaAlerta,
                            onValueChange = { tiempoVidaAlerta = it },
                            valueRange = 1f..1440f,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                        Text(
                            "Oculta del mapa las alertas automáticamente una vez transcurrido este tiempo (1 a 1440 minutos).",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.guardarRedConfig(
                        tipo = if (tipoRed == "Abierta (GPS)") "Abierta" else "Cerrada",
                        radio = radioMaximo.toDouble(),
                        tiempoAntiFalsa = tiempoAntiFalsa.toDouble(),
                        checkVida = checkVida.toDouble(),
                        esperarDiasNuevos = esperarDiasNuevos.toInt(),
                        tiempoVidaAlerta = tiempoVidaAlerta.toDouble()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura)
            ) {
                Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
