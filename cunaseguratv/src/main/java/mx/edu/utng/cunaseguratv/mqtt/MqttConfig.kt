package mx.edu.utng.cunaseguratv.mqtt

import mx.edu.utng.cunaseguratv.BuildConfig

/**
 * Objeto de configuración centralizada para la comunicación MQTT en la Central de Monitoreo (Smart TV).
 *
 * Administra las credenciales leídas dinámicamente desde [BuildConfig] generadas a partir de `local.properties`,
 * así como los nombres de los tópicos del protocolo y parámetros de calidad de servicio (QoS).
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
object MqttConfig {
    /** URL del Broker MQTT (ej. `ssl://xxxx.hivemq.cloud:8883` o `tcp://broker.hivemq.com:1883`). */
    val BROKER_URL: String = BuildConfig.HIVEMQ_BROKER_URL

    /** Nombre de usuario para la autenticación en el Broker HiveMQ. */
    val USERNAME:   String = BuildConfig.HIVEMQ_USERNAME

    /** Contraseña para la autenticación en el Broker HiveMQ. */
    val PASSWORD:   String = BuildConfig.HIVEMQ_PASSWORD

    /** Tópico para la recepción de alertas de emergencia SOS en tiempo real. */
    const val TOPIC_ALERTAS = "cunasegura/alertas"

    /** Tópico para el intercambio de mensajes de vinculación y emparejamiento con el móvil. */
    const val TOPIC_VINCULACION = "cunasegura/tv/vinculacion"

    /** Tópico para publicar el estado de conectividad (Online/Offline) de la Smart TV. */
    const val TOPIC_STATUS = "cunasegura/tv/status"

    /** Nivel de Calidad de Servicio (QoS 1 = Al menos una entrega garantizada). */
    const val QOS = 1

    /** Prefijo para generar identificadores de cliente únicos por cada Smart TV. */
    const val CLIENT_TV_BASE = "cunasegura-tv-"
}

