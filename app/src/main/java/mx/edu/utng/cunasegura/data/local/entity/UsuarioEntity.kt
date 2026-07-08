package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa a un vecino registrado en CunaSegura.
 * Tabla: usuarios
 *
 * version 2: agrega [correo] y [password] para soporte de login admin.
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
    val esAdminGlobal: Boolean = false,
    val estado: String = "activo"
)
