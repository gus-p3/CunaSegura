package mx.edu.utng.cunasegura.presentation.home

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
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.ActivarAlertaUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

/**
 * ViewModel que gestiona la pantalla principal del ciudadano.
 *
 * Mantiene la información del usuario en sesión, su red comunitaria, el botón de pánico SOS
 * y la verificación de políticas de seguridad (antigüedad de miembros `esperarDiasNuevos` y ventana anti-falsa alarma `tiempoAntiFalsa`).
 *
 * @property obtenerUsuarioActualUseCase Caso de uso para obtener la sesión activa.
 * @property activarAlertaUseCase Caso de uso para emitir alertas SOS.
 */
class HomeViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val activarAlertaUseCase: ActivarAlertaUseCase
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _alertaCreada = MutableStateFlow(false)
    val alertaCreada: StateFlow<Boolean> = _alertaCreada.asStateFlow()

    private val _alertaId = MutableStateFlow<Int?>(null)
    val alertaId: StateFlow<Int?> = _alertaId.asStateFlow()

    private val _errorAlerta = MutableStateFlow<String?>(null)
    val errorAlerta: StateFlow<String?> = _errorAlerta.asStateFlow()

    private val _tiempoAntiFalsa = MutableStateFlow(3f)
    val tiempoAntiFalsa: StateFlow<Float> = _tiempoAntiFalsa.asStateFlow()

    init {
        cargarUsuarioActual()
    }

    /**
     * Carga el perfil del usuario activo desde SQLite Room y sincroniza datos frescos desde Firebase Realtime Database.
     */
    private fun cargarUsuarioActual() {
        viewModelScope.launch {
            // Obtenemos el usuario de Room que representa la sesión activa
            val localUser = obtenerUsuarioActualUseCase()
            if (localUser != null) {
                _usuario.value = localUser
                // Cargar datos en tiempo real de Firebase para tener la red y fecha de ingreso fresca
                try {
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        val snapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                            .getReference("usuarios").child(firebaseUser.uid).get().await()
                        val netId = snapshot.child("networkId").getValue(String::class.java) ?: ""
                        val fechaIngreso = snapshot.child("fechaIngreso").getValue(Long::class.java) ?: 0L
                        _usuario.value = localUser.copy(
                            networkId = netId,
                            fechaIngreso = fechaIngreso
                        )
                        val db = com.google.firebase.database.FirebaseDatabase.getInstance()
                        val globalSnap = db.getReference("configuracion_global").get().await()
                        if (globalSnap.exists()) {
                            val taf = globalSnap.child("tiempoAntiFalsa").getValue(Double::class.java)?.toFloat()
                            if (taf != null) {
                                _tiempoAntiFalsa.value = taf.coerceIn(1f, 10f)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            } else {
                // Fallback a Firebase Auth si Room está vacío temporalmente
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val nombre = when {
                        !firebaseUser.displayName.isNullOrBlank() -> firebaseUser.displayName!!
                        else -> firebaseUser.email?.substringBefore("@") ?: "Vecino"
                    }
                    try {
                        val snapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                            .getReference("usuarios").child(firebaseUser.uid).get().await()
                        val netId = snapshot.child("networkId").getValue(String::class.java) ?: ""
                        val fechaIngreso = snapshot.child("fechaIngreso").getValue(Long::class.java) ?: 0L
                        _usuario.value = Usuario(
                            id = 0,
                            nombre = nombre,
                            telefono = "",
                            correo = firebaseUser.email ?: "",
                            password = "",
                            rol = "usuario",
                            networkId = netId,
                            fechaIngreso = fechaIngreso
                        )
                    } catch (e: Exception) {
                        _usuario.value = Usuario(
                            id = 0,
                            nombre = nombre,
                            telefono = "",
                            correo = firebaseUser.email ?: "",
                            password = "",
                            rol = "usuario"
                        )
                    }
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error de alerta activa.
     */
    fun clearErrorAlerta() {
        _errorAlerta.value = null
    }

    /**
     * Activa una alerta de emergencia usando las coordenadas del usuario.
     * Verifica que se cumplan las políticas del administrador antes de actuar.
     */
    fun activarSOS() {
        val user = _usuario.value ?: return
        viewModelScope.launch {
            try {
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (user.networkId.isNotBlank() && user.networkId != currentUid) {
                    val db = com.google.firebase.database.FirebaseDatabase.getInstance()
                    val netSnapshot = db.getReference("networks").child(user.networkId).get().await()
                    if (netSnapshot.exists()) {
                        val esperarDiasNuevos = netSnapshot.child("esperarDiasNuevos").getValue(Int::class.java) ?: 0
                        if (esperarDiasNuevos > 0) {
                            val joinedTime = user.fechaIngreso
                            val now = System.currentTimeMillis()
                            val diffMillis = now - joinedTime
                            val diffDays = diffMillis / (1000 * 60 * 60 * 24)
                            if (diffDays < esperarDiasNuevos) {
                                val remainingDays = esperarDiasNuevos - diffDays
                                _errorAlerta.value = "Regla de Seguridad: Debes esperar $remainingDays días más antes de enviar SOS en esta red."
                                return@launch
                            }
                        }
                    }
                }

                val generatedId = activarAlertaUseCase(
                    usuarioId = user.id,
                    nombreUsuario = user.nombre,
                    latitud = user.latActual,
                    longitud = user.lonActual
                )
                _alertaId.value = generatedId.toInt()
                _alertaCreada.value = true
            } catch (e: Exception) {
                _errorAlerta.value = "Error al activar alerta: ${e.message}"
            }
        }
    }

    /**
     * Reinicia el estado de navegación de la alerta.
     */
    fun resetAlertaState() {
        _alertaId.value = null
        _alertaCreada.value = false
    }
}

/**
 * Fábrica para instanciar [HomeViewModel] inyectando casos de uso desde [AppModule].
 */
class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                AppModule.provideObtenerUsuarioActualUseCase(context),
                AppModule.provideActivarAlertaUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}

