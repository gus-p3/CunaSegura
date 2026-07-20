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
import com.google.firebase.database.FirebaseDatabase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult
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

    fun onQrScanned(contents: String) {
        val tvId = if (contents.contains("tvId=")) {
            contents.substringAfter("tvId=")
        } else {
            contents
        }
        
        viewModelScope.launch {
            val user = _uiState.value.usuario ?: return@launch
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val dbRef = FirebaseDatabase.getInstance().getReference("tvs").child(tvId).child("networkId")
            dbRef.setValue(uid).addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("networkId").setValue(uid)
                mx.edu.utng.cunasegura.mqtt.MqttPublisher.publishTvVinculacion(tvId, uid)
                onToggleTvVinculada(true)
            }.addOnFailureListener {
                _uiState.value = _uiState.value.copy(infoMessage = "Error al enlazar Smart TV")
            }
        }
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

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val VerdeVinculado @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val GrisDesvinculado @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.outline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: TvConfigViewModel = viewModel(factory = TvConfigViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                viewModel.onQrScanned(result.contents)
            }
        }
    )

    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage != null) {
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración Smart TV", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
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
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Card de Estado de Vinculación ──────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.tvVinculada) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
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
                        Icon(Icons.Default.Tv, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
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

            // ── Lector QR en Teléfono ─────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "VINCULAR NUEVA PANTALLA",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.5.sp,
                        color = AzulCunaSegura
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Abre la app de Cuna Segura en tu Smart TV y escanea el código QR que aparece en pantalla.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val options = ScanOptions()
                            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            options.setPrompt("Apunta al código QR en tu TV")
                            options.setBeepEnabled(true)
                            options.setBarcodeImageEnabled(false)
                            scanLauncher.launch(options)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Escanear QR de la TV", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

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
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer),
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


