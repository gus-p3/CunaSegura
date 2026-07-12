package mx.edu.utng.cunasegura.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color definitions
private val RojoSOS = Color(0xFFD32F2F)
private val AzulCunaSegura = Color(0xFF1F4E79)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEmergency: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(context)
    )

    val usuarioState = viewModel.usuario.collectAsState()
    val alertaCreada by viewModel.alertaCreada.collectAsState()
    val alertaId by viewModel.alertaId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showLocationDialog by remember { mutableStateOf(false) }
    var currentGpsCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentGpsCoords = Pair(location.latitude, location.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        }
    }

    val obtenerCoordenadasActuales = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentGpsCoords = Pair(location.latitude, location.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Sustained press state
    var isPressed by remember { mutableStateOf(false) }
    var pressProgress by remember { mutableFloatStateOf(0f) }

    // Listen for alert creation to navigate
    LaunchedEffect(alertaCreada, alertaId) {
        if (alertaCreada && alertaId != null) {
            onNavigateToEmergency(alertaId!!)
            viewModel.resetAlertaState()
        }
    }

    // Timer logic for sustained 3 seconds press
    LaunchedEffect(isPressed) {
        if (isPressed) {
            val startTime = System.currentTimeMillis()
            while (isPressed && pressProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                pressProgress = (elapsed / 3000f).coerceIn(0f, 1f)
                if (pressProgress >= 1f) {
                    viewModel.activarSOS()
                }
                delay(16) // ~60fps updates
            }
        } else {
            pressProgress = 0f
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cuna Segura",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulCunaSegura
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(paddingValues)
                .background(Color(0xFFF7F9FC))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bienvenida
            val userName = usuarioState.value?.nombre ?: "Vecino"
            Text(
                text = "¡Hola, $userName!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Botón SOS con su indicador de progreso circular
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                if (pressProgress > 0f) {
                    CircularProgressIndicator(
                        progress = { pressProgress },
                        modifier = Modifier.size(224.dp),
                        color = RojoSOS,
                        strokeWidth = 6.dp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(RojoSOS)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        isPressed = false
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerta SOS",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mantén 3 seg",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Mensaje informativo
            Text(
                text = "Envía ubicación a Contactos y Servicios",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid de 2x2 con servicios convertido a Rows estáticas
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceCard(
                            title = "911 / Policía",
                            icon = Icons.Default.Warning,
                            color = RojoSOS,
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_DIAL,
                                    android.net.Uri.parse("tel:911")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceCard(
                            title = "Ambulancia IMSS",
                            icon = Icons.Default.Favorite,
                            color = Color(0xFFE91E63),
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_DIAL,
                                    android.net.Uri.parse("tel:800-623-2323")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceCard(
                            title = "Bomberos Dto.",
                            icon = Icons.Default.Star,
                            color = Color(0xFFFF9800),
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_DIAL,
                                    android.net.Uri.parse("tel:068")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceCard(
                            title = "Mi Ubicación",
                            icon = Icons.Default.LocationOn,
                            color = Color(0xFF4CAF50),
                            onClick = {
                                obtenerCoordenadasActuales()
                                showLocationDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Dialogo para mostrar Mi Ubicación Real
        if (showLocationDialog) {
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                title = { Text("Mi Ubicación Actual") },
                text = {
                    Text(
                        if (currentGpsCoords != null) {
                            "Latitud: ${currentGpsCoords!!.first}\nLongitud: ${currentGpsCoords!!.second}"
                        } else {
                            "Obteniendo coordenadas del GPS..."
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showLocationDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}
