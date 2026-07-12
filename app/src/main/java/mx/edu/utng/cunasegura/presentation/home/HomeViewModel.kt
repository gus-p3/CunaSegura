package mx.edu.utng.cunasegura.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.ActivarAlertaUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

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

    init {
        cargarUsuarioActual()
    }

    private fun cargarUsuarioActual() {
        viewModelScope.launch {
            // Obtenemos el usuario de Room que representa la sesión activa
            val localUser = obtenerUsuarioActualUseCase()
            if (localUser != null) {
                _usuario.value = localUser
            } else {
                // Fallback a Firebase Auth si Room está vacío temporalmente
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val nombre = when {
                        !firebaseUser.displayName.isNullOrBlank() -> firebaseUser.displayName!!
                        else -> firebaseUser.email?.substringBefore("@") ?: "Vecino"
                    }
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

    /**
     * Activa una alerta de emergencia usando las coordenadas del usuario.
     */
    fun activarSOS() {
        val user = _usuario.value ?: return
        viewModelScope.launch {
            val generatedId = activarAlertaUseCase(
                usuarioId = user.id,
                latitud = user.latActual,
                longitud = user.lonActual
            )
            _alertaId.value = generatedId.toInt()
            _alertaCreada.value = true
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
