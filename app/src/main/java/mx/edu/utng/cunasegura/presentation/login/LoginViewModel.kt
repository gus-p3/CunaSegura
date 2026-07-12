package mx.edu.utng.cunasegura.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
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

/**
 * Estado de la pantalla de Login.
 */
data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToHome: Boolean = false,
    val navigateToAdmin: Boolean = false,
    val navigateToRegister: Boolean = false
)

class LoginViewModel(
    private val guardarUsuarioUseCase: GuardarUsuarioUseCase,
    private val limpiarSesionLocalUseCase: LimpiarSesionLocalUseCase,
    private val context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onNavigateToRegister() {
        _uiState.value = _uiState.value.copy(navigateToRegister = true)
    }
    
    fun onRegisterNavigated() {
        _uiState.value = _uiState.value.copy(navigateToRegister = false)
    }

    fun onLoginClick() {
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
            try {
                val result = auth.signInWithEmailAndPassword(correo, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // Fetch real user data from Realtime Database
                    val snapshot = db.getReference("usuarios").child(firebaseUser.uid).get().await()
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: firebaseUser.displayName ?: correo.substringBefore("@")
                    val telefono = snapshot.child("telefono").getValue(String::class.java) ?: ""
                    val rolDb = snapshot.child("rol").getValue(String::class.java) ?: "usuario"
                    
                    val ADMIN_EMAIL = "admin@cunasegura.com"
                    val esAdmin = (rolDb == "admin") || (firebaseUser.email == ADMIN_EMAIL)
                    val rolFinal = if (esAdmin) "admin" else "usuario"

                    // Clear previous session so Room LIMIT 1 works correctly for this new user
                    limpiarSesionLocalUseCase()

                    // Save user to local Room DB
                    val usuario = Usuario(
                        id = 0,
                        nombre = nombre,
                        telefono = telefono,
                        correo = correo,
                        password = "",
                        rol = rolFinal
                    )
                    guardarUsuarioUseCase(usuario)

                    if (esAdmin) {
                        _uiState.value = _uiState.value.copy(isLoading = false, navigateToAdmin = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, navigateToHome = true)
                    }
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No existe una cuenta con ese correo. ¿Quieres registrarte?"
                )
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Contraseña incorrecta. Verifica tus datos."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.localizedMessage ?: "Sin conexión a internet"}"
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
                AppModule.provideLimpiarSesionLocalUseCase(context),
                context
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}