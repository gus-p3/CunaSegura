package mx.edu.utng.cunasegura.presentation.networks

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario

/**
 * Estado inmutable de la pantalla de gestión de Redes Vecinales Comunitarias.
 *
 * @property usuarioActual Usuario en sesión.
 * @property redActual Red vecinal a la que pertenece el usuario.
 * @property miembrosRed Lista de vecinos afiliados a la red actual.
 * @property alertasRed Historial de alertas emitidas en la red.
 * @property esAdminDeRed Indica si el usuario es creador o moderador de la red actual.
 * @property redesCercanas Redes abiertas descubiertas por proximidad geográfica.
 * @property isLoading Bandera de progreso durante transacciones en Firebase.
 * @property mensaje Notificación de retroalimentación o error.
 */
data class NetworksUiState(
    val usuarioActual: Usuario? = null,
    val redActual: Network? = null,
    val miembrosRed: List<Usuario> = emptyList(),
    val alertasRed: List<mx.edu.utng.cunasegura.domain.model.Alerta> = emptyList(),
    val esAdminDeRed: Boolean = false,
    val redesCercanas: List<Pair<Network, Float>> = emptyList(), // Red vecinal y su distancia en metros
    val isLoading: Boolean = false,
    val mensaje: String? = null
)

/**
 * ViewModel que gestiona la afiliación, creación, descubrimiento por geolocalización y administración de Redes Vecinales.
 *
 * @param context Contexto de la aplicación para resolver dependencias desde [AppModule].
 */
class NetworksViewModel(context: Context) : ViewModel() {
    private val userRepo = AppModule.provideUsuarioRepository(context)
    private val netRepo = AppModule.provideNetworkRepository(context)

    private val _uiState = MutableStateFlow(NetworksUiState())
    val uiState: StateFlow<NetworksUiState> = _uiState.asStateFlow()

    init {
        cargarInformacion()
    }

    /**
     * Consulta la red actual del usuario, sus miembros, alertas e identifica si posee privilegios de moderación.
     */
    fun cargarInformacion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, mensaje = null)
            try {
                val usuario = userRepo.obtenerUsuarioActual()
                if (usuario != null) {
                    // Obtener UID de Firebase Auth
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    
                    if (uid.isNotEmpty()) {
                        // Obtener perfil detallado de usuarios
                        val userSnap = com.google.firebase.database.FirebaseDatabase.getInstance()
                            .getReference("usuarios").child(uid).get().await()
                        
                        val networkId = userSnap.child("networkId").getValue(String::class.java) ?: uid
                        val rolEnRed = userSnap.child("rolEnRed").getValue(String::class.java) ?: ""
                        val red = netRepo.obtenerNetworkPorId(networkId)
                        val miembros = if (red != null) netRepo.obtenerMiembrosDeRed(networkId) else emptyList()
                        val alertas = if (red != null) netRepo.obtenerAlertasDeRed(networkId) else emptyList()

                        val esAdmin = rolEnRed == "admin" || (red != null && red.id == uid)
                        
                        val currentDetalleUsuario = usuario.copy(
                            rol = userSnap.child("rol").getValue(String::class.java) ?: "usuario"
                        )

                        _uiState.value = _uiState.value.copy(
                            usuarioActual = currentDetalleUsuario,
                            redActual = red,
                            miembrosRed = miembros,
                            alertasRed = alertas,
                            esAdminDeRed = esAdmin,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            usuarioActual = usuario,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Sesión no iniciada")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al cargar información: ${e.message}")
            }
        }
    }

    /**
     * Expulsa a un miembro de la red vecinal actual.
     *
     * @param uidMiembro UID del usuario a expulsar.
     */
    fun expulsarMiembro(uidMiembro: String) {
        viewModelScope.launch {
            try {
                val redId = _uiState.value.redActual?.id ?: return@launch
                _uiState.value = _uiState.value.copy(isLoading = true)
                val exito = netRepo.expulsarMiembro(uidMiembro, redId)
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Usuario expulsado de la red")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al expulsar usuario")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Renombra la red vecinal actual.
     *
     * @param nuevoNombre Nuevo nombre de la comunidad.
     */
    fun actualizarNombreRed(nuevoNombre: String) {
        viewModelScope.launch {
            val redId = _uiState.value.redActual?.id ?: return@launch
            if (nuevoNombre.isBlank()) {
                _uiState.value = _uiState.value.copy(mensaje = "El nombre de la red no puede estar vacío")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val exito = netRepo.actualizarNombreRed(redId, nuevoNombre.trim())
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Nombre de red actualizado a '$nuevoNombre'")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al cambiar nombre")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Crea y registra una nueva red vecinal comunitaria asignando al creador como moderador.
     */
    fun crearRedVecinal(nombre: String, tipo: String, lat: Double, lng: Double, radio: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                
                // Establecer el rol en la red vecinal como "admin" (sin sobreescribir el rol global del sistema)
                val userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(uid)
                userRef.child("rolEnRed").setValue("admin").await()
                
                // Si anteriormente se sobreescribió como "administrador", restablecerlo a "usuario"
                val rolActual = userRef.child("rol").get().await().getValue(String::class.java)
                if (rolActual == "administrador" || rolActual == "admin") {
                    userRef.child("rol").setValue("usuario").await()
                }
                
                val nuevaRed = Network(
                    id = uid,
                    nombre = nombre,
                    tipo = tipo,
                    latitud = lat,
                    longitud = lng,
                    radio = radio,
                    miembros = mapOf(uid to true)
                )
                netRepo.crearNetwork(nuevaRed)
                netRepo.unirseARed(uid, uid)
                
                _uiState.value = _uiState.value.copy(mensaje = "¡Red vecinal '$nombre' creada con éxito!")
                cargarInformacion()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al crear red: ${e.message}")
            }
        }
    }

    /**
     * Une al usuario a una red vecinal mediante el escaneo de un código QR.
     *
     * @param networkId Identificador extraído del código QR.
     */
    fun unirsePorQr(networkId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                
                val red = netRepo.obtenerNetworkPorId(networkId)
                if (red == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Código QR inválido. Red no encontrada.")
                    return@launch
                }
                
                val exito = netRepo.unirseARed(uid, networkId)
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Te has unido a la red: ${red.nombre}")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al unirse a la red.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Busca y filtra redes comunitarias abiertas por radio de cobertura GPS.
     *
     * @param lat Latitud GPS.
     * @param lng Longitud GPS.
     */
    fun buscarRedesAbiertasCercanas(lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val redes = netRepo.obtenerRedesAbiertas()
                val filtradas = mutableListOf<Pair<Network, Float>>()
                
                for (red in redes) {
                    val results = FloatArray(1)
                    Location.distanceBetween(lat, lng, red.latitud, red.longitud, results)
                    val distancia = results[0]
                    // Verificar si la distancia actual está dentro del radio de cobertura de la red
                    if (distancia <= red.radio) {
                        filtradas.add(Pair(red, distancia))
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    redesCercanas = filtradas.sortedBy { it.second },
                    isLoading = false,
                    mensaje = if (filtradas.isEmpty()) "No se encontraron redes abiertas dentro de tu cobertura." else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al buscar: ${e.message}")
            }
        }
    }

    /**
     * Se une a una red pública abierta seleccionada por cercanía.
     */
    fun unirseARedAbierta(networkId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val exito = netRepo.unirseARed(uid, networkId)
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Te has unido con éxito")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al unirse")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Abandona la red vecinal actual restableciendo la red personal independiente del usuario.
     */
    fun salirDeRedActual() {
        viewModelScope.launch {
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val redId = _uiState.value.redActual?.id ?: return@launch
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val exito = netRepo.salirDeRed(uid, redId)
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Saliste de la red vecinal")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al salir de la red")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Limpia mensajes informativos o de alerta en la UI.
     */
    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
}

/**
 * Fábrica para instanciar [NetworksViewModel] pasando el contexto de la aplicación.
 */
class NetworksViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NetworksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NetworksViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

