package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa un evento de alerta SOS ciudadana.
 *
 * @property id Identificador numérico local o remoto.
 * @property usuarioId Clave del usuario emisor.
 * @property nombreUsuario Nombre visible del vecino emisor.
 * @property estado Estado actual (`activa`, `cancelada`, `atendida`).
 * @property latitud Coordenada de latitud GPS del incidente.
 * @property longitud Coordenada de longitud GPS del incidente.
 * @property fueAtendida Indica si la alerta ya fue auxiliada.
 * @property esFalsaAlarma Indica si fue cancelada por error por el usuario.
 * @property creadoEn Marca de tiempo Unix de creación.
 * @property networkId Identificador de la red vecinal receptora.
 */
data class Alerta(
    val id: Int = 0,
    val usuarioId: Int,
    val nombreUsuario: String = "Vecino",
    val estado: String = "activa",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fueAtendida: Boolean = false,
    val esFalsaAlarma: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis(),
    val networkId: String = ""
)

