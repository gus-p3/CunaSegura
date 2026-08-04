package mx.edu.utng.cunaseguratv.presentation

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.edu.utng.cunaseguratv.data.AlertaTV
import mx.edu.utng.cunaseguratv.data.FirebaseAlertListener
import mx.edu.utng.cunaseguratv.mqtt.AlertaMqttMessage
import mx.edu.utng.cunaseguratv.mqtt.MqttTvSubscriber
import mx.edu.utng.cunaseguratv.mqtt.TvVinculacionMqttMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

data class VecinoLocation(val id: String, val nombre: String, val lat: Double, val lon: Double)

data class VecinoInfo(
    val id: String,
    val nombre: String,
    val telefono: String,
    val correo: String,
    val lat: Double,
    val lon: Double
)

data class TvUiState(
    val isVinculada: Boolean = false,
    val networkId: String = "",
    val networkNombre: String = "",
    val usuarioNombre: String = "",
    val usuarioCorreo: String = "",
    val usuarioId: String = "",
    val qrCode: Bitmap? = null,
    val mqttConnected: Boolean = false,
    val alertaActiva: AlertaMqttMessage? = null,
    val alertasRecientes: List<AlertaTV> = emptyList(),
    val showAlertModal: Boolean = false,
    val isSilenced: Boolean = false,
    val vecinosLocations: List<VecinoLocation> = emptyList(),
    val vecinosList: List<VecinoInfo> = emptyList(),
    // Colores personalizables de los marcadores del mapa
    val colorUsuario: Int = android.graphics.Color.parseColor("#2196F3"),   // Azul
    val colorVecinos: Int = android.graphics.Color.parseColor("#4CAF50"),   // Verde
    val colorAlertas: Int = android.graphics.Color.parseColor("#F44336"),   // Rojo
    val showColorPicker: Boolean = false
)

class TvMonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    private val alertMqttFlow = MutableStateFlow<AlertaMqttMessage?>(null)
    private val vinculacionMqttFlow = MutableStateFlow<TvVinculacionMqttMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(alertMqttFlow, vinculacionMqttFlow)
    
    private val firebaseListener = FirebaseAlertListener()
    private var mediaPlayer: android.media.MediaPlayer? = null

    private var netInfoListenerRef: com.google.firebase.database.DatabaseReference? = null
    private var netInfoListener: ValueEventListener? = null

    private var titularListenerRef: com.google.firebase.database.DatabaseReference? = null
    private var titularListener: ValueEventListener? = null

    private var vecinosListenerRef: com.google.firebase.database.DatabaseReference? = null
    private var vecinosListener: ValueEventListener? = null

    init {
        // Cargar colores personalizados guardados previamente
        cargarColores()

        // Inicialmente generar QR de vinculación
        generarQRVinculacion()
        
        // Conectar a MQTT
        mqttSubscriber.connect()

        viewModelScope.launch {
            mqttSubscriber.isConnected.collect { connected ->
                _state.update { it.copy(mqttConnected = connected) }
            }
        }

        // Escuchar alertas instantáneas vía MQTT
        viewModelScope.launch {
            alertMqttFlow.collect { mqttAlerta ->
                // Solo reproducir alarma si la TV está vinculada
                if (mqttAlerta != null && _state.value.isVinculada) {
                    reproducirAlarma()
                    _state.update { 
                        it.copy(
                            alertaActiva = mqttAlerta,
                            showAlertModal = true,
                            isSilenced = false
                        )
                    }
                } else if (mqttAlerta == null) {
                    // Si se anula es porque fue cancelada
                    detenerAlarma()
                    _state.update { 
                        it.copy(
                            alertaActiva = null,
                            showAlertModal = false,
                            isSilenced = false
                        )
                    }
                }
            }
        }

        // Escuchar alertas históricas/recientes en Firebase (solo para la red vinculada)
        viewModelScope.launch {
            firebaseListener.escucharAlertasActivas().collect { alertas ->
                val currentNetId = _state.value.networkId
                val alertasFiltradas = if (currentNetId.isNotEmpty()) {
                    alertas.filter { it.networkId == currentNetId || it.networkId.isEmpty() }
                } else emptyList()

                _state.update { it.copy(alertasRecientes = alertasFiltradas) }
                
                if (_state.value.isVinculada) {
                    val masReciente = alertasFiltradas.firstOrNull { it.estado == "activa" }
                    if (masReciente != null && _state.value.alertaActiva == null) {
                        val alertaMqtt = AlertaMqttMessage(
                            usuarioId = masReciente.usuarioId,
                            nombreUsuario = masReciente.nombreUsuario,
                            latitud = masReciente.latitud,
                            longitud = masReciente.longitud,
                            estado = masReciente.estado,
                            timestamp = masReciente.creadoEn
                        )
                        reproducirAlarma()
                        _state.update {
                            it.copy(
                                alertaActiva = alertaMqtt,
                                showAlertModal = true,
                                isSilenced = false
                            )
                        }
                    } else if (alertasFiltradas.none { it.estado == "activa" } && _state.value.alertaActiva != null) {
                        detenerAlarma()
                        _state.update {
                            it.copy(
                                alertaActiva = null,
                                showAlertModal = false,
                                isSilenced = false
                            )
                        }
                    }
                }
            }
        }

        // Escuchar vinculaciones vía MQTT
        viewModelScope.launch {
            vinculacionMqttFlow.collect { vinculacion ->
                if (vinculacion != null) {
                    val prefs = getApplication<Application>().getSharedPreferences("CunaSeguraTV", Context.MODE_PRIVATE)
                    val tvId = prefs.getString("tvId", null)
                    if (tvId != null && tvId == vinculacion.tvId) {
                        simularVinculacionExitosa(vinculacion.networkId, null)
                    }
                }
            }
        }
    }

    private fun escucharVecinosYRedFirebase(networkId: String, linkedBy: String?) {
        val db = FirebaseDatabase.getInstance()

        // Limpiar listeners anteriores para evitar conflictos fantasma
        netInfoListenerRef?.let { ref -> netInfoListener?.let { l -> ref.removeEventListener(l) } }
        titularListenerRef?.let { ref -> titularListener?.let { l -> ref.removeEventListener(l) } }
        vecinosListenerRef?.let { ref -> vecinosListener?.let { l -> ref.removeEventListener(l) } }

        // 1. Escuchar la información de la red si existe en /networks
        val netRef = db.getReference("networks").child(networkId)
        netInfoListenerRef = netRef
        netInfoListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val netName = snapshot.child("nombre").getValue(String::class.java) ?: "Red Vecinal"
                        _state.update { it.copy(networkNombre = netName) }
                    } else {
                        // Si no está en /networks, intentar buscar el usuario creador de la red
                        db.getReference("usuarios").child(networkId).child("nombre").get()
                            .addOnSuccessListener { userSnap ->
                                val name = userSnap.getValue(String::class.java)
                                val defaultNetName = if (!name.isNullOrBlank()) "Red de $name" else "Red Vecinal"
                                _state.update { it.copy(networkNombre = defaultNetName) }
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
        netRef.addValueEventListener(netInfoListener!!)

        // 2. Escuchar usuario vinculador o titular de la red
        val userToListen = if (!linkedBy.isNullOrEmpty()) linkedBy else networkId
        val titRef = db.getReference("usuarios").child(userToListen)
        titularListenerRef = titRef
        titularListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Vecino"
                        val correo = snapshot.child("correo").getValue(String::class.java) ?: ""
                        _state.update { 
                            it.copy(
                                usuarioNombre = nombre,
                                usuarioCorreo = correo,
                                usuarioId = userToListen
                            ) 
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
        titRef.addValueEventListener(titularListener!!)

        // 3. Escuchar a todos los vecinos pertenecientes a esta red en /usuarios
        val vecRef = db.getReference("usuarios")
        vecinosListenerRef = vecRef
        vecinosListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val locations = mutableListOf<VecinoLocation>()
                    val infoList = mutableListOf<VecinoInfo>()
                    var titularNombre = _state.value.usuarioNombre
                    var titularCorreo = _state.value.usuarioCorreo

                    for (child in snapshot.children) {
                        val userNetId = child.child("networkId").getValue(String::class.java) ?: ""
                        val userId = child.key ?: ""
                        val nombre = child.child("nombre").getValue(String::class.java) ?: "Vecino"
                        val correo = child.child("correo").getValue(String::class.java) ?: ""
                        val telefono = child.child("telefono").getValue(String::class.java) ?: ""
                        val lat = child.child("latActual").getValue(Double::class.java) ?: 0.0
                        val lon = child.child("lonActual").getValue(Double::class.java) ?: 0.0
                        val rol = child.child("rol").getValue(String::class.java) ?: "usuario"

                        if (userNetId == networkId || userId == networkId) {
                            infoList.add(VecinoInfo(userId, nombre, telefono, correo, lat, lon))
                            if (lat != 0.0 && lon != 0.0) {
                                locations.add(VecinoLocation(userId, nombre, lat, lon))
                            }
                            // Priorizar al usuario que vinculó la TV
                            if (!linkedBy.isNullOrEmpty() && userId == linkedBy) {
                                titularNombre = nombre
                                titularCorreo = correo
                            } else if (linkedBy.isNullOrEmpty() && (titularNombre.isEmpty() || titularNombre == "Vecino" || userId == networkId)) {
                                titularNombre = nombre
                                titularCorreo = correo
                            }
                        }
                    }
                    _state.update { 
                        it.copy(
                            vecinosLocations = locations,
                            vecinosList = infoList,
                            usuarioNombre = if (titularNombre.isNotBlank()) titularNombre else it.usuarioNombre,
                            usuarioCorreo = if (titularCorreo.isNotBlank()) titularCorreo else it.usuarioCorreo,
                            usuarioId = userToListen
                        ) 
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
        vecRef.addValueEventListener(vecinosListener!!)
    }

    private fun reproducirAlarma() {
        if (mediaPlayer == null) {
            val context = getApplication<Application>().applicationContext
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(context, uri)
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.start()
    }

    private fun detenerAlarma() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun silenciarAlarma() {
        detenerAlarma()
        _state.update { it.copy(isSilenced = true) }
    }

    fun cerrarSesion() {
        val prefs = getApplication<Application>().getSharedPreferences("CunaSeguraTV", Context.MODE_PRIVATE)
        val tvId = prefs.getString("tvId", null)
        if (tvId != null) {
            FirebaseDatabase.getInstance().getReference("tvs").child(tvId).removeValue()
        }
        detenerAlarma()
        _state.update {
            TvUiState(
                isVinculada = false,
                networkId = "",
                networkNombre = "",
                usuarioNombre = "",
                usuarioCorreo = "",
                mqttConnected = it.mqttConnected,
                qrCode = it.qrCode
            )
        }
        generarQRVinculacion()
    }

    private fun generarQRVinculacion() {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("CunaSeguraTV", Context.MODE_PRIVATE)
            var tvId = prefs.getString("tvId", null)
            if (tvId == null) {
                tvId = UUID.randomUUID().toString()
                prefs.edit().putString("tvId", tvId).apply()
            }
            
            // Escuchar el nodo /tvs/$tvId en Firebase para detectar networkId y usuario vinculador
            val dbRef = FirebaseDatabase.getInstance().getReference("tvs").child(tvId)
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val networkId = snapshot.child("networkId").getValue(String::class.java)
                    val linkedBy = snapshot.child("linkedBy").getValue(String::class.java)

                    if (!networkId.isNullOrEmpty()) {
                        simularVinculacionExitosa(networkId, linkedBy)
                    }

                    if (!linkedBy.isNullOrEmpty()) {
                        escucharCambiosRedUsuario(tvId, linkedBy)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("TvMonitorViewModel", "Error en Firebase: ${error.message}")
                }
            })

            val content = "cunasegura://vincular?tvId=$tvId"
            
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                    }
                }
                
                _state.update { it.copy(qrCode = bmp) }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var usuarioListenerRef: com.google.firebase.database.DatabaseReference? = null
    private var usuarioListener: ValueEventListener? = null

    // Escucha en tiempo real si el usuario que vinculó esta TV se une o sale de una red vecinal en la app móvil
    private fun escucharCambiosRedUsuario(tvId: String, linkedByUid: String) {
        usuarioListenerRef?.let { ref ->
            usuarioListener?.let { l -> ref.removeEventListener(l) }
        }

        val userNetRef = FirebaseDatabase.getInstance().getReference("usuarios").child(linkedByUid).child("networkId")
        usuarioListenerRef = userNetRef
        usuarioListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newNetworkId = snapshot.getValue(String::class.java)
                if (!newNetworkId.isNullOrEmpty() && newNetworkId != _state.value.networkId) {
                    android.util.Log.d("TvMonitorViewModel", "🔔 La TV detectó cambio de red vecinal del usuario $linkedByUid a: $newNetworkId")
                    FirebaseDatabase.getInstance().getReference("tvs").child(tvId).child("networkId").setValue(newNetworkId)
                    simularVinculacionExitosa(newNetworkId, linkedByUid)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        userNetRef.addValueEventListener(usuarioListener!!)
    }

    fun simularVinculacionExitosa(networkId: String = "RED-VECINAL-123", linkedBy: String? = null) {
        mqttSubscriber.setNetworkId(networkId)
        escucharVecinosYRedFirebase(networkId, linkedBy)
        _state.update { 
            it.copy(
                isVinculada = true,
                networkId = networkId,
                usuarioId = linkedBy ?: it.usuarioId
            )
        }
    }

    fun descartarModalAlerta() {
        _state.update { it.copy(showAlertModal = false) }
    }

    fun toggleColorPicker() {
        _state.update { it.copy(showColorPicker = !it.showColorPicker) }
    }

    fun guardarColores(colorUsuario: Int, colorVecinos: Int, colorAlertas: Int) {
        val prefs = getApplication<Application>().getSharedPreferences("mapa_colores", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("colorUsuario", colorUsuario)
            .putInt("colorVecinos", colorVecinos)
            .putInt("colorAlertas", colorAlertas)
            .apply()
        _state.update { it.copy(colorUsuario = colorUsuario, colorVecinos = colorVecinos, colorAlertas = colorAlertas, showColorPicker = false) }
    }

    fun cargarColores() {
        val prefs = getApplication<Application>().getSharedPreferences("mapa_colores", android.content.Context.MODE_PRIVATE)
        val colorU = prefs.getInt("colorUsuario", android.graphics.Color.parseColor("#2196F3"))
        val colorV = prefs.getInt("colorVecinos", android.graphics.Color.parseColor("#4CAF50"))
        val colorA = prefs.getInt("colorAlertas", android.graphics.Color.parseColor("#F44336"))
        _state.update { it.copy(colorUsuario = colorU, colorVecinos = colorV, colorAlertas = colorA) }
    }

    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
        detenerAlarma()
    }
}
