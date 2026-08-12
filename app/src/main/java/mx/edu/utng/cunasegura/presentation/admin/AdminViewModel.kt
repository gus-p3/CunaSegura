package mx.edu.utng.cunasegura.presentation.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
import mx.edu.utng.cunasegura.domain.repository.INetworkRepository
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * ViewModel maestro para la consola y paneles de control del Administrador Global.
 *
 * Administra el censo global de usuarios, monitoreo de alertas de todas las redes, cambios de estado de cuentas (`bloqueado`, `activo`)
 * y guardado de parámetros globales del sistema (`configuracion_global`).
 *
 * @property usuarioRepository Repositorio de usuarios.
 * @property alertaRepository Repositorio de alertas.
 * @property networkRepository Repositorio de redes comunitarias.
 */
class AdminViewModel(
    private val usuarioRepository: IUsuarioRepository,
    private val alertaRepository: IAlertaRepository,
    private val networkRepository: INetworkRepository
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _totalUsuarios = MutableStateFlow(0)
    val totalUsuarios: StateFlow<Int> = _totalUsuarios.asStateFlow()

    private val _adminActual = MutableStateFlow<Usuario?>(null)
    val adminActual: StateFlow<Usuario?> = _adminActual.asStateFlow()

    private val _network = MutableStateFlow<Network?>(null)
    val network: StateFlow<Network?> = _network.asStateFlow()

    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val alertas: StateFlow<List<Alerta>> = _alertas.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _tiempoVidaAlerta = MutableStateFlow<Double>(720.0)
    val tiempoVidaAlerta: StateFlow<Double> = _tiempoVidaAlerta.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Consulta el catálogo total de usuarios, datos del administrador, red asignada, alertas y configuración global.
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            try {
                val todos = usuarioRepository.obtenerTodosLosUsuarios()
                _usuarios.value = todos
                _totalUsuarios.value = todos.size
                
                val admin = usuarioRepository.obtenerUsuarioActual()
                _adminActual.value = admin
                
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val snapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("usuarios").child(uid).get().await()
                    val netId = snapshot.child("networkId").getValue(String::class.java) ?: uid
                    val net = networkRepository.obtenerNetworkPorId(netId)
                    _network.value = net
                }

                // Cargar todas las alertas para estadísticas
                val todasAlertas = alertaRepository.obtenerTodasLasAlertas()
                _alertas.value = todasAlertas

                // Cargar configuracion global
                val config = networkRepository.obtenerConfiguracionGlobal()
                val tiempoVidaStr = config["tiempoVidaAlerta"]?.toString() ?: "720.0"
                _tiempoVidaAlerta.value = tiempoVidaStr.toDoubleOrNull() ?: 720.0
            } catch (e: Exception) {
                _statusMessage.value = "Error al cargar datos: ${e.message}"
            }
        }
    }

    /**
     * Fuerza una recarga completa de todos los datos administrativos.
     */
    fun recargar() = cargarDatos()

    /**
     * Persiste los parámetros globales de red y directivas de seguridad en Firebase.
     */
    fun guardarRedConfig(
        tipo: String,
        radio: Double,
        tiempoAntiFalsa: Double,
        checkVida: Double,
        esperarDiasNuevos: Int,
        tiempoVidaAlerta: Double
    ) {
        viewModelScope.launch {
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val currentNet = _network.value
                val updatedNet = Network(
                    id = currentNet?.id ?: uid,
                    nombre = currentNet?.nombre ?: "Red Vecinal",
                    tipo = tipo,
                    latitud = currentNet?.latitud ?: 0.0,
                    longitud = currentNet?.longitud ?: 0.0,
                    radio = radio,
                    miembros = currentNet?.miembros ?: mapOf(uid to true),
                    tvId = currentNet?.tvId ?: "",
                    tiempoAntiFalsa = tiempoAntiFalsa,
                    checkVida = checkVida,
                    esperarDiasNuevos = esperarDiasNuevos
                )
                networkRepository.crearNetwork(updatedNet)
                networkRepository.guardarConfiguracionGlobal(tipo, radio, tiempoAntiFalsa, checkVida, esperarDiasNuevos, tiempoVidaAlerta)
                _network.value = updatedNet
                _tiempoVidaAlerta.value = tiempoVidaAlerta
                _statusMessage.value = "¡Configuración de la red guardada con éxito!"
            } catch (e: Exception) {
                _statusMessage.value = "Error al guardar configuración: ${e.message}"
            }
        }
    }

    /**
     * Modifica el estado operativo de una cuenta de usuario (`activo`, `bloqueado`).
     *
     * @param uid Identificador del usuario.
     * @param nuevoEstado Estado a establecer.
     */
    fun cambiarEstadoUsuario(uid: String, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(uid).child("estado").setValue(nuevoEstado).await()
                _statusMessage.value = "Estado del usuario actualizado a '$nuevoEstado'"
                cargarDatos()
            } catch (e: Exception) {
                _statusMessage.value = "Error al actualizar estado: ${e.message}"
            }
        }
    }

    /**
     * Limpia el mensaje informativo de estado.
     */
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}

/**
 * Fábrica para instanciar [AdminViewModel] resolviendo repositorios mediante [AppModule].
 */
class AdminViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(
                AppModule.provideUsuarioRepository(context),
                AppModule.provideAlertaRepository(context),
                AppModule.provideNetworkRepository(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}

