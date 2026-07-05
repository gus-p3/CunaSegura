package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa la configuración de toques de un vecino.
 */
data class ConfiguracionToque(
    val id: Int = 0,
    val usuarioId: Int,
    val cantidadToques: Int = 3,
    val tipoAccion: String = "alerta",
    val esperar5Seg: Boolean = true
)
