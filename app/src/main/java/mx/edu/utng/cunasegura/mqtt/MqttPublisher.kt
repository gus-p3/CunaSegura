package mx.edu.utng.cunasegura.mqtt

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mx.edu.utng.cunasegura.BuildConfig
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

/**
 * Modelo de datos serializable para transmisión de eventos de alerta hacia el broker MQTT.
 *
 * @property usuarioId Identificador del usuario emisor.
 * @property nombreUsuario Nombre visible del vecino.
 * @property latitud Coordenada GPS de latitud.
 * @property longitud Coordenada GPS de longitud.
 * @property nivelAlerta Nivel de severidad / número de toques (1 a 4).
 * @property estado Estado de la alerta (`activa`, `cancelada`, `atendida`).
 * @property timestamp Marca de tiempo Unix de emisión.
 * @property networkId Identificador de la red comunitaria receptora.
 */
@Serializable
data class AlertaMqttMessage(
    val usuarioId: Int,
    val nombreUsuario: String = "Vecino Desconocido",
    val latitud: Double,
    val longitud: Double,
    val nivelAlerta: Int = 3,
    val estado: String = "activa",
    val timestamp: Long = System.currentTimeMillis(),
    val networkId: String = ""
)

/**
 * Modelo de datos serializable para eventos de vinculación de Smart TV vía MQTT.
 *
 * @property tvId Identificador único de la pantalla Smart TV.
 * @property networkId Identificador de la red comunitaria enlazada.
 */
@Serializable
data class TvVinculacionMqttMessage(
    val tvId: String,
    val networkId: String
)

/**
 * Cliente publicador MQTT asíncrono basado en Eclipse Paho con soporte TLS/SSL para HiveMQ Cloud.
 *
 * Transmite paquetes JSON hacia los canales `cunasegura/alertas` y `cunasegura/tv/vinculacion`
 * para alimentar en tiempo real la aplicación comunitaria de Smart TV Android Leanback.
 */
object MqttPublisher {
    private const val TAG = "MQTT_PUBLISHER"
    private const val TOPIC_ALERTAS = "cunasegura/alertas"

    /**
     * Publica una alerta SOS en el topic general de alertas de Smart TV a través del broker MQTT HiveMQ.
     *
     * @param usuarioId ID local o hash del usuario emisor.
     * @param nombreUsuario Nombre del vecino en emergencia.
     * @param lat Latitud GPS.
     * @param lon Longitud GPS.
     * @param networkId Red comunitaria a la que pertenece el evento.
     * @param estado Estado de la alerta (`activa` o `cancelada`).
     * @param nivelAlerta Nivel de urgencia (por defecto 3 para TV).
     */
    fun publishAlertaTv(
        usuarioId: Int = 1,
        nombreUsuario: String = "Vecino",
        lat: Double,
        lon: Double,
        networkId: String = "",
        estado: String = "activa",
        nivelAlerta: Int = 3
    ) {
        try {
            val clientId = "cunasegura-phone-${UUID.randomUUID().toString().substring(0, 8)}"
            val client = MqttAsyncClient(
                BuildConfig.HIVEMQ_BROKER_URL,
                clientId,
                MemoryPersistence()
            )

            val options = MqttConnectOptions().apply {
                userName = BuildConfig.HIVEMQ_USERNAME
                password = BuildConfig.HIVEMQ_PASSWORD.toCharArray()
                isCleanSession = true
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
                connectionTimeout = 10
            }

            client.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(token: IMqttToken?) {
                    Log.d(TAG, "✅ Conectado a MQTT. Publicando alerta ($estado)...")
                    
                    val mensaje = AlertaMqttMessage(
                        usuarioId = usuarioId,
                        nombreUsuario = nombreUsuario,
                        latitud = lat,
                        longitud = lon,
                        nivelAlerta = nivelAlerta,
                        estado = estado,
                        networkId = networkId
                    )
                    
                    val payload = Json.encodeToString(AlertaMqttMessage.serializer(), mensaje).toByteArray()
                    
                    try {
                        client.publish(TOPIC_ALERTAS, payload, 1, false)
                        Log.d(TAG, "🚨 Alerta TV publicada exitosamente en MQTT (estado: $estado, net: $networkId)")
                        
                        client.disconnect()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al publicar mensaje MQTT", e)
                    }
                }

                override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                    Log.e(TAG, "❌ Error al conectar a MQTT para publicar alerta", ex)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando MqttPublisher", e)
        }
    }

    /**
     * Publica el evento de vinculación exitosa entre un dispositivo móvil y una Smart TV.
     *
     * @param tvId Identificador escaneado de la pantalla Smart TV.
     * @param networkId Identificador de la red asignada.
     */
    fun publishTvVinculacion(tvId: String, networkId: String) {
        try {
            val clientId = "cunasegura-phone-${UUID.randomUUID().toString().substring(0, 8)}"
            val client = MqttAsyncClient(
                BuildConfig.HIVEMQ_BROKER_URL,
                clientId,
                MemoryPersistence()
            )

            val options = MqttConnectOptions().apply {
                userName = BuildConfig.HIVEMQ_USERNAME
                password = BuildConfig.HIVEMQ_PASSWORD.toCharArray()
                isCleanSession = true
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
                connectionTimeout = 10
            }

            client.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(token: IMqttToken?) {
                    val mensaje = TvVinculacionMqttMessage(tvId, networkId)
                    val payload = Json.encodeToString(TvVinculacionMqttMessage.serializer(), mensaje).toByteArray()
                    try {
                        client.publish("cunasegura/tv/vinculacion", payload, 1, false)
                        client.disconnect()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al publicar vinculacion MQTT", e)
                    }
                }
                override fun onFailure(token: IMqttToken?, ex: Throwable?) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando MqttPublisher para vinculacion", e)
        }
    }
}

