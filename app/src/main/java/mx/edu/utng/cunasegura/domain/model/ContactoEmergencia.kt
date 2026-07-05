package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa un contacto de emergencia.
 */
data class ContactoEmergencia(
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val telefono: String,
    val relacion: String,
    val creadoEn: Long = System.currentTimeMillis()
)
