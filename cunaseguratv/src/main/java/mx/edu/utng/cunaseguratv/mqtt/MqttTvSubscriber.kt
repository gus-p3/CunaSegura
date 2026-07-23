package mx.edu.utng.cunaseguratv.mqtt

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

@kotlinx.serialization.Serializable
data class TvVinculacionMqttMessage(
    val tvId: String,
    val networkId: String
)

class MqttTvSubscriber(
    private val alertFlow: MutableStateFlow<AlertaMqttMessage?>,
    private val vinculacionFlow: MutableStateFlow<TvVinculacionMqttMessage?> = MutableStateFlow(null)
) {
    private var client: MqttAsyncClient? = null
    private val TAG = "MQTT_TV"
    private val clientId = MqttConfig.CLIENT_TV_BASE + UUID.randomUUID().toString().substring(0, 8)
    private var networkId: String = ""

    val isConnected = MutableStateFlow(false)

    fun setNetworkId(id: String) {
        networkId = id
        // Si ya está conectado y cambia la red, podríamos re-suscribir a topics específicos por red
    }

    fun connect() {
        if (client?.isConnected == true) return

        try {
            client = MqttAsyncClient(
                MqttConfig.BROKER_URL,
                clientId,
                MemoryPersistence()
            )

            client?.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String, msg: MqttMessage) {
                    try {
                        val payloadStr = String(msg.payload)
                        Log.d(TAG, "Mensaje MQTT recibido en topic $topic: $payloadStr")
                        
                        if (topic == MqttConfig.TOPIC_VINCULACION) {
                            val vinculacionMsg = Json.decodeFromString<TvVinculacionMqttMessage>(payloadStr)
                            vinculacionFlow.value = vinculacionMsg
                        } else if (topic == MqttConfig.TOPIC_ALERTAS) {
                            val alertaMsg = Json.decodeFromString<AlertaMqttMessage>(payloadStr)
                            
                            // Filtrar: la TV debe estar vinculada (networkId no vacío) y la alerta debe pertenecer a su misma red vecinal
                            if (networkId.isNotEmpty() && (alertaMsg.networkId == networkId || alertaMsg.networkId.isEmpty())) {
                                if (alertaMsg.estado == "activa") {
                                    alertFlow.value = alertaMsg
                                } else if (alertaMsg.estado == "cancelada") {
                                    // Si se cancela y es la misma alerta activa, limpiamos
                                    if (alertFlow.value?.usuarioId == alertaMsg.usuarioId) {
                                        alertFlow.value = null
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al decodificar mensaje MQTT: ${e.message}")
                    }
                }
                override fun connectionLost(cause: Throwable?) {
                    Log.w(TAG, "Conexión perdida con el broker MQTT: ${cause?.message}")
                    isConnected.value = false
                    // Implementar reconexión automática si es necesario
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isCleanSession = true
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
                connectionTimeout = 10
                keepAliveInterval = 20
                isAutomaticReconnect = true
            }

            Log.d(TAG, "Conectando al broker MQTT: ${MqttConfig.BROKER_URL} ...")
            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(token: IMqttToken?) {
                    isConnected.value = true
                    Log.d(TAG,"✅ TV conectada a MQTT con ID: $clientId")
                    
                    // Suscribirse a las alertas generales (o por red vecinal si el topic incluyera el ID)
                    client?.subscribe(MqttConfig.TOPIC_ALERTAS, MqttConfig.QOS)
                    client?.subscribe(MqttConfig.TOPIC_VINCULACION, MqttConfig.QOS)
                    Log.d(TAG,"✅ Suscrito a ${MqttConfig.TOPIC_ALERTAS} y ${MqttConfig.TOPIC_VINCULACION}")
                    
                    publicarEstadoTv(true)
                }
                override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                    isConnected.value = false
                    Log.e(TAG,"❌ Error de conexión MQTT", ex)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando MQTT client", e)
        }
    }

    private fun publicarEstadoTv(online: Boolean) {
        try {
            if (client?.isConnected == true) {
                val statusMsg = TvStatusMessage(tvId = clientId, networkId = networkId, isOnline = online)
                val payload = Json.encodeToString(TvStatusMessage.serializer(), statusMsg).toByteArray()
                client?.publish(MqttConfig.TOPIC_STATUS, payload, MqttConfig.QOS, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error publicando estado", e)
        }
    }

    fun disconnect() {
        try {
            publicarEstadoTv(false)
            client?.disconnect()
            isConnected.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error desconectando MQTT", e)
        }
    }
}
