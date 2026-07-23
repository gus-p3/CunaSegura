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

@Serializable
data class TvVinculacionMqttMessage(
    val tvId: String,
    val networkId: String
)

object MqttPublisher {
    private const val TAG = "MQTT_PUBLISHER"
    private const val TOPIC_ALERTAS = "cunasegura/alertas"

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
