package mx.edu.utng.cunaseguratv.data

/**
 * Modelo de datos inmutable que representa una alerta de seguridad comunitaria en la TV.
 *
 * Utilizado para mapear la información recibida desde Firebase Realtime Database
 * dentro del nodo `/alertas` y reflejar el historial reciente de incidentes en el Dashboard.
 *
 * @property id Identificador entero secuencial de la alerta.
 * @property usuarioId Identificador del usuario que emitió la alerta.
 * @property nombreUsuario Nombre completo del vecino que activó la señal de auxilio.
 * @property estado Estado operativo de la alerta (e.g. "activa", "cancelada", "atendida").
 * @property latitud Coordenada GPS de latitud del incidente.
 * @property longitud Coordenada GPS de longitud del incidente.
 * @property fueAtendida Bandera booleana que indica si las autoridades o vecinos acudieron al auxilio.
 * @property esFalsaAlarma Bandera booleana que marca si la alerta fue clasificada como falsa alarma.
 * @property creadoEn Marca de tiempo epoch en milisegundos cuando se detonó el incidente.
 * @property networkId Identificador de la red vecinal a la que pertenece el usuario emisor.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
data class AlertaTV(
    val id: Int = 0,
    val usuarioId: Int = 0,
    val nombreUsuario: String = "Vecino",
    val estado: String = "activa",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fueAtendida: Boolean = false,
    val esFalsaAlarma: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis(),
    val networkId: String = ""
)

