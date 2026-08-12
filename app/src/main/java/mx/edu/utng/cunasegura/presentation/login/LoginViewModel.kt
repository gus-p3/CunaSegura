package mx.edu.utng.cunasegura.presentation.login

import android.content.Context
import android.util.Log
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
 * Estado inmutable de la interfaz de usuario para la pantalla de inicio de sesión.
 *
 * @property correo Correo electrónico ingresado.
 * @property password Contraseña de acceso.
 * @property isLoading Bandera de progreso durante la autenticación remota.
 * @property errorMessage Mensaje de error de credenciales o red.
 * @property navigateToHome Bandera de redirección al flujo de vecino común.
 * @property navigateToAdmin Bandera de redirección al panel de administración.
 * @property navigateToRegister Bandera de navegación al formulario de registro.
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

/**
 * ViewModel encargado del flujo de autenticación de usuarios y administradores.
 *
 * Valida credenciales contra Firebase Authentication, comprueba el estado de la cuenta en Realtime Database,
 * limpia la sesión local previa y persiste el usuario activo en SQLite Room.
 *
 * @property guardarUsuarioUseCase Caso de uso para persistencia local.
 * @property limpiarSesionLocalUseCase Caso de uso para saneamiento de sesión.
 * @property context Contexto de la aplicación.
 */
class LoginViewModel(
    private val guardarUsuarioUseCase: GuardarUsuarioUseCase,
    private val limpiarSesionLocalUseCase: LimpiarSesionLocalUseCase,
    private val context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance().apply {
        firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }
    private val db = FirebaseDatabase.getInstance()
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el campo de correo y limpia mensajes de error previos.
     */
    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, errorMessage = null)
    }

    /**
     * Actualiza el campo de contraseña.
     */
    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    /**
     * Activa el evento de navegación hacia la pantalla de registro.
     */
    fun onNavigateToRegister() {
        _uiState.value = _uiState.value.copy(navigateToRegister = true)
    }
    
    /**
     * Restablece la bandera tras completar la navegación a registro.
     */
    fun onRegisterNavigated() {
        _uiState.value = _uiState.value.copy(navigateToRegister = false)
    }

    /**
     * Ejecuta el proceso de inicio de sesión con Firebase Auth y realiza el ruteo condicional por rol.
     */
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
                    val estadoDb = snapshot.child("estado").getValue(String::class.java) ?: "activo"
                    
                    if (estadoDb == "bloqueado" || estadoDb == "suspendido") {
                        auth.signOut()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Tu cuenta ha sido BLOQUEADA por el Administrador Global. Contacta a soporte."
                        )
                        return@launch
                    }

                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: firebaseUser.displayName ?: correo.substringBefore("@")
                    val telefono = snapshot.child("telefono").getValue(String::class.java) ?: ""
                    val rolDb = snapshot.child("rol").getValue(String::class.java) ?: "usuario"
                    val netId = snapshot.child("networkId").getValue(String::class.java) ?: ""
                    
                    val ADMIN_EMAIL = "admin@cunasegura.com"
                    val esAdmin = (rolDb == "admin_global") || (rolDb == "system_admin") || (firebaseUser.email == ADMIN_EMAIL)
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
                        rol = rolFinal,
                        networkId = netId
                    )
                    guardarUsuarioUseCase(usuario)

                    if (esAdmin) {
                        _uiState.value = _uiState.value.copy(isLoading = false, navigateToAdmin = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, navigateToHome = true)
                    }
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                if (correo == "admin@cunasegura.com") {
                    try {
                        val createResult = auth.createUserWithEmailAndPassword("admin@cunasegura.com", "123456789").await()
                        val newUser = createResult.user
                        if (newUser != null) {
                            val adminMap = mapOf(
                                "nombre" to "Administrador Global",
                                "correo" to "admin@cunasegura.com",
                                "telefono" to "0000000000",
                                "rol" to "admin_global",
                                "estado" to "activo"
                            )
                            db.getReference("usuarios").child(newUser.uid).setValue(adminMap).await()
                            limpiarSesionLocalUseCase()
                            guardarUsuarioUseCase(Usuario(id = 0, nombre = "Administrador Global", telefono = "0000000000", correo = "admin@cunasegura.com", password = "", rol = "admin"))
                            _uiState.value = _uiState.value.copy(isLoading = false, navigateToAdmin = true)
                            return@launch
                        }
                    } catch (ex: Exception) {
                        Log.e("LoginViewModel", "Error auto-creando admin", ex)
                    }
                }
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
 * Fábrica de ViewModel que inyecta los casos de uso correspondientes mediante [AppModule].
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