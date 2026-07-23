package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una alerta ciudadana emitida por un vecino.
 * Tabla: alertas
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
