package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa a un usuario o vecino registrado en la base de datos local SQLite.
 *
 * Mapea la tabla `usuarios` y almacena el perfil del ciudadano, sus credenciales locales,
 * estado de sesión, permisos y coordenadas GPS en tiempo real.
 *
 * @property id Identificador primario autonumérico local.
 * @property nombre Nombre completo del vecino o administrador.
 * @property telefono Número telefónico de contacto (10 dígitos).
 * @property correo Correo electrónico utilizado para autenticación.
 * @property password Contraseña de acceso cifrada o local.
 * @property consentimientoGps Bandera que indica si el usuario autorizó el rastreo GPS en segundo plano.
 * @property latActual Última latitud registrada del dispositivo.
 * @property lonActual Última longitud registrada del dispositivo.
 * @property fcmToken Token de Firebase Cloud Messaging para notificaciones push.
 * @property tvVinculada Indica si el usuario vinculó su Smart TV al centro de monitoreo.
 * @property rol Rol del usuario en el sistema (`usuario`, `admin`, etc.).
 * @property estado Estado de la cuenta (`activo`, `bloqueado`, `pendiente`).
 * @property networkId Identificador de la red vecinal comunitaria a la que pertenece.
 * @property fechaIngreso Timestamp Unix en milisegundos del momento de registro o unión a la red.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
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

