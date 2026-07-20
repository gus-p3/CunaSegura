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

data class TvUiState(
    val isVinculada: Boolean = false,
    val networkId: String = "",
    val qrCode: Bitmap? = null,
    val mqttConnected: Boolean = false,
    val alertaActiva: AlertaMqttMessage? = null,
    val alertasRecientes: List<AlertaTV> = emptyList(),
    val showAlertModal: Boolean = false,
    val isSilenced: Boolean = false,
    val vecinosLocations: List<VecinoLocation> = emptyList()
)

class TvMonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    private val alertMqttFlow = MutableStateFlow<AlertaMqttMessage?>(null)
    private val vinculacionMqttFlow = MutableStateFlow<TvVinculacionMqttMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(alertMqttFlow, vinculacionMqttFlow)
    
    private val firebaseListener = FirebaseAlertListener()
    private var mediaPlayer: android.media.MediaPlayer? = null

    init {
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
                if (mqttAlerta != null) {
                    reproducirAlarma()
                    _state.update { 
                        it.copy(
                            alertaActiva = mqttAlerta,
                            showAlertModal = true,
                            isSilenced = false
                        )
                    }
                } else {
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

        // Escuchar alertas históricas/recientes en Firebase
        viewModelScope.launch {
            firebaseListener.escucharAlertasActivas().collect { alertas ->
                _state.update { it.copy(alertasRecientes = alertas) }
            }
        }

        // Escuchar vinculaciones vía MQTT
        viewModelScope.launch {
            vinculacionMqttFlow.collect { vinculacion ->
                if (vinculacion != null) {
                    val prefs = getApplication<Application>().getSharedPreferences("CunaSeguraTV", Context.MODE_PRIVATE)
                    val tvId = prefs.getString("tvId", null)
                    if (tvId != null && tvId == vinculacion.tvId) {
                        simularVinculacionExitosa(vinculacion.networkId)
                    }
                }
            }
        }
    }

    private fun escucharVecinosFirebase(networkId: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")
        dbRef.orderByChild("networkId").equalTo(networkId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<VecinoLocation>()
                    for (child in snapshot.children) {
                        val lat = child.child("latActual").getValue(Double::class.java) ?: 0.0
                        val lon = child.child("lonActual").getValue(Double::class.java) ?: 0.0
                        val nombre = child.child("nombre").getValue(String::class.java) ?: "Vecino"
                        val id = child.key ?: ""
                        if (lat != 0.0 && lon != 0.0) {
                            list.add(VecinoLocation(id, nombre, lat, lon))
                        }
                    }
                    _state.update { it.copy(vecinosLocations = list) }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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

    private fun generarQRVinculacion() {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("CunaSeguraTV", Context.MODE_PRIVATE)
            var tvId = prefs.getString("tvId", null)
            if (tvId == null) {
                tvId = UUID.randomUUID().toString()
                prefs.edit().putString("tvId", tvId).apply()
            }
            
            // Listen to Firebase for this tvId
            val dbRef = FirebaseDatabase.getInstance().getReference("tvs").child(tvId).child("networkId")
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val networkId = snapshot.getValue(String::class.java)
                    if (!networkId.isNullOrEmpty()) {
                        simularVinculacionExitosa(networkId)
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

    fun simularVinculacionExitosa(networkId: String = "RED-VECINAL-123") {
        mqttSubscriber.setNetworkId(networkId)
        escucharVecinosFirebase(networkId)
        _state.update { 
            it.copy(
                isVinculada = true,
                networkId = networkId
            )
        }
    }

    fun descartarModalAlerta() {
        _state.update { it.copy(showAlertModal = false) }
    }

    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
        detenerAlarma()
    }
}
