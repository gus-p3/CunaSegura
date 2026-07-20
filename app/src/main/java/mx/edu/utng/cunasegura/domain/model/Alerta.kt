package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa una alerta ciudadana.
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
    val creadoEn: Long = System.currentTimeMillis()
)
