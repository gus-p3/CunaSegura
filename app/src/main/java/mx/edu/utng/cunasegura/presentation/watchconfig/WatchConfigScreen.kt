package mx.edu.utng.cunasegura.presentation.watchconfig

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.data.local.entity.ConfiguracionToqueEntity
import mx.edu.utng.cunasegura.di.AppModule

// ──────────────────────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────────────────────

private val ACCIONES_MAP = mapOf(
    "MENSAJE_SMS" to "Enviar mensaje de alerta",
    "UBICACION_TIEMPO_REAL" to "Compartir ubicación en tiempo real",
    "ALARMA_TV" to "Activar alarma en TV de vecinos",
    "LLAMAR_911" to "Llamar al 911"
)
private val ACCIONES_KEYS = ACCIONES_MAP.keys.toList()

data class WatchUiState(
    val toque1: String = ACCIONES_KEYS[0],
    val toque2: String = ACCIONES_KEYS[1],
    val toque3: String = ACCIONES_KEYS[2],
    val toque4: String = ACCIONES_KEYS[3],
    val watchVinculado: Boolean = false,
    val guardado: Boolean = false,
    val isLoading: Boolean = false
)

class WatchConfigViewModel(private val context: Context) : ViewModel() {

    private val db = AppModule.provideDatabase(context)
    private val toqueDao = db.configuracionToqueDao()
    private val obtenerUsuarioActualUseCase = AppModule.provideObtenerUsuarioActualUseCase(context)
    private val prefs = mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager(context)

    private val _uiState = MutableStateFlow(WatchUiState(watchVinculado = prefs.isWatchLinked()))
    val uiState: StateFlow<WatchUiState> = _uiState.asStateFlow()

    private var usuarioId: Int = 1 // Fallback ID

    init {
        cargarConfiguracion()
    }

    private fun cargarConfiguracion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val usuario = obtenerUsuarioActualUseCase()
            if (usuario != null) {
                usuarioId = usuario.id
            }
            val configs = toqueDao.obtenerPorUsuario(usuarioId)
            if (configs.isNotEmpty()) {
                val map = configs.associate { it.cantidadToques to it.tipoAccion }
                _uiState.value = _uiState.value.copy(
                    toque1 = map[1] ?: ACCIONES_KEYS[0],
                    toque2 = map[2] ?: ACCIONES_KEYS[1],
                    toque3 = map[3] ?: ACCIONES_KEYS[2],
                    toque4 = map[4] ?: ACCIONES_KEYS[3],
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onAccionChange(numeroToque: Int, accion: String) {
        _uiState.value = when (numeroToque) {
            1 -> _uiState.value.copy(toque1 = accion, guardado = false)
            2 -> _uiState.value.copy(toque2 = accion, guardado = false)
            3 -> _uiState.value.copy(toque3 = accion, guardado = false)
            4 -> _uiState.value.copy(toque4 = accion, guardado = false)
            else -> _uiState.value
        }
    }

    fun guardarConfiguracion() {
        viewModelScope.launch {
            val state = _uiState.value
            
            val t1 = toqueDao.obtenerPorUsuarioYToque(usuarioId, 1)
            toqueDao.insertarOActualizar(ConfiguracionToqueEntity(id = t1?.id ?: 0, usuarioId = usuarioId, cantidadToques = 1, tipoAccion = state.toque1))

            val t2 = toqueDao.obtenerPorUsuarioYToque(usuarioId, 2)
            toqueDao.insertarOActualizar(ConfiguracionToqueEntity(id = t2?.id ?: 0, usuarioId = usuarioId, cantidadToques = 2, tipoAccion = state.toque2))

            val t3 = toqueDao.obtenerPorUsuarioYToque(usuarioId, 3)
            toqueDao.insertarOActualizar(ConfiguracionToqueEntity(id = t3?.id ?: 0, usuarioId = usuarioId, cantidadToques = 3, tipoAccion = state.toque3))

            val t4 = toqueDao.obtenerPorUsuarioYToque(usuarioId, 4)
            toqueDao.insertarOActualizar(ConfiguracionToqueEntity(id = t4?.id ?: 0, usuarioId = usuarioId, cantidadToques = 4, tipoAccion = state.toque4))

            _uiState.value = _uiState.value.copy(guardado = true)
            
            val payload = "${state.toque1}|${state.toque2}|${state.toque3}|${state.toque4}"
            val messageClient = com.google.android.gms.wearable.Wearable.getMessageClient(context)
            val nodeClient = com.google.android.gms.wearable.Wearable.getNodeClient(context)
            
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val nodes = com.google.android.gms.tasks.Tasks.await(nodeClient.connectedNodes)
                    val data = payload.toByteArray()
                    for (node in nodes) {
                        messageClient.sendMessage(node.id, "/cunasegura/config/update", data)
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (nodes.isNotEmpty()) {
                            android.widget.Toast.makeText(context, "Sincronizando con reloj...", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Guardado, pero no hay reloj conectado", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WatchConfigViewModel", "Error enviando config", e)
                }
            }
        }
    }

    fun onSimularVinculacion() {
        val newState = !_uiState.value.watchVinculado
        _uiState.value = _uiState.value.copy(watchVinculado = newState)
        prefs.setWatchLinked(newState)
    }
}

class WatchConfigViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WatchConfigViewModel(context) as T
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Screen
// ──────────────────────────────────────────────────────────────────────────────

// MaterialTheme colors will be used
private val VerdeVinculado = Color(0xFF4CAF50)
private val NaranjaDesvinculado = Color(0xFFFF9800)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: WatchConfigViewModel = viewModel(factory = WatchConfigViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    // La vinculación ahora es gestionada automáticamente por Play Services Wearable API.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración SmartWatch", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F9FC)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Estado de Vinculación BLE ─────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.watchVinculado) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(
                                if (uiState.watchVinculado) VerdeVinculado else NaranjaDesvinculado
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Watch, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (uiState.watchVinculado) "SmartWatch Vinculado" else "Sin SmartWatch vinculado",
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.watchVinculado) VerdeVinculado else NaranjaDesvinculado
                            )
                            Text(
                                if (uiState.watchVinculado) "Dispositivo BLE conectado ✓"
                                else "Activa Bluetooth y acerca tu watch",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // ── Información BLE ───────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Requisitos de Vinculación", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Watch Wear OS o compatible con BLE\n• Celular con Bluetooth activo\n• Radio máximo ~10 metros\n• La app necesita permiso BLUETOOTH_CONNECT", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            // ── Configuración de Toques ───────────────────────────────────
            item {
                Text("Asignación de Acciones por Toque", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                Text("Define qué hace cada toque del botón de pánico en tu watch:", fontSize = 12.sp, color = Color.Gray)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf(
                            Pair(1, uiState.toque1),
                            Pair(2, uiState.toque2),
                            Pair(3, uiState.toque3),
                            Pair(4, uiState.toque4)
                        ).forEach { (num, accion) ->
                            ToqueRow(
                                numero = num,
                                accionSeleccionada = accion,
                                onAccionChange = { viewModel.onAccionChange(num, it) }
                            )
                            if (num < 4) HorizontalDivider(color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }

            // ── Botón Guardar ─────────────────────────────────────────────
            item {
                Button(
                    onClick = { viewModel.guardarConfiguracion() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Configuración", fontWeight = FontWeight.Bold)
                }

                if (uiState.guardado) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "✅ Configuración guardada correctamente en el dispositivo",
                        color = VerdeVinculado,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToqueRow(
    numero: Int,
    accionSeleccionada: String,
    onAccionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val emojis = mapOf(1 to "☝️", 2 to "✌️", 3 to "🤟", 4 to "🖐️")
    val iconos: Map<String, ImageVector> = mapOf(
        "MENSAJE_SMS" to Icons.Default.Message,
        "UBICACION_TIEMPO_REAL" to Icons.Default.LocationOn,
        "ALARMA_TV" to Icons.Default.Tv,
        "LLAMAR_911" to Icons.Default.Call
    )

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        // Badge del número de toque
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(emojis[numero] ?: "$numero", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${numero} toque${if (numero > 1) "s" else ""}",
                fontSize = 12.sp, color = Color.Gray
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = ACCIONES_MAP[accionSeleccionada] ?: accionSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(iconos[accionSeleccionada] ?: Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ACCIONES_KEYS.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(iconos[opcion] ?: Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(ACCIONES_MAP[opcion] ?: opcion, fontSize = 13.sp)
                            }},
                            onClick = {
                                onAccionChange(opcion)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
