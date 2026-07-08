package mx.edu.utng.cunasegura.presentation.login

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
import mx.edu.utng.cunasegura.domain.usecase.GuardarUsuarioUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioUseCase
import mx.edu.utng.cunasegura.domain.usecase.ValidarAdminUseCase

/**
 * Estado de la pantalla de Login.
 */
data class LoginUiState(
    // Modo vecino
    val phoneNumber: String = "",
    // Modo admin
    val correo: String = "",
    val password: String = "",
    // Modo activo
    val esAdmin: Boolean = false,
    // Estado
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToHome: Boolean = false,
    val navigateToAdmin: Boolean = false
)

class LoginViewModel(
    private val guardarUsuarioUseCase: GuardarUsuarioUseCase,
    private val obtenerUsuarioUseCase: ObtenerUsuarioUseCase,
    private val validarAdminUseCase: ValidarAdminUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Alterna entre modo vecino (teléfono) y modo admin (correo + contraseña). */
    fun onToggleAdminMode() {
        _uiState.value = _uiState.value.copy(
            esAdmin = !_uiState.value.esAdmin,
            errorMessage = null,
            phoneNumber = "",
            correo = "",
            password = ""
        )
    }

    /** Actualiza el número de teléfono (solo dígitos, máximo 10). */
    fun onPhoneNumberChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(10)
        _uiState.value = _uiState.value.copy(
            phoneNumber = digitsOnly,
            errorMessage = null
        )
    }

    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    /** Se llama al presionar el botón de ingresar. Delega al modo correcto. */
    fun onLoginClick() {
        if (_uiState.value.esAdmin) {
            loginAdmin()
        } else {
            loginVecino()
        }
    }

    // ------------------------------------------------------------------
    // Login vecino (número de teléfono)
    // ------------------------------------------------------------------
    private fun loginVecino() {
        val phone = _uiState.value.phoneNumber
        if (phone.isBlank() || phone.length < 10) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Ingresa un número de teléfono válido de 10 dígitos"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val usuarioExistente = obtenerUsuarioUseCase(phone)
            if (usuarioExistente == null) {
                guardarUsuarioUseCase(Usuario(nombre = "Vecino", telefono = phone))
            }
            _uiState.value = _uiState.value.copy(isLoading = false, navigateToHome = true)
        }
    }

    // ------------------------------------------------------------------
    // Login admin (correo + contraseña)
    // ------------------------------------------------------------------
    private fun loginAdmin() {
        val correo = _uiState.value.correo.trim()
        val password = _uiState.value.password

        if (correo.isBlank() || !correo.contains("@")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingresa un correo válido")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingresa la contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val admin = validarAdminUseCase(correo, password)
            if (admin != null) {
                // Guardar sesión del admin para el Splash en futuros arranques
                guardarUsuarioUseCase(admin)
                _uiState.value = _uiState.value.copy(isLoading = false, navigateToAdmin = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Credenciales incorrectas. Solo el administrador global puede ingresar aquí."
                )
            }
        }
    }
}

/**
 * Factory manual: crea LoginViewModel inyectando los UseCases desde AppModule.
 */
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(
                AppModule.provideGuardarUsuarioUseCase(context),
                AppModule.provideObtenerUsuarioUseCase(context),
                AppModule.provideValidarAdminUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}