package mx.edu.utng.cunasegura.presentation.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.edu.utng.cunasegura.domain.model.Alerta
import java.text.SimpleDateFormat
import java.util.*

private val AzulCunaSegura = Color(0xFF1F4E79)
private val RojoSOS = Color(0xFFD32F2F)
// Dolores Hidalgo, Gto. — posición por defecto
private val DEFAULT_LOCATION = LatLng(21.1565, -100.9327)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMapScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToDevices: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel(factory = MapViewModelFactory(context))

    val usuario by viewModel.usuario.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()

    // Solicitar permiso de ubicación en runtime
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Estado de la cámara del mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, 14f)
    }

    // Centra la cámara cuando se carga la ubicación del usuario
    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(loc, 15f),
                durationMs = 800
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GPS — Mapa Comunitario", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
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
                    selected = true,
                    onClick = { /* Ya estamos aquí */ },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                    label = { Text("Mapa") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- Mapa (60% de la pantalla) ---
            Box(modifier = Modifier.weight(0.6f).fillMaxWidth()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = locationPermissionGranted
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = locationPermissionGranted,
                        zoomControlsEnabled = true
                    )
                ) {
                    // Marcador verde del usuario actual
                    userLocation?.let { loc ->
                        Marker(
                            state = MarkerState(position = loc),
                            title = "Yo — ${usuario?.nombre ?: "Vecino"}",
                            snippet = "Mi ubicación actual",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN
                            )
                        )
                    }

                    // Marcadores rojos de alertas activas
                    activeAlerts.forEach { alerta ->
                        val alertaPos = LatLng(alerta.latitud, alerta.longitud)
                        if (alerta.latitud != 0.0 || alerta.longitud != 0.0) {
                            Marker(
                                state = MarkerState(position = alertaPos),
                                title = "🔴 Alerta SOS",
                                snippet = "Estado: ${alerta.estado}",
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED
                                )
                            )
                        }
                    }
                }

                // Leyenda sobre el mapa
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeyendaItem("🟢", "Yo")
                        LeyendaItem("🔴", "SOS")
                        LeyendaItem("✅", "OK")
                    }
                }
            }

            // --- Info de actualización ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulCunaSegura.copy(alpha = 0.07f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AzulCunaSegura,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Ubicación activa cada 3m",
                    fontSize = 12.sp,
                    color = AzulCunaSegura,
                    fontWeight = FontWeight.Medium
                )
            }

            // --- Lista de alertas activas (40% de la pantalla) ---
            if (activeAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sin alertas activas",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Todo está en calma en tu zona",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.weight(0.4f)) {
                    Text(
                        text = "Alertas activas (${activeAlerts.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RojoSOS,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeAlerts) { alerta ->
                            AlertaItem(
                                alerta = alerta,
                                nombreUsuario = usuario?.nombre ?: "Vecino",
                                telefonoUsuario = usuario?.telefono ?: "",
                                onLlamarUsuario = { telefono ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono"))
                                    context.startActivity(intent)
                                },
                                onLlamar911 = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertaItem(
    alerta: Alerta,
    nombreUsuario: String,
    telefonoUsuario: String,
    onLlamarUsuario: (String) -> Unit,
    onLlamar911: () -> Unit
) {
    val iniciales = nombreUsuario.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    val horaFormateada = remember(alerta.creadoEn) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(alerta.creadoEn))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con iniciales
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(RojoSOS),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(nombreUsuario, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "🔴 SOS activo · $horaFormateada",
                    fontSize = 12.sp,
                    color = RojoSOS
                )
                Text(
                    "GPS: ${alerta.latitud.format(4)}, ${alerta.longitud.format(4)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Botón llamar al usuario
            IconButton(onClick = { onLlamarUsuario(telefonoUsuario) }) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Llamar al vecino",
                    tint = AzulCunaSegura
                )
            }

            // Botón llamar al 911
            IconButton(onClick = onLlamar911) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RojoSOS),
                    contentAlignment = Alignment.Center
                ) {
                    Text("911", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LeyendaItem(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(2.dp))
        Text(label, fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
