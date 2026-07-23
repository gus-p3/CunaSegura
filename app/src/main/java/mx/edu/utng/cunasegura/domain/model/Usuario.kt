package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa a un vecino/admin de CunaSegura.
 * Sin anotaciones de frameworks de persistencia.
 */
data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val telefono: String,
    val correo: String = "",
    val password: String = "",
    val consentimientoGps: Boolean = false,
    val latActual: Double = 0.0,
    val lonActual: Double = 0.0,
    val fcmToken: String = "",
    val tvVinculada: Boolean = false,
    val rol: String = "usuario",
    val estado: String = "activo",
    val networkId: String = "",
    val fechaIngreso: Long = 0L
)
