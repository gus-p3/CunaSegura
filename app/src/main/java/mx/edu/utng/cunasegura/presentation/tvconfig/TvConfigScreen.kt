package mx.edu.utng.cunasegura.presentation.tvconfig

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario
import kotlin.random.Random

// ──────────────────────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────────────────────

data class TvUiState(
    val usuario: Usuario? = null,
    val tvVinculada: Boolean = false,
    val isLoading: Boolean = false,
    val infoMessage: String? = null
)

class TvConfigViewModel(private val context: Context) : ViewModel() {

    private val obtenerUsuarioActualUseCase = AppModule.provideObtenerUsuarioActualUseCase(context)
    private val guardarUsuarioUseCase = AppModule.provideGuardarUsuarioUseCase(context)

    private val _uiState = MutableStateFlow(TvUiState())
    val uiState: StateFlow<TvUiState> = _uiState.asStateFlow()

    init {
        cargarUsuario()
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = obtenerUsuarioActualUseCase()
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    usuario = user,
                    tvVinculada = user.tvVinculada,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onToggleTvVinculada(vinculada: Boolean) {
        viewModelScope.launch {
            val user = _uiState.value.usuario ?: return@launch
            val updatedUser = user.copy(tvVinculada = vinculada)
            guardarUsuarioUseCase(updatedUser)
            _uiState.value = _uiState.value.copy(
                usuario = updatedUser,
                tvVinculada = vinculada,
                infoMessage = if (vinculada) "¡Smart TV vinculada correctamente!" else "Smart TV desvinculada"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }
}

class TvConfigViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TvConfigViewModel(context) as T
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Screen
// ──────────────────────────────────────────────────────────────────────────────

private val AzulCunaSegura = Color(0xFF1F4E79)
private val VerdeVinculado = Color(0xFF4CAF50)
private val GrisDesvinculado = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: TvConfigViewModel = viewModel(factory = TvConfigViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage != null) {
            // Limpiar mensaje tras mostrarse
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración Smart TV", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
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
                .background(Color(0xFFF7F9FC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Card de Estado de Vinculación ──────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.tvVinculada) Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                ),
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (uiState.tvVinculada) VerdeVinculado else GrisDesvinculado),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (uiState.tvVinculada) "Smart TV Vinculada" else "Smart TV no vinculada",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.tvVinculada) VerdeVinculado else Color.DarkGray
                        )
                        Text(
                            if (uiState.tvVinculada) "Central de Monitoreo activa ✓"
                            else "Escanea el código QR desde tu Smart TV",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = uiState.tvVinculada,
                        onCheckedChange = { viewModel.onToggleTvVinculada(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = VerdeVinculado)
                    )
                }
            }

            // ── Generación de QR Dinámico Local ─────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Código QR de Vinculación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AzulCunaSegura
                    )
                    Text(
                        "Escanea este código QR con la cámara de tu Smart TV para enlazar tu cuenta e iniciar el monitoreo vecinal en tiempo real.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // QR Code dibujado por Canvas
                    val linkData = "cunasegura://link_tv?user=${uiState.usuario?.telefono ?: "none"}"
                    MockQrCode(
                        data = linkData,
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.White)
                            .shadow(1.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ID de Vinculación: CS-${uiState.usuario?.id ?: 0}-${uiState.usuario?.telefono?.takeLast(4) ?: "0000"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AzulCunaSegura
                    )
                }
            }

            // ── Card de Info Leanback ──────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Monitoreo en Smart TV", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Recibe alertas visuales en tu pantalla\n" +
                            "• Observa a tus vecinos en el mapa al activarse un SOS\n" +
                            "• Visualiza información de contacto del vecino en riesgo",
                            fontSize = 12.sp, color = Color.DarkGray
                        )
                    }
                }
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Listo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Dibuja un código QR realista usando Canvas de Compose.
 * Tiene los 3 marcadores cuadrados característicos y píxeles distribuidos de forma determinista basados en el texto.
 */
@Composable
fun MockQrCode(data: String, modifier: Modifier = Modifier) {
    val seed = data.hashCode().toLong()
    val random = remember(data) { Random(seed) }

    Canvas(modifier = modifier.padding(12.dp)) {
        val size = this.size.width
        val dotsCount = 21 // Versión estándar 1 del código QR
        val dotSize = size / dotsCount

        // 1. Dibujar los tres marcadores de posición principales (esquinas superior-izquierda, superior-derecha, inferior-izquierda)
        fun drawFinderPattern(offsetX: Float, offsetY: Float) {
            // Cuadrado exterior (7x7 módulos)
            drawRect(
                color = Color.Black,
                topLeft = Offset(offsetX, offsetY),
                size = Size(dotSize * 7, dotSize * 7)
            )
            // Cuadrado interior blanco (5x5 módulos)
            drawRect(
                color = Color.White,
                topLeft = Offset(offsetX + dotSize, offsetY + dotSize),
                size = Size(dotSize * 5, dotSize * 5)
            )
            // Centro negro (3x3 módulos)
            drawRect(
                color = Color.Black,
                topLeft = Offset(offsetX + dotSize * 2, offsetY + dotSize * 2),
                size = Size(dotSize * 3, dotSize * 3)
            )
        }

        // Fondo blanco total
        drawRect(color = Color.White, size = Size(size, size))

        // Marcadores
        drawFinderPattern(0f, 0f) // Superior izquierda
        drawFinderPattern(dotSize * (dotsCount - 7), 0f) // Superior derecha
        drawFinderPattern(0f, dotSize * (dotsCount - 7)) // Inferior izquierda

        // Dibujar píxeles aleatorios/deterministas
        for (row in 0 until dotsCount) {
            for (col in 0 until dotsCount) {
                // Saltar las áreas de los marcadores de posición
                val inTopLeft = row < 8 && col < 8
                val inTopRight = row < 8 && col >= dotsCount - 8
                val inBottomLeft = row >= dotsCount - 8 && col < 8
                if (inTopLeft || inTopRight || inBottomLeft) continue

                // Dibujar un píxel negro con ~50% de probabilidad
                if (random.nextBoolean()) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(col * dotSize, row * dotSize),
                        size = Size(dotSize, dotSize)
                    )
                }
            }
        }
    }
}
