package mx.edu.utng.cunaseguratv.mqtt

import mx.edu.utng.cunaseguratv.BuildConfig

/**
 * Configuración MQTT para la Central de Monitoreo (Smart TV).
 * Las credenciales se leen desde BuildConfig generadas por local.properties.
 */
object MqttConfig {
    val BROKER_URL: String = BuildConfig.HIVEMQ_BROKER_URL
    val USERNAME:   String = BuildConfig.HIVEMQ_USERNAME
    val PASSWORD:   String = BuildConfig.HIVEMQ_PASSWORD

    // Topics del proyecto CunaSegura
    const val TOPIC_ALERTAS = "cunasegura/alertas"
    const val TOPIC_VINCULACION = "cunasegura/tv/vinculacion"
    const val TOPIC_STATUS = "cunasegura/tv/status"

    const val QOS = 1

    // Client ID base (se le agregará un sufijo único por TV para evitar colisiones)
    const val CLIENT_TV_BASE = "cunasegura-tv-"
}
