package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa la asignación de acciones por número de toques en SmartWatch.
 *
 * @property id Identificador local.
 * @property usuarioId Clave del usuario.
 * @property cantidadToques Número de toques asociados (1 a 4).
 * @property tipoAccion Identificador de la acción (`MENSAJE_SMS`, `UBICACION_TIEMPO_REAL`, `ALARMA_TV`, `LLAMAR_911`).
 * @property esperar5Seg Bandera para ventana de espera anti-falsa alarma.
 */
data class ConfiguracionToque(
    val id: Int = 0,
    val usuarioId: Int,
    val cantidadToques: Int = 3,
    val tipoAccion: String = "alerta",
    val esperar5Seg: Boolean = true
)

