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

/**
 * Estado de la pantalla de Login.
 */
data class LoginUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToHome: Boolean = false
)

class LoginViewModel(
    private val guardarUsuarioUseCase: GuardarUsuarioUseCase,
    private val obtenerUsuarioUseCase: ObtenerUsuarioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Se llama cada vez que el usuario escribe en el campo de teléfono. */
    fun onPhoneNumberChange(value: String) {
        // Solo dígitos, máximo 10 caracteres
        val digitsOnly = value.filter { it.isDigit() }.take(10)
        _uiState.value = _uiState.value.copy(
            phoneNumber = digitsOnly,
            errorMessage = null
        )
    }

    /** Se llama al presionar "Ingresar con número de Teléfono". */
    fun onLoginClick() {
        val phone = _uiState.value.phoneNumber

        if (phone.isBlank() || phone.length < 10) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Ingresa un número de teléfono válido de 10 dígitos"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Si ya existe, no lo duplica (el repositorio se encarga de eso)
            val usuarioExistente = obtenerUsuarioUseCase(phone)
            if (usuarioExistente == null) {
                guardarUsuarioUseCase(Usuario(nombre = "Vecino", telefono = phone))
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                navigateToHome = true
            )
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
                AppModule.provideObtenerUsuarioUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}