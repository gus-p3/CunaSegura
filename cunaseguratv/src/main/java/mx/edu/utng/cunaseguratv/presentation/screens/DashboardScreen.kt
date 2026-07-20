package mx.edu.utng.cunaseguratv.presentation.screens

import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.edu.utng.cunaseguratv.data.AlertaTV
import mx.edu.utng.cunaseguratv.presentation.TvUiState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.sp
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun DashboardScreen(
    state: TvUiState,
    onSilenciar: () -> Unit
) {
    val context = LocalContext.current
    // Control del ciclo de vida del mapa de OSMDroid
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Control para expandir/colapsar la barra lateral (inicia expandida y ocupa el 40%)
    var isSidebarExpanded by remember { mutableStateOf(true) }
    
    // Configuración inicial de OSMDroid (Síncrona, obligatoria antes de instanciar MapView)
    // Esto previene que el servidor rechace la petición de mosaicos al no detectar un Agente Válido.
    Configuration.getInstance().userAgentValue = context.packageName

    // Instancia única del MapView asociada al ciclo de vida
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            val startPoint = GeoPoint(21.1561, -100.9325)
            controller.setCenter(startPoint)
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
        // Contiene la marca de la aplicación y controles globales como el toggle de la barra lateral.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 16.dp),
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
                    Text(if (isSidebarExpanded) "Ocultar Panel Lateral" else "Mostrar Panel Lateral")
                }
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.mqttConnected) "Servidor Conectado" else "Servidor Desconectado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- CONTENIDO PRINCIPAL (MAPA + PANEL LATERAL) ---
        // Layout dividido. Si el panel está activo toma el 40%, y el mapa el 60%.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // --- SIDEBAR LATERAL ---
            // Se utiliza AnimatedVisibility para una transición suave al colapsar.
            androidx.compose.animation.AnimatedVisibility(
                visible = isSidebarExpanded,
                modifier = Modifier.weight(0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card para Red Vecinal con texto responsivo y sin desbordes
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
                                text = state.networkId.ifEmpty { "Sin vincular" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Estado OK grande con tamaño de fuente ajustado para TV (no se corta)
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
                    
                    // Sub-lista de alertas (evita cortes de pantalla y desbordes)
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

            // --- MAPA PRINCIPAL (OSMDroid) ---
            // Ocupa el 60% del espacio (o el 100% si el sidebar está oculto).
            var hasCentered by remember { mutableStateOf(false) }

            // Lógica de auto-centrado: ubica la cámara en el primer vecino reportado.
            LaunchedEffect(state.vecinosLocations) {
                if (state.vecinosLocations.isNotEmpty() && !hasCentered) {
                    val firstLoc = GeoPoint(state.vecinosLocations.first().lat, state.vecinosLocations.first().lon)
                    mapView.controller.setCenter(firstLoc)
                    hasCentered = true
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
                    .padding(end = 24.dp, top = 24.dp, bottom = 24.dp, start = if (isSidebarExpanded) 12.dp else 24.dp)
                    .clipToBounds() // Evita que sobresalga
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { mv ->
                        mv.overlays.clear()
                        for (vecino in state.vecinosLocations) {
                            val marker = Marker(mv)
                            marker.position = GeoPoint(vecino.lat, vecino.lon)
                            val esVinculado = vecino.id == state.networkId
                            marker.title = if (esVinculado) "★ ${vecino.nombre} (Tú)" else vecino.nombre
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mv.overlays.add(marker)
                        }
                        mv.invalidate()
                    }
                )

                // Controles de navegación interactivos para TV (D-pad/Control Remoto)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CONTROLES MAPA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { mapView.controller.zoomIn() },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { mapView.controller.zoomOut() },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val center = mapView.mapCenter
                            mapView.controller.animateTo(GeoPoint(center.latitude + 0.003, center.longitude))
                        },
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("▲", fontSize = 10.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                val center = mapView.mapCenter
                                mapView.controller.animateTo(GeoPoint(center.latitude, center.longitude - 0.003))
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("◀", fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                if (state.vecinosLocations.isNotEmpty()) {
                                    val firstLoc = GeoPoint(state.vecinosLocations.first().lat, state.vecinosLocations.first().lon)
                                    mapView.controller.animateTo(firstLoc)
                                }
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("◉", fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                val center = mapView.mapCenter
                                mapView.controller.animateTo(GeoPoint(center.latitude, center.longitude + 0.003))
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("▶", fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val center = mapView.mapCenter
                            mapView.controller.animateTo(GeoPoint(center.latitude - 0.003, center.longitude))
                        },
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("▼", fontSize = 10.sp)
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
            val nombreVecino = if (alerta.nombreUsuario.isNotEmpty() && alerta.nombreUsuario != "Vecino") {
                alerta.nombreUsuario
            } else {
                val vecinoEncontrado = state.vecinosLocations.find { it.id == alerta.usuarioId.toString() }
                vecinoEncontrado?.nombre ?: "Alerta (ID: ${alerta.usuarioId})"
            }
            
            Text(
                text = "🏠 Vecino: $nombreVecino",
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
