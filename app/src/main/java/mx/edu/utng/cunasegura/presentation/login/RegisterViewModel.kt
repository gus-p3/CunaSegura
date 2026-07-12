package mx.edu.utng.cunasegura.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.GuardarUsuarioUseCase
import mx.edu.utng.cunasegura.domain.usecase.LimpiarSesionLocalUseCase

data class RegisterUiState(
    val nombre: String = "",
    val telefono: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

class RegisterViewModel(
    private val guardarUsuarioUseCase: GuardarUsuarioUseCase,
    private val limpiarSesionLocalUseCase: LimpiarSesionLocalUseCase
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, errorMessage = null)
    }

    fun onTelefonoChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(10)
        _uiState.value = _uiState.value.copy(telefono = digitsOnly, errorMessage = null)
    }

    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun onRegisterClick() {
        val state = _uiState.value
        val nombre = state.nombre.trim()
        val telefono = state.telefono
        val correo = state.correo.trim()
        val password = state.password
        val confirm = state.confirmPassword

        if (nombre.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Ingresa tu nombre completo")
            return
        }
        if (telefono.length < 10) {
            _uiState.value = state.copy(errorMessage = "Ingresa un número de teléfono de 10 dígitos")
            return
        }
        if (correo.isBlank() || !correo.contains("@")) {
            _uiState.value = state.copy(errorMessage = "Ingresa un correo válido")
            return
        }
        if (password.length < 6) {
            _uiState.value = state.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        if (password != confirm) {
            _uiState.value = state.copy(errorMessage = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // 1. Create user in Firebase Auth
                val result = auth.createUserWithEmailAndPassword(correo, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // 2. Set display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()
                    
                    // 3. Save user data to Firebase Realtime Database
                    val db = FirebaseDatabase.getInstance()
                    val userData = mapOf(
                        "uid" to firebaseUser.uid,
                        "nombre" to nombre,
                        "telefono" to telefono,
                        "correo" to correo,
                        "rol" to "usuario",
                        "estado" to "activo",
                        "creadoEn" to System.currentTimeMillis()
                    )
                    db.getReference("usuarios").child(firebaseUser.uid).setValue(userData).await()
                    
                    // 4. Save to local Room for offline access (clear previous session first)
                    limpiarSesionLocalUseCase()
                    val nuevoUsuario = Usuario(
                        id = 0,
                        nombre = nombre,
                        telefono = telefono,
                        correo = correo,
                        password = "",
                        rol = "usuario"
                    )
                    guardarUsuarioUseCase(nuevoUsuario)
                    
                    _uiState.value = _uiState.value.copy(isLoading = false, registerSuccess = true)
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ya existe una cuenta con ese correo. Inicia sesión."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.localizedMessage ?: "No se pudo crear la cuenta"}"
                )
            }
        }
    }
}

class RegisterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(
                AppModule.provideGuardarUsuarioUseCase(context),
                AppModule.provideLimpiarSesionLocalUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
