package mx.edu.utng.cunaseguratv.mqtt

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

/**
 * Carga útil recibida en el tópico `cunasegura/tv/vinculacion` cuando el smartphone
 * escanea el código QR de la Smart TV y envía el emparejamiento con la red vecinal.
 *
 * @property tvId Identificador único de la TV que se vinculó.
 * @property networkId Identificador de la red vecinal asociada a la vinculación.
 */
@kotlinx.serialization.Serializable
data class TvVinculacionMqttMessage(
    val tvId: String,
    val networkId: String
)

/**
 * Cliente suscriptor MQTT asíncrono diseñado para Android TV.
 *
 * Implementa [MqttCallback] y [IMqttActionListener] mediante la librería Eclipse Paho MQTT.
 * Mantiene una conexión ligera TCP/SSL abierta con el broker HiveMQ, procesa la llegada
 * de alertas de seguridad instantáneas, filtra mensajes por `networkId` y emite
 * notificaciones reactivas a través de [MutableStateFlow].
 *
 * @property alertFlow Flujo reactivo mutable donde se emiten las alertas recibidas en tiempo real.
 * @property vinculacionFlow Flujo reactivo mutable donde se despachan eventos de vinculación remota.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
class MqttTvSubscriber(
    private val alertFlow: MutableStateFlow<AlertaMqttMessage?>,
    private val vinculacionFlow: MutableStateFlow<TvVinculacionMqttMessage?> = MutableStateFlow(null)
) {
    private var client: MqttAsyncClient? = null
    private val TAG = "MQTT_TV"
    private val clientId = MqttConfig.CLIENT_TV_BASE + UUID.randomUUID().toString().substring(0, 8)
    private var networkId: String = ""

    /** Estado reactivo que expone si la TV mantiene una conexión activa con el broker MQTT. */
    val isConnected = MutableStateFlow(false)

    /**
     * Actualiza el identificador de la red vecinal a la que pertenece esta Smart TV.
     * Permite filtrar alertas entrantes para no mostrar eventos de otras vecindades.
     *
     * @param id Identificador único de la red vecinal.
     */
    fun setNetworkId(id: String) {
        networkId = id
    }

    /**
     * Establece la conexión asíncrona con el broker MQTT configurado en [MqttConfig].
     *
     * Configura:
     * - Opciones SSL/TLS con [javax.net.ssl.SSLSocketFactory].
     * - Reconexión automática habilitada (`isAutomaticReconnect = true`).
     * - Tiempos de keep-alive (20s) y timeout de conexión (10s).
     * - Suscripción a tópicos de alertas y vinculación tras una conexión exitosa.
     * - Notificación de estado en línea mediante [publicarEstadoTv].
     */
    fun connect() {
        if (client?.isConnected == true) return

        try {
            client = MqttAsyncClient(
                MqttConfig.BROKER_URL,
                clientId,
                MemoryPersistence()
            )

            client?.setCallback(object : MqttCallback {
                /**
                 * Callback invocado cuando arriba un nuevo mensaje MQTT en cualquiera de los tópicos suscritos.
                 *
                 * @param topic Nombre del tópico en el que se recibió el mensaje.
                 * @param msg Objeto de mensaje con la carga binaria (payload) recibida.
                 */
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

                /**
                 * Invocado cuando la conexión con el broker MQTT se interrumpe inesperadamente.
                 *
                 * @param cause Causa de la desconexión.
                 */
                override fun connectionLost(cause: Throwable?) {
                    Log.w(TAG, "Conexión perdida con el broker MQTT: ${cause?.message}")
                    isConnected.value = false
                }

                /**
                 * Invocado cuando se completa la entrega de un mensaje publicado (QoS 1 o 2).
                 *
                 * @param token Token de entrega asociado.
                 */
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
                /**
                 * Invocado cuando la conexión con el broker se establece exitosamente.
                 * Realiza la suscripción a los tópicos y publica la presencia en línea.
                 */
                override fun onSuccess(token: IMqttToken?) {
                    isConnected.value = true
                    Log.d(TAG,"✅ TV conectada a MQTT con ID: $clientId")
                    
                    // Suscribirse a las alertas generales y tópicos de vinculación
                    client?.subscribe(MqttConfig.TOPIC_ALERTAS, MqttConfig.QOS)
                    client?.subscribe(MqttConfig.TOPIC_VINCULACION, MqttConfig.QOS)
                    Log.d(TAG,"✅ Suscrito a ${MqttConfig.TOPIC_ALERTAS} y ${MqttConfig.TOPIC_VINCULACION}")
                    
                    publicarEstadoTv(true)
                }

                /**
                 * Invocado cuando la solicitud de conexión al broker falla.
                 */
                override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                    isConnected.value = false
                    Log.e(TAG,"❌ Error de conexión MQTT", ex)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando MQTT client", e)
        }
    }

    /**
     * Publica el estado de telemetría y disponibilidad de la Smart TV en el tópico `cunasegura/tv/status`.
     *
     * @param online `true` si la televisión está lista para recibir alarmas, `false` si se apaga o desconecta.
     */
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

    /**
     * Cierra la sesión activa con el broker MQTT y libera los sockets de red.
     * Publica previamente el estado offline de la televisión.
     */
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

