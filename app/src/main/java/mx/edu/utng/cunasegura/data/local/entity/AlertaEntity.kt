package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que modela un evento de alerta ciudadana de emergencia (SOS) en SQLite.
 *
 * Mapea la tabla `alertas` e indexa por `usuarioId` para optimizar consultas de historial y estado activo.
 *
 * @property id Identificador primario autonumérico de la alerta local.
 * @property usuarioId Clave foránea que referencia al usuario emisor de la alerta.
 * @property nombreUsuario Nombre visible del vecino que detonó la alerta.
 * @property estado Estado operativo de la alerta (`activa`, `cancelada`, `atendida`).
 * @property latitud Coordenada de latitud GPS del lugar de la emergencia.
 * @property longitud Coordenada de longitud GPS del lugar de la emergencia.
 * @property fueAtendida Indica si la alerta fue atendida por vecinos o autoridades.
 * @property esFalsaAlarma Indica si el usuario canceló la alerta dentro de la ventana de gracia de 5 segundos.
 * @property creadoEn Marca de tiempo Unix en milisegundos en que se detonó la alerta.
 */
@Entity(
    tableName = "alertas",
    indices = [Index("usuarioId")]
)
data class AlertaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombreUsuario: String = "Vecino",
    val estado: String = "activa",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fueAtendida: Boolean = false,
    val esFalsaAlarma: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis()
)

