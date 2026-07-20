package mx.edu.utng.cunasegura.presentation.map

import android.Manifest
import java.io.File
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationServices
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.R // for default icons if needed
import java.text.SimpleDateFormat
import java.util.*

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val RojoSOS @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error
// Dolores Hidalgo, Gto. — posición por defecto
private val DEFAULT_LOCATION = GeoPoint(21.1565, -100.9327)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMapScreen() {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel(factory = MapViewModelFactory(context))

    val usuario by viewModel.usuario.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()

    // Check current permission state on startup
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Fetch actual real location when permission is granted
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.setUbicacionUsuario(location.latitude, location.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Permissions denied/revoked
            }
        }
    }

    // Configuramos Osmdroid user agent para que el servidor deje descargar mapas
    LaunchedEffect(Unit) {
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = context.packageName
        val basePath = File(context.cacheDir, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = File(basePath, "tiles")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GPS — Mapa Comunitario", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Mapa (Altura fija de 350.dp para evitar conflictos con el resto del contenido) ---
            Box(modifier = Modifier.height(350.dp).fillMaxWidth()) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(DEFAULT_LOCATION)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { mapView ->
                        mapView.overlays.clear()
                        
                        // Centrar en usuario si está disponible
                        userLocation?.let { loc ->
                            mapView.controller.setCenter(loc)
                            
                            val userMarker = Marker(mapView)
                            userMarker.position = loc
                            userMarker.title = "Yo — ${usuario?.nombre ?: "Vecino"}"
                            userMarker.snippet = "Mi ubicación actual"
                            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            // Marcador verde por defecto podemos teñir el ícono si tuviéramos un Drawable
                            // Por simplicidad, usamos el marcador por defecto
                            mapView.overlays.add(userMarker)
                        }

                        // Marcadores rojos de alertas activas
                        activeAlerts.forEach { alerta ->
                            val alertaPos = GeoPoint(alerta.latitud, alerta.longitud)
                            if (alerta.latitud != 0.0 || alerta.longitud != 0.0) {
                                val alertMarker = Marker(mapView)
                                alertMarker.position = alertaPos
                                alertMarker.title = "🔴 Alerta SOS"
                                alertMarker.snippet = "Estado: ${alerta.estado}"
                                alertMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(alertMarker)
                            }
                        }
                        
                        mapView.invalidate()
                    }
                )

                // Leyenda sobre el mapa
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
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

            // --- Lista de alertas activas ---
            if (activeAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Alertas activas (${activeAlerts.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RojoSOS,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    activeAlerts.forEach { alerta ->
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
                        Spacer(modifier = Modifier.height(8.dp))
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
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer),
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
                Text(iniciales, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    Text("911", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
