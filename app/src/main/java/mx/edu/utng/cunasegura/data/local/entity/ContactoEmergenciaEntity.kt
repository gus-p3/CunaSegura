package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un contacto de confianza / emergencia para un usuario.
 *
 * Mapea la tabla `contactos_emergencia`, con clave foránea en cascada hacia [UsuarioEntity]
 * para garantizar integridad referencial al eliminar perfiles.
 *
 * @property id Identificador autonumérico del contacto.
 * @property usuarioId Clave foránea al usuario propietario del contacto.
 * @property nombre Nombre o alias del contacto de confianza.
 * @property telefono Número telefónico para recepción de SMS de auxilio y llamadas automáticas.
 * @property relacion Vínculo personal o familiar (ej. Familiar, Vecino, Amigo, Autoridad).
 * @property creadoEn Marca de tiempo Unix de inserción.
 */
@Entity(
    tableName = "contactos_emergencia",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class ContactoEmergenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val telefono: String,
    val relacion: String,
    val creadoEn: Long = System.currentTimeMillis()
)

