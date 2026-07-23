package mx.edu.utng.cunasegura.presentation.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import mx.edu.utng.cunasegura.domain.model.Alerta
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val DEFAULT_LOCATION = GeoPoint(21.1565, -100.9327)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMapScreen() {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel(factory = MapViewModelFactory(context))

    val usuario by viewModel.usuario.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val vecinosLocations by viewModel.vecinosLocations.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()

    // Estado de permisos
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

    // Solicitar permisos al entrar
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Obtener ubicación real al tener permiso
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.setUbicacionUsuario(location.latitude, location.longitude)
                    }
                }
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, null
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.setUbicacionUsuario(location.latitude, location.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Permiso revocado en tiempo de ejecución
            }
        }
    }

    // Configurar OSMDroid
    LaunchedEffect(Unit) {
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = "CunaSegura/1.0 (${context.packageName})"
        val basePath = File(context.cacheDir, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = File(basePath, "tiles")
    }

    // Iconos de marcadores por tipo
    val userIcon = remember { createColoredMarkerDrawable(context, android.graphics.Color.parseColor("#2196F3"), 36) } // Azul para Mí
    val vecinoIcon = remember { createColoredMarkerDrawable(context, android.graphics.Color.parseColor("#4CAF50"), 30) } // Verde para Vecinos
    val alertIcon = remember { createColoredMarkerDrawable(context, android.graphics.Color.parseColor("#F44336"), 38) } // Rojo para Alertas SOS

    // MapView con ciclo de vida correcto
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            controller.setZoom(15.0)
            controller.setCenter(userLocation ?: DEFAULT_LOCATION)
            onResume()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Mapa Comunitario", color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                if (locationPermissionGranted) "📍 GPS activo" else "⚠️ Sin permiso GPS",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    if (locationPermissionGranted) {
                        IconButton(onClick = {
                            try {
                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { loc ->
                                        if (loc != null) viewModel.setUbicacionUsuario(loc.latitude, loc.longitude)
                                    }
                            } catch (e: SecurityException) { /* ignorar */ }
                        }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Centrar mapa",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ─── MAPA FIJO (50% de la pantalla, no hace scroll) ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { mv ->
                        mv.overlays.clear()

                        // 1. Marcador del usuario actual (AZUL)
                        userLocation?.let { loc ->
                            mv.controller.animateTo(loc)
                            val marker = Marker(mv)
                            marker.position = loc
                            marker.title = "⭐ ${usuario?.nombre ?: "Yo"} (Mi ubicación)"
                            marker.snippet = "Actualizado ahora"
                            marker.icon = userIcon
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            mv.overlays.add(marker)
                        }

                        // 2. Marcadores de vecinos de la red (VERDE)
                        vecinosLocations.forEach { vecino ->
                            val marker = Marker(mv)
                            marker.position = GeoPoint(vecino.lat, vecino.lon)
                            marker.title = "🏠 Vecino: ${vecino.nombre}"
                            marker.icon = vecinoIcon
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            mv.overlays.add(marker)
                        }

                        // 3. Marcadores de alertas activas (ROJO)
                        activeAlerts.forEach { alerta ->
                            if (alerta.latitud != 0.0 || alerta.longitud != 0.0) {
                                val marker = Marker(mv)
                                marker.position = GeoPoint(alerta.latitud, alerta.longitud)
                                marker.title = "🚨 SOS: ${if (alerta.nombreUsuario.isNotBlank() && alerta.nombreUsuario != "Vecino") alerta.nombreUsuario else "Alerta de la Red"}"
                                marker.snippet = "Estado: ${alerta.estado}"
                                marker.icon = alertIcon
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                mv.overlays.add(marker)
                            }
                        }

                        mv.invalidate()
                    }
                )

                // Leyenda flotante sobre el mapa con identificadores claros
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LeyendaItem("🔵", "Yo")
                        LeyendaItem("🟢", "Vecinos (${vecinosLocations.size})")
                        LeyendaItem("🔴", "SOS (${activeAlerts.size})")
                    }
                }

                // Badge de alertas activas sobre el mapa
                if (activeAlerts.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.error,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            "${activeAlerts.size} SOS",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ─── SEPARADOR GRADIENTE ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            // ─── PANEL DE ALERTAS (50% inferior, con su propio scroll) ────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (activeAlerts.isEmpty())
                                MaterialTheme.colorScheme.surface
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (activeAlerts.isEmpty()) "✅ Sin alertas activas" else "🚨 Alertas activas en la red",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (activeAlerts.isEmpty())
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.error
                    )
                    if (activeAlerts.isNotEmpty()) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("${activeAlerts.size}", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                if (activeAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✅", fontSize = 36.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Todo está en calma",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "No hay alertas SOS en tu red vecinal",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeAlerts) { alerta ->
                            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                                AlertaItem(
                                    alerta = alerta,
                                    nombreUsuarioFallback = usuario?.nombre ?: "Vecino",
                                    telefonoUsuarioFallback = usuario?.telefono ?: "",
                                    onLlamarUsuario = { telefono ->
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono")))
                                    },
                                    onLlamar911 = {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")))
                                    }
                                )
                            }
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
    nombreUsuarioFallback: String,
    telefonoUsuarioFallback: String,
    onLlamarUsuario: (String) -> Unit,
    onLlamar911: () -> Unit
) {
    val nombreMostrado = if (alerta.nombreUsuario.isNotBlank() && alerta.nombreUsuario != "Vecino") {
        alerta.nombreUsuario
    } else {
        nombreUsuarioFallback
    }

    val iniciales = nombreMostrado.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    val horaFormateada = remember(alerta.creadoEn) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(alerta.creadoEn))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(nombreMostrado, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer)
                Text(
                    "🔴 SOS activo · $horaFormateada",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "GPS: ${alerta.latitud.format(4)}, ${alerta.longitud.format(4)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { onLlamarUsuario(telefonoUsuarioFallback) }) {
                Icon(Icons.Default.Call, contentDescription = "Llamar al vecino",
                    tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onLlamar911) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Text("911", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun LeyendaItem(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(emoji, fontSize = 12.sp)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium)
    }
}

private fun createColoredMarkerDrawable(context: Context, color: Int, sizeDp: Int = 32): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Círculo relleno del color deseado
    paint.color = color
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 4, paint)
    
    // Borde blanco
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f * density
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 4, paint)
    
    // Punto central blanco
    paint.style = Paint.Style.FILL
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, 4f * density, paint)
    
    return BitmapDrawable(context.resources, bitmap)
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
