package mx.edu.utng.cunasegura.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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

private val AzulCunaSegura = Color(0xFF1F4E79)
private val RojoSOS = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEmergency: (Long) -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToMap: () -> Unit
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

    // Sustained press state
    var isPressed by remember { mutableStateOf(false) }
    var pressProgress by remember { mutableFloatStateOf(0f) }

    // Listen for alert creation to navigate
    LaunchedEffect(alertaCreada, alertaId) {
        if (alertaCreada && alertaId != null) {
            onNavigateToEmergency(alertaId!!.toLong())
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Ya estamos en Home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToContacts,
                    icon = { Icon(Icons.Default.Call, contentDescription = "Contactos") },
                    label = { Text("Contactos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToDevices,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Dispositivos") },
                    label = { Text("Dispositivos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMap,
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                    label = { Text("Mapa") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F9FC))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Bienvenida
            val userName = usuarioState.value?.nombre ?: "Vecino"
            Text(
                text = "¡Hola, $userName!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AzulCunaSegura,
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

            // Grid de 2x2 con servicios
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ServiceCard(
                        title = "911 / Policía",
                        icon = Icons.Default.Warning,
                        color = RojoSOS,
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Función disponible próximamente")
                            }
                        }
                    )
                }
                item {
                    ServiceCard(
                        title = "Ambulancia IMSS",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFE91E63),
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Función disponible próximamente")
                            }
                        }
                    )
                }
                item {
                    ServiceCard(
                        title = "Bomberos Dto.",
                        icon = Icons.Default.Star,
                        color = Color(0xFFFF9800),
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Función disponible próximamente")
                            }
                        }
                    )
                }
                item {
                    ServiceCard(
                        title = "Mi Ubicación",
                        icon = Icons.Default.LocationOn,
                        color = Color(0xFF4CAF50),
                        onClick = {
                            showLocationDialog = true
                        }
                    )
                }
            }
        }

        // Dialogo para mostrar Mi Ubicación desde Room
        if (showLocationDialog) {
            val user = usuarioState.value
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                title = { Text("Mi Ubicación Actual") },
                text = {
                    Text(
                        if (user != null) {
                            "Latitud: ${user.latActual}\nLongitud: ${user.lonActual}"
                        } else {
                            "Cargando coordenadas del usuario..."
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
