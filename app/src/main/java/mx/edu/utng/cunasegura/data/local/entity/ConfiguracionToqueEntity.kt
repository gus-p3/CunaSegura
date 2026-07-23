package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que almacena la configuración de toques/golpes para un vecino.
 * Tabla: configuracion_toque
 */
@Entity(
    tableName = "configuracion_toque",
    indices = [Index("usuarioId")]
)
data class ConfiguracionToqueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val cantidadToques: Int = 3,
    val tipoAccion: String = "alerta",
    val esperar5Seg: Boolean = true
)
