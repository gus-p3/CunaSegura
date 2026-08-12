package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que almacena la configuración de acciones mapeadas a secuencias de toques / pulsaciones en SmartWatch.
 *
 * Mapea la tabla `configuracion_toque` con clave foránea en cascada hacia [UsuarioEntity].
 *
 * @property id Identificador autonumérico del registro.
 * @property usuarioId Clave foránea al usuario configurador.
 * @property cantidadToques Número de toques o pulsaciones (1, 2, 3 o 4 toques).
 * @property tipoAccion Identificador de acción asociada (`MENSAJE_SMS`, `UBICACION_TIEMPO_REAL`, `ALARMA_TV`, `LLAMAR_911`).
 * @property esperar5Seg Bandera para habilitar ventana de confirmación anti-falsa alarma.
 */
@Entity(
    tableName = "configuracion_toque",
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
data class ConfiguracionToqueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val cantidadToques: Int = 3,
    val tipoAccion: String = "alerta",
    val esperar5Seg: Boolean = true
)

