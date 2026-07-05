package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa a un vecino de CunaSegura.
 * Sin anotaciones de frameworks de persistencia.
 */
data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val telefono: String,
    val consentimientoGps: Boolean = false,
    val latActual: Double = 0.0,
    val lonActual: Double = 0.0,
    val fcmToken: String = "",
    val tvVinculada: Boolean = false,
    val esAdminGlobal: Boolean = false,
    val estado: String = "activo"
)
