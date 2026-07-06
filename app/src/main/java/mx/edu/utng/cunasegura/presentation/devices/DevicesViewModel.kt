package mx.edu.utng.cunasegura.presentation.devices

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.data.local.dao.ContactoDao
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

class DevicesViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val contactoDao: ContactoDao
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _contactCount = MutableStateFlow(0)
    val contactCount: StateFlow<Int> = _contactCount.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            val user = obtenerUsuarioActualUseCase()
            _usuario.value = user
            if (user != null) {
                // Cuenta los contactos guardados en Room
                val contactos = contactoDao.obtenerPorUsuario(user.id)
                _contactCount.value = contactos.size
            }
        }
    }
}

class DevicesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevicesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DevicesViewModel(
                obtenerUsuarioActualUseCase = AppModule.provideObtenerUsuarioActualUseCase(context),
                contactoDao = AppModule.provideDatabase(context).contactoDao()
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
