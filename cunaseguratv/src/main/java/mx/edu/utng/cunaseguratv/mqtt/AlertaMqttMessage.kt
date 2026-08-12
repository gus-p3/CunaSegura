package mx.edu.utng.cunaseguratv.mqtt

import kotlinx.serialization.Serializable

/**
 * Carga útil (Payload) serializable en JSON enviada y recibida mediante el broker MQTT en el tópico `cunasegura/alertas`.
 *
 * Transporta la información crítica en tiempo real sobre la activación o cancelación
 * de una alerta de emergencia originada en un Smartwatch (Wear OS) o Smartphone.
 *
 * @property usuarioId Identificador del usuario emisor del evento.
 * @property nombreUsuario Nombre legible del vecino que detonó la alarma.
 * @property latitud Coordenada GPS de latitud donde se originó el evento.
 * @property longitud Coordenada GPS de longitud donde se originó el evento.
 * @property nivelAlerta Cantidad de toques físicos (1-4) que determinan la severidad o tipo de alerta.
 * @property estado Estado de la alarma ("activa" para disparar sirena/modal, "cancelada" para apagarla).
 * @property timestamp Marca de tiempo epoch en milisegundos de la emisión del mensaje.
 * @property networkId Identificador de la red vecinal a la que pertenece el usuario.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Serializable
data class AlertaMqttMessage(
    val usuarioId: Int,
    val nombreUsuario: String = "Vecino Desconocido",
    val latitud: Double,
    val longitud: Double,
    val nivelAlerta: Int = 3, // Cantidad de toques
    val estado: String = "activa",
    val timestamp: Long = System.currentTimeMillis(),
    val networkId: String = ""
)

/**
 * Mensaje de telemetría y presencia serializable en JSON emitido en el tópico `cunasegura/tv/status`.
 *
 * Permite a otros nodos del ecosistema (como la app móvil) conocer si la Smart TV
 * está en línea y lista para proyectar alertas de emergencia.
 *
 * @property tvId Identificador único asignado a la Smart TV.
 * @property networkId Identificador de la red vecinal a la que está emparejada la TV.
 * @property isOnline Estado booleano de conexión (true = conectada, false = desconectada).
 * @property timestamp Marca de tiempo epoch en milisegundos de la publicación del estado.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Serializable
data class TvStatusMessage(
    val tvId: String,
    val networkId: String,
    val isOnline: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

