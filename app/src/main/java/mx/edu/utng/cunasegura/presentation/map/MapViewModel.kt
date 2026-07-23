package mx.edu.utng.cunasegura.presentation.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.util.GeoPoint
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

data class VecinoLocationMobile(
    val id: String,
    val nombre: String,
    val lat: Double,
    val lon: Double
)

/**
 * ViewModel para [CommunityMapScreen].
 *
 * Carga la ubicación del usuario (lat/lng de Room), observa vecinos de la red
 * y las alertas activas de la comunidad en tiempo real.
 */
class MapViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val alertaRepository: IAlertaRepository
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    private val _vecinosLocations = MutableStateFlow<List<VecinoLocationMobile>>(emptyList())
    val vecinosLocations: StateFlow<List<VecinoLocationMobile>> = _vecinosLocations.asStateFlow()

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
                if (usuario.latActual != 0.0 || usuario.lonActual != 0.0) {
                    _userLocation.value = GeoPoint(usuario.latActual, usuario.lonActual)
                } else {
                    _userLocation.value = GeoPoint(21.1565, -100.9327)
                }

                // Escuchar alertas activas de la comunidad en tiempo real (Firebase)
                alertaRepository.obtenerAlertasVecinalesActivas()
                    .onEach { alertas ->
                        _activeAlerts.value = alertas
                    }
                    .launchIn(viewModelScope)

                // Escuchar vecinos de la red en Firebase
                escucharVecinosEnFirebase(usuario.networkId)
            }
        }
    }

    private fun escucharVecinosEnFirebase(networkId: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")
        
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<VecinoLocationMobile>()
                for (child in snapshot.children) {
                    val uid = child.key ?: ""
                    if (uid == currentUid) continue // El usuario actual se maneja por separado

                    val userNetId = child.child("networkId").getValue(String::class.java) ?: ""
                    if (networkId.isNotEmpty() && (userNetId == networkId || uid == networkId)) {
                        val nombre = child.child("nombre").getValue(String::class.java) ?: "Vecino"
                        val lat = child.child("latActual").getValue(Double::class.java) ?: 0.0
                        val lon = child.child("lonActual").getValue(Double::class.java) ?: 0.0
                        if (lat != 0.0 && lon != 0.0) {
                            list.add(VecinoLocationMobile(uid, nombre, lat, lon))
                        }
                    }
                }
                _vecinosLocations.value = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setUbicacionUsuario(lat: Double, lon: Double) {
        _userLocation.value = GeoPoint(lat, lon)
        actualizarUbicacionEnFirebase(lat, lon)
    }

    private fun actualizarUbicacionEnFirebase(lat: Double, lon: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
        db.child("latActual").setValue(lat)
        db.child("lonActual").setValue(lon)
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
