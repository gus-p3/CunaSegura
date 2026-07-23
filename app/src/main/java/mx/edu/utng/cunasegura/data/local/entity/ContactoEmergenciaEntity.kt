package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un contacto de emergencia asociado a un vecino.
 * Tabla: contactos_emergencia
 */
@Entity(
    tableName = "contactos_emergencia",
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
