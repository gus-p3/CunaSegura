package mx.edu.utng.cunaseguratv.presentation.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import mx.edu.utng.cunaseguratv.data.AlertaTV
import mx.edu.utng.cunaseguratv.presentation.TvUiState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun DashboardScreen(
    state: TvUiState,
    onSilenciar: () -> Unit,
    onCerrarSesion: () -> Unit,
    onToggleColorPicker: () -> Unit = {},
    onGuardarColores: (Int, Int, Int) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    // Control del ciclo de vida del mapa de OSMDroid
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Control para expandir/colapsar la barra lateral (inicia expandida y ocupa el 40%)
    var isSidebarExpanded by remember { mutableStateOf(true) }
    
    // Configuración inicial de OSMDroid (OpenStreetMap para Android)
    val conf = Configuration.getInstance()
    conf.load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
    conf.userAgentValue = "CunaSeguraTV/1.0 (${context.packageName})"
    conf.osmdroidBasePath = context.cacheDir
    conf.osmdroidTileCache = java.io.File(context.cacheDir, "osmdroid")

    // MapView con OSMDroid + OpenStreetMap MAPNIK (sin API key, 100% gratuito)
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(21.1561, -100.9325))
            onResume() // iniciar carga de tiles de inmediato
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- NAVBAR (ENCABEZADO) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🛡️ CUNA SEGURA TV",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black
                )
                
                Button(
                    onClick = { isSidebarExpanded = !isSidebarExpanded },
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (isSidebarExpanded) "Ocultar Panel" else "Mostrar Panel")
                }
            }

            // Datos de sesión activa
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "👤 Sesión: ${state.usuarioNombre.ifEmpty { "Usuario Vinculado" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "🏠 Red: ${state.networkNombre.ifEmpty { state.networkId.take(12) }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (state.mqttConnected) androidx.compose.ui.graphics.Color.Green else MaterialTheme.colorScheme.error,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.mqttConnected) "Conectado" else "Desconectado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onCerrarSesion,
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Cerrar Sesión TV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onToggleColorPicker,
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("🎨 Colores", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- CONTENIDO PRINCIPAL (MAPA + PANEL LATERAL) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // --- SIDEBAR LATERAL ---
            androidx.compose.animation.AnimatedVisibility(
                visible = isSidebarExpanded,
                modifier = Modifier.weight(0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 24.dp, top = 16.dp, bottom = 24.dp, end = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card para Red Vecinal e información del usuario
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.tv.material3.CardDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "RED VECINAL VINCULADA",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.networkNombre.ifEmpty { state.networkId },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.usuarioNombre.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Titular: ${state.usuarioNombre} (${state.usuarioCorreo})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Card de Vecinos pertenecientes a la Red
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.tv.material3.CardDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "👥 VECINOS DE LA RED (${state.vecinosList.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.vecinosList.isEmpty()) {
                                Text(
                                    text = "Buscando vecinos en la nube...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                state.vecinosList.forEach { vecino ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "• ${vecino.nombre}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (vecino.telefono.isNotEmpty()) {
                                                Text(
                                                    text = "  Tel: ${vecino.telefono}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (vecino.lat != 0.0) "📍 Activo" else "⚪ Offline",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (vecino.lat != 0.0) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Estado OK grande
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.tv.material3.CardDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ESTADO: ✅ ZONA SEGURA",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Últimas Alertas:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.alertasRecientes.isEmpty()) {
                            Text(
                                text = "No hay alertas recientes.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                        state.alertasRecientes.take(10).forEach { alerta ->
                                AlertaHistoryItem(alerta, state)
                            }
                        }
                    }
                }
            }

            // --- MAPA PRINCIPAL (OSMDroid + OpenStreetMap MAPNIK) ---
            val firstVecino = state.vecinosLocations.firstOrNull { it.lat != 0.0 }
            LaunchedEffect(firstVecino) {
                if (firstVecino != null) {
                    mapView.controller.animateTo(GeoPoint(firstVecino.lat, firstVecino.lon))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
                    .padding(end = 24.dp, top = 24.dp, bottom = 24.dp, start = if (isSidebarExpanded) 12.dp else 24.dp)
                    .clipToBounds()
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { mv ->
                        mv.overlays.clear()
                        // Marcadores de VECINOS (color personalizable)
                        state.vecinosLocations.filter { it.lat != 0.0 }.forEach { vecino ->
                            val marker = Marker(mv)
                            marker.position = GeoPoint(vecino.lat, vecino.lon)
                            val esUsuarioVinculado = vecino.id == state.usuarioId
                            marker.title = if (esUsuarioVinculado) "★ ${vecino.nombre} (Tú)" else vecino.nombre
                            val color = if (esUsuarioVinculado) state.colorUsuario else state.colorVecinos
                            marker.icon = createColoredMarker(mv.context, color)
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            mv.overlays.add(marker)
                        }
                        // Marcadores de ALERTAS SOS (color personalizable)
                        state.alertasRecientes.filter { it.latitud != 0.0 && it.estado == "activa" }.forEach { alerta ->
                            val marker = Marker(mv)
                            marker.position = GeoPoint(alerta.latitud, alerta.longitud)
                            marker.title = "🚨 SOS: ${alerta.nombreUsuario}"
                            marker.icon = createColoredMarker(mv.context, state.colorAlertas)
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            mv.overlays.add(marker)
                        }
                        mv.invalidate()
                    }
                )

                // Controles del mapa navegables con control remoto / D-Pad
                Card(
                    onClick = {},
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                    colors = androidx.tv.material3.CardDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "🗺️ MAPA TV",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Zoom In / Out
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { mapView.controller.zoomIn() },
                                colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(width = 44.dp, height = 34.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("➕", fontSize = 12.sp) }

                            Button(
                                onClick = { mapView.controller.zoomOut() },
                                colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(width = 44.dp, height = 34.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("➖", fontSize = 12.sp) }
                        }

                        // Cruceta Direccional (Mover Norte)
                        Button(
                            onClick = {
                                val center = mapView.mapCenter as? GeoPoint ?: GeoPoint(21.1561, -100.9325)
                                mapView.controller.animateTo(GeoPoint(center.latitude + 0.003, center.longitude))
                            },
                            colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.size(width = 44.dp, height = 32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("▲", fontSize = 11.sp) }

                        // Mover Oeste / Recentrar / Mover Este
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val center = mapView.mapCenter as? GeoPoint ?: GeoPoint(21.1561, -100.9325)
                                    mapView.controller.animateTo(GeoPoint(center.latitude, center.longitude - 0.003))
                                },
                                colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.size(width = 44.dp, height = 32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("◄", fontSize = 11.sp) }

                            Button(
                                onClick = {
                                    val firstVecino = state.vecinosLocations.firstOrNull { it.lat != 0.0 }
                                    val target = if (firstVecino != null) GeoPoint(firstVecino.lat, firstVecino.lon) else GeoPoint(21.1561, -100.9325)
                                    mapView.controller.animateTo(target)
                                    mapView.controller.setZoom(15.0)
                                },
                                colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.size(width = 44.dp, height = 32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("🎯", fontSize = 11.sp) }

                            Button(
                                onClick = {
                                    val center = mapView.mapCenter as? GeoPoint ?: GeoPoint(21.1561, -100.9325)
                                    mapView.controller.animateTo(GeoPoint(center.latitude, center.longitude + 0.003))
                                },
                                colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.size(width = 44.dp, height = 32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("►", fontSize = 11.sp) }
                        }

                        // Mover Sur
                        Button(
                            onClick = {
                                val center = mapView.mapCenter as? GeoPoint ?: GeoPoint(21.1561, -100.9325)
                                mapView.controller.animateTo(GeoPoint(center.latitude - 0.003, center.longitude))
                            },
                            colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.size(width = 44.dp, height = 32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("▼", fontSize = 11.sp) }
                    }
                }

                if (state.alertaActiva != null && !state.isSilenced && !state.showAlertModal) {
                    Button(
                        onClick = onSilenciar,
                        colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Text("SILENCIAR ALARMA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Panel selector de colores (D-pad navigable)
    if (state.showColorPicker) {
        ColorPickerDialog(
            colorUsuario = state.colorUsuario,
            colorVecinos = state.colorVecinos,
            colorAlertas = state.colorAlertas,
            onGuardar = onGuardarColores,
            onCerrar = onToggleColorPicker
        )
    }
}

// Componente visual para cada elemento del historial de alertas.
// Emplea color surfaceVariant para garantizar su visibilidad en televisores bajo modo oscuro.
@Composable
fun AlertaHistoryItem(alerta: AlertaTV, state: TvUiState) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.tv.material3.CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val nombreVecino = when {
                alerta.nombreUsuario.isNotBlank() && alerta.nombreUsuario != "Vecino" -> alerta.nombreUsuario
                state.usuarioNombre.isNotBlank() -> state.usuarioNombre
                else -> {
                    val vecinoEncontrado = state.vecinosLocations.find { it.id == alerta.usuarioId.toString() }
                    vecinoEncontrado?.nombre ?: "Alerta SOS de Vecino"
                }
            }
            
            Text(
                text = "🚨 $nombreVecino",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            val fecha = sdf.format(java.util.Date(alerta.creadoEn))
            Text(text = "🕒 Fecha: $fecha", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "⚠️ Estado: ${alerta.estado}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@android.annotation.SuppressLint("SetJavaScriptEnabled")
@Composable
fun TvWebViewMapView(
    vecinos: List<mx.edu.utng.cunaseguratv.presentation.VecinoLocation>,
    alertas: List<AlertaTV>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultLat = vecinos.firstOrNull { it.lat != 0.0 }?.lat ?: 21.1561
    val defaultLon = vecinos.firstOrNull { it.lon != 0.0 }?.lon ?: -100.9325

    val htmlData = remember(vecinos, alertas) {
        val markersList = mutableListOf<String>()
        vecinos.filter { it.lat != 0.0 }.forEach { v ->
            markersList.add("""{ "name": "Vecino: ${v.nombre.replace("\"", "'")}", "lat": ${v.lat}, "lon": ${v.lon}, "isAlert": false }""")
        }
        alertas.filter { it.latitud != 0.0 && it.estado == "activa" }.forEach { a ->
            markersList.add("""{ "name": "SOS: ${a.nombreUsuario.replace("\"", "'")}", "lat": ${a.latitud}, "lon": ${a.longitud}, "isAlert": true }""")
        }
        val markersJson = markersList.joinToString(",")

        """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<link rel="stylesheet" href="file:///android_asset/leaflet.css"/>
<script src="file:///android_asset/leaflet.js"></script>
<style>
html,body,#map{height:100%;width:100%;margin:0;padding:0;background:#0d1117;}
.leaflet-container{background:#0d1117;}
</style>
</head>
<body>
<div id="map"></div>
<script>
var map=L.map('map',{zoomControl:true,attributionControl:false}).setView([$defaultLat,$defaultLon],15);
L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,crossOrigin:true}).addTo(map);
var greenIcon=L.divIcon({className:'',html:'<div style="background:#00e676;border-radius:50%;width:18px;height:18px;border:3px solid #fff;"></div>',iconSize:[18,18]});
var redIcon=L.divIcon({className:'',html:'<div style="background:#f44336;border-radius:50%;width:22px;height:22px;border:3px solid #fff;animation:pulse 1s infinite;"></div>',iconSize:[22,22]});
var markersData=[$markersJson];
markersData.forEach(function(m){
  if(m.lat!==0&&m.lon!==0){
    var icon=m.isAlert?redIcon:greenIcon;
    L.marker([m.lat,m.lon],{icon:icon}).addTo(map).bindPopup('<b>'+m.name+'</b>');
  }
});
</script>
</body>
</html>"""
    }

    AndroidView(
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = android.webkit.WebViewClient()
                loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

// Crea un marcador circular de color personalizado para OSMDroid
fun createColoredMarker(context: android.content.Context, color: Int, sizeDp: Int = 36): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // Círculo de color principal
    paint.color = color
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 4, paint)
    // Borde blanco
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f * density
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 4, paint)
    // Punto central blanco
    paint.style = Paint.Style.FILL
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, 5f * density, paint)
    return BitmapDrawable(context.resources, bitmap)
}

// Colores disponibles para seleccionar en el panel
private val COLORES_DISPONIBLES = listOf(
    0xFF2196F3.toInt() to "Azul",
    0xFF4CAF50.toInt() to "Verde",
    0xFFF44336.toInt() to "Rojo",
    0xFFFF9800.toInt() to "Naranja",
    0xFF9C27B0.toInt() to "Morado",
    0xFF00BCD4.toInt() to "Cian",
    0xFFFFEB3B.toInt() to "Amarillo",
    0xFFE91E63.toInt() to "Rosa",
    0xFF795548.toInt() to "Café",
    0xFF607D8B.toInt() to "Gris",
    0xFFFFFFFF.toInt() to "Blanco",
    0xFF212121.toInt() to "Negro"
)

@Composable
fun ColorPickerDialog(
    colorUsuario: Int,
    colorVecinos: Int,
    colorAlertas: Int,
    onGuardar: (Int, Int, Int) -> Unit,
    onCerrar: () -> Unit
) {
    var selectedUsuario by remember { mutableIntStateOf(colorUsuario) }
    var selectedVecinos by remember { mutableIntStateOf(colorVecinos) }
    var selectedAlertas by remember { mutableIntStateOf(colorAlertas) }

    Dialog(onDismissRequest = onCerrar) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1E1E2E), RoundedCornerShape(20.dp))
                .padding(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    "🎨 Personalizar Colores del Mapa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Selector color usuario vinculado
                ColorSelectorRow(
                    label = "⭐ Mi marcador (usuario vinculado)",
                    selectedColor = selectedUsuario,
                    onSelect = { selectedUsuario = it }
                )

                // Selector color vecinos
                ColorSelectorRow(
                    label = "👥 Vecinos de la red",
                    selectedColor = selectedVecinos,
                    onSelect = { selectedVecinos = it }
                )

                // Selector color alertas
                ColorSelectorRow(
                    label = "🚨 Alertas SOS activas",
                    selectedColor = selectedAlertas,
                    onSelect = { selectedAlertas = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    Button(
                        onClick = onCerrar,
                        colors = ButtonDefaults.colors(containerColor = Color(0xFF424242))
                    ) { Text("Cancelar", color = Color.White) }

                    Button(
                        onClick = { onGuardar(selectedUsuario, selectedVecinos, selectedAlertas) },
                        colors = ButtonDefaults.colors(containerColor = Color(0xFF4CAF50))
                    ) { Text("✓ Guardar", fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun ColorSelectorRow(label: String, selectedColor: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Color(0xFFBBBBBB), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(COLORES_DISPONIBLES) { (color, _) ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { onSelect(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text("✓", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
