package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa a un ciudadano o administrador de Cuna Segura.
 *
 * Desacoplado de frameworks de persistencia o red.
 *
 * @property id Identificador entero local asignado en SQLite Room.
 * @property nombre Nombre completo del usuario.
 * @property telefono Teléfono celular (10 dígitos).
 * @property correo Correo electrónico utilizado para autenticación en Firebase Auth.
 * @property password Contraseña de acceso (opcional en UI).
 * @property consentimientoGps Indica si autorizó geolocalización continua.
 * @property latActual Coordenada de latitud actual en tiempo real.
 * @property lonActual Coordenada de longitud actual en tiempo real.
 * @property fcmToken Token de mensajería Firebase Cloud Messaging.
 * @property tvVinculada Indica si la cuenta está asociada a una Smart TV comunitaria.
 * @property rol Rol asignado (`usuario`, `admin`).
 * @property estado Estado de la cuenta (`activo`, `bloqueado`, `pendiente`).
 * @property networkId Identificador de la red vecinal comunitaria activa.
 * @property fechaIngreso Timestamp Unix de afiliación.
 * @property uid Identificador único de Firebase Authentication.
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
    val fechaIngreso: Long = 0L,
    val uid: String = ""
)

