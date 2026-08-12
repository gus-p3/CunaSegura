package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa un contacto de confianza y auxilio.
 *
 * @property id Identificador entero del contacto.
 * @property usuarioId Clave del usuario propietario.
 * @property nombre Nombre o alias del contacto.
 * @property telefono Teléfono móvil para notificación SMS y llamadas.
 * @property relacion Parentesco o vínculo de confianza.
 * @property creadoEn Marca de tiempo Unix de registro.
 */
data class ContactoEmergencia(
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val telefono: String,
    val relacion: String,
    val creadoEn: Long = System.currentTimeMillis()
)

