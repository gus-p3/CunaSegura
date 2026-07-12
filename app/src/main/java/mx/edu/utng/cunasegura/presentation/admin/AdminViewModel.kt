package mx.edu.utng.cunasegura.presentation.admin

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

class AdminViewModel(
    private val usuarioRepository: mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository,
    private val alertaRepository: mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _totalUsuarios = MutableStateFlow(0)
    val totalUsuarios: StateFlow<Int> = _totalUsuarios.asStateFlow()

    private val _adminActual = MutableStateFlow<Usuario?>(null)
    val adminActual: StateFlow<Usuario?> = _adminActual.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            val todos = usuarioRepository.obtenerTodosLosUsuarios()
            _usuarios.value = todos
            _totalUsuarios.value = todos.size
            _adminActual.value = usuarioRepository.obtenerUsuarioActual()
        }
    }

    fun recargar() = cargarDatos()
}

class AdminViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(
                AppModule.provideUsuarioRepository(context),
                AppModule.provideAlertaRepository(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
