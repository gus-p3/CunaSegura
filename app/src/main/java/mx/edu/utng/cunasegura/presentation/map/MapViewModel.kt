package mx.edu.utng.cunasegura.presentation.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

/**
 * ViewModel para [CommunityMapScreen].
 *
 * Carga la ubicación del usuario (lat/lng de Room) y observa
 * las alertas activas del mismo usuario de forma reactiva.
 */
class MapViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val alertaRepository: IAlertaRepository
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private val _activeAlerts = MutableStateFlow<List<Alerta>>(emptyList())
    val activeAlerts: StateFlow<List<Alerta>> = _activeAlerts.asStateFlow()

    init {
        cargarUsuario()
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase()
            _usuario.value = usuario
            if (usuario != null) {
                // Actualizar la posición del marcador con lat/lng de Room
                if (usuario.latActual != 0.0 || usuario.lonActual != 0.0) {
                    _userLocation.value = LatLng(usuario.latActual, usuario.lonActual)
                } else {
                    // Coordenadas por defecto: Dolores Hidalgo, Guanajuato
                    _userLocation.value = LatLng(21.1565, -100.9327)
                }

                // Observar alertas activas del usuario de forma reactiva
                alertaRepository.obtenerAlertaActiva(usuario.id)
                    .onEach { alerta ->
                        _activeAlerts.value = if (alerta != null) listOf(alerta) else emptyList()
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
}

class MapViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(
                obtenerUsuarioActualUseCase = AppModule.provideObtenerUsuarioActualUseCase(context),
                alertaRepository = AppModule.provideAlertaRepository(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
